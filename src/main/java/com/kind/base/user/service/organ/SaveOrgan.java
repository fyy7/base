package com.kind.base.user.service.organ;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.constant.ConProperties;
import com.kind.common.stream.MqBean;
import com.kind.common.utils.CodeSwitching;
import com.kind.common.utils.Encrypt;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.GenID;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年2月28日
 */
@Service
@Action(requireLogin = true, action = "#organ_saves", description = "保存组织结构", powerCode = "userpower.organ", requireTransaction = true)
public class SaveOrgan extends BaseActionService {

	private String organId = null;
	private String pId = null;
	private String organPid = null;
	private String cmd = null;
	private String accountPerfix = null;
	private DataBean bean = null;

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// 消息List
		List<MqBean> mqList = new ArrayList<>();
		int result = 0;
		organId = dc.getRequestString("ORGANID");
		pId = dc.getRequestString("PARENTID");
		cmd = dc.getRequestString("CMD");
		accountPerfix = dc.getRequestString("ACCOUNT_PERFIX");

		List<Object> paramList = new ArrayList<Object>();
		if (ConCommon.CMD_A.equals(cmd)) {
			organId = "OR" + GenID.gen(34);
		} else {
			String selectParentIdSql = "select PARENTID from SYS_ORGANIZATION_INFO where ORGANID=?";
			paramList.add(organId);
			organPid = String.valueOf(this.getOneFiledValue(hmLogSql, selectParentIdSql, paramList));
		}

		/* 账号前缀检查 */
		paramList.clear();
		paramList.add(organId);
		paramList.add(accountPerfix);
		String selectPerfixNumSql = "select count(1) from SYS_ORGANIZATION_INFO where ORGANID!=? and ACCOUNT_PERFIX=?";
		Object perfixNum = this.getOneFiledValue(hmLogSql, selectPerfixNumSql, paramList);
		if (Integer.valueOf(perfixNum.toString()) != 0) {
			return this.setFailInfo(dc, "组织账号前缀已经存在，请使用其它的前缀名称！");
		}

		/* 账号名称检查 */
		paramList.clear();
		paramList.add(organId);
		paramList.add(dc.getRequestString("NAME"));
		String selectNameNumSql = "select count(1) from SYS_ORGANIZATION_INFO where ORGANID!=? and NAME=?";
		Object nameNum = this.getOneFiledValue(hmLogSql, selectNameNumSql, paramList);
		if (Integer.valueOf(nameNum.toString()) != 0) {
			return this.setFailInfo(dc, "组织名称已经存在，请重新填写名称！");
		}

		bean = this.getBean(dc, "SYS_ORGANIZATION_INFO", "ORGANID");
		bean.set("ORGANID", organId);
		/* 辅分组只能新建自身的组别机构 */
		if (ConCommon.CMD_A.equals(cmd) || !organPid.equals(pId)) {
			// ORDIDX, 计算
			int ordidx = 1;
			String selectNumBeanSql = "SELECT MAX(ORDIDX) AS MAXORDIDX FROM SYS_ORGANIZATION_INFO WHERE PARENTID =?";
			paramList.clear();
			paramList.add(pId);
			Object maxOrdidx = this.getOneFiledValue(hmLogSql, selectNumBeanSql, paramList);
			if (maxOrdidx != null) {
				ordidx = Integer.valueOf(String.valueOf(maxOrdidx)) + 1;
			}
			bean.set("ORDIDX", ordidx);

			// NLEVEL,ALLORDIDX 计算
			int nlevel = 0;
			String allOrdidx = "";
			String parentAllOrdidx = "";
			String selectLevelBeanSql = "select NLEVEL,ALLORDIDX from SYS_ORGANIZATION_INFO where  ORGANID =?";
			List<Map<String, Object>> levelBean = this.getSelectList(hmLogSql, selectLevelBeanSql, paramList);
			if (levelBean.size() == 1) {
				Object tempNlevel = levelBean.get(0).get("NLEVEL");
				if (tempNlevel != null) {
					nlevel = Integer.valueOf(String.valueOf(levelBean.get(0).get("NLEVEL"))) + 1;
				}
				parentAllOrdidx = String.valueOf(levelBean.get(0).get("ALLORDIDX"));
				if (!ConCommon.STR_NUll.equals(parentAllOrdidx) && !StrUtil.isEmpty(parentAllOrdidx)) {
					if (parentAllOrdidx == null || "".equals(parentAllOrdidx) || ConCommon.STR_NUll.equals(parentAllOrdidx)) {
						parentAllOrdidx = "0";
					}
					allOrdidx = String.valueOf(parentAllOrdidx) + "." + String.valueOf(new java.text.DecimalFormat("0000").format(ordidx));
				}
			}
			bean.set("NLEVEL", nlevel);
			bean.set("ALLORDIDX", allOrdidx);
			// if (!OrganSendUtils.isMainGroup()) {
			// bean.set("GROUPTYPE", OrganSendUtils.getGroupType());
			// }
		}

		/* 添加机构管理员 */
		try {
			if (addOrganAdmin(mqList, hmLogSql, dc) != 1) {
				return this.setFailInfo(dc, "添加机构管理员失败！");
			}
			if (addOrganAdminDept(mqList, hmLogSql, dc, organId) != 1) {
				return this.setFailInfo(dc, "机构管理员初始化部门失败！");
			}

		} catch (Exception e) {
			e.printStackTrace();
			return this.setFailInfo(dc, "保存失败！");
		}

		if (ConCommon.CMD_A.equals(cmd)) {
			result = this.insert(hmLogSql, bean);
			MqBean mqBean = new MqBean(bean, MqBean.CMD_A, null);
			mqList.add(mqBean);

			/* 初始化部门与职务类型 */
			if (result == 1) {
				StringBuffer initDeptOffice = new StringBuffer("INSERT INTO ORGAN_DMB (ORGAN_ID,DMLX,DM,DMNR,DMCC,BZ,BZSM,APP_ID,UUID) SELECT '").append(organId).append("' AS ORGAN_ID,DMLX,DM,DMNR,DMCC,BZ,BZSM,APP_ID,UUID FROM SYS_DMB WHERE DMLX='DEPT.TYPE' OR DMLX='OFFICE.TYPE'");
				paramList.clear();
				StringBuffer queryDeptOffic = new StringBuffer("SELECT '").append(organId).append("' AS ORGAN_ID,DMLX,DM,DMNR,DMCC,BZ,BZSM,APP_ID,UUID FROM SYS_DMB WHERE DMLX='DEPT.TYPE' OR DMLX='OFFICE.TYPE'");
				DataBean[] dmbBeans = this.getSelectArrayDataBean(hmLogSql, queryDeptOffic.toString(), paramList);
				if (dmbBeans != null) {

					for (DataBean dmbBean : dmbBeans) {
						dmbBean.setTableName("ORGAN_DMB");
						dmbBean.setKeyField("ORGAN_ID,DMLX,DM");
						mqBean = new MqBean(dmbBean, MqBean.CMD_A, null);
						mqList.add(mqBean);
					}
					result = this.executeSql(hmLogSql, initDeptOffice.toString(), paramList);
				}
			}
		} else {

			result = this.update(hmLogSql, bean);
			MqBean mqBean = new MqBean(bean, MqBean.CMD_U, null);
			mqList.add(mqBean);

		}

		if (result > 0) {
			return this.setSuccessInfo(dc, "保存成功！");
		}
		return this.setFailInfo(dc, "保存失败！");

	}

	private int addOrganAdmin(List<MqBean> mqList, HashMap<String, String> hmLogSql, DataContext dc) throws Exception {
		DataBean bean = new DataBean("SYS_N_USERS", "OPNO");
		bean.set("OPACCOUNT", this.accountPerfix + ConProperties.SUPER_ACCOUNT);
		bean.set("OPNO", organId);
		bean.set("OPACCOUNT1", this.accountPerfix);
		bean.set("OPACCOUNT2", ConProperties.SUPER_ACCOUNT);

		List<Object> paramList = new ArrayList<Object>();
		paramList.add(organId);
		String sql = "SELECT COUNT(1) FROM SYS_N_USERS WHERE OPNO = ?";
		String organAdminCount = String.valueOf(this.getOneFiledValue(hmLogSql, sql, paramList));

		if (ConCommon.NUM_0.equals(organAdminCount)) {
			// 暂未有机构管理员的，添加,opno值采用机构organId值，与.net版OA规则一致
			bean.set("OPNO", organId);
			bean.set("OPNAME", "机构管理员");

			bean.set("CREATETIME", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
			bean.set("UPDATEDATE", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
			// 参数获取 默认密码
			String defaultPwd = CodeSwitching.getReidiosJsonObj("sys").getString("500"), pwd = null;
			StringBuffer opnoMD5 = new StringBuffer(Encrypt.getAspMD5(bean.getString("OPNO"), ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE));
			String defaultPwdMD5 = Encrypt.getAspMD5(defaultPwd, ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE);
			pwd = Encrypt.getAspMD5(opnoMD5.append(defaultPwdMD5).toString(), ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE);

			bean.set("PWD", pwd);
			bean.set("ENABLED", "1");
			bean.set("REMARK", "组织机构自动添加机构管理员账号");
			// 0为超级管理员，1为某机构管理员，2为普通用户
			bean.set("OPTYPE", "1");
			MqBean mqBean = new MqBean(bean, MqBean.CMD_A, null);
			mqList.add(mqBean);

			return this.insert(hmLogSql, bean);
		} else if (ConCommon.NUM_1.equals(organAdminCount)) {

			MqBean mqBean = new MqBean(bean, MqBean.CMD_U, null);
			mqList.add(mqBean);
			return this.update(hmLogSql, bean);
		}
		return 1;
	}

	/**
	 * 给管理员添加一个默认部门
	 * 
	 * @param ac
	 * @param oaganid
	 * @return
	 * @throws Exception
	 */
	private int addOrganAdminDept(List<MqBean> mqList, HashMap<String, String> hmLogSql, DataContext dc, String oaganid) throws Exception {
		// 判断是否有部门，没有添加一个,同时判断机构管理员是否有部门没有也给他加一个
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(organId);
		String sql = "SELECT COUNT(1) FROM SYS_DEPARTMENT_INFO WHERE ORGANID=? ";
		String organDeptCount = String.valueOf(this.getOneFiledValue(hmLogSql, sql, paramList));

		int status = 1;
		if (ConCommon.NUM_0.equals(organDeptCount)) {
			DataBean instrBean = getOrganDeptBean(dc, oaganid);
			MqBean mqBean = new MqBean(instrBean, MqBean.CMD_A, null);
			mqList.add(mqBean);
			status = this.insert(hmLogSql, instrBean);

			if (status == 0) {
				return status;
			}
		}

		return status;
	}

	/**
	 * 获取默认部门bean （id不能与机构id一致）
	 * 
	 * @param ac
	 * @param oaganid
	 * @return
	 * @throws Exception
	 */
	private DataBean getOrganDeptBean(DataContext dc, String oaganid) throws Exception {
		DataBean bean = new DataBean("SYS_DEPARTMENT_INFO", "DEPTID");
		bean.set("DEPTID", "DEP" + GenID.gen(33));
		bean.set("DEPTNAME", "办公室");
		bean.set("PARENTID", oaganid);
		bean.set("ALLORDIDX", "0.0001.0001");
		bean.set("ORDIDX", "1");

		bean.set("DLEVEL", "1");
		bean.set("ORGANID", oaganid);
		bean.set("IDDEL", "0");
		return bean;
	}

	@Override
	public String pageAddress(DataContext dc) {
		return null;
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
