package com.kind.base.user.service.dept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.stream.MqBean;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.GenID;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年3月2日
 */
@Service
@Action(requireLogin = true, action = "#dept_save", description = "保存部门", powerCode = "userpower.branch", requireTransaction = true)
public class SaveDept extends BaseActionService {

	private String deptId = null;
	private String pId = null;
	private String oldPid = null;
	private String deptName = null;
	private String cmd = null;

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// 消息List
		List<MqBean> mqList = new ArrayList<>();

		int result = 0;
		deptId = dc.getRequestString("DEPTID");
		pId = dc.getRequestString("PARENTID");
		deptName = dc.getRequestString("DEPTNAME");
		cmd = dc.getRequestString("CMD");
		String organId = dc.getRequestString("ORGANID");
		List<Object> paramList = new ArrayList<Object>();

		if (ConCommon.CMD_A.equals(cmd)) {
			deptId = "DP" + GenID.gen(34);
		} else {
			String selectParentIdSql = "SELECT PARENTID FROM SYS_DEPARTMENT_INFO WHERE DEPTID=?";
			paramList.add(deptId);
			oldPid = String.valueOf(this.getOneFiledValue(hmLogSql, selectParentIdSql, paramList));
		}

		// 验证同级名称是否存在
		if (checkComLevelNameIfExist(deptId, deptName, pId, organId, cmd, hmLogSql)) {
			return this.setFailInfo(dc, "部门名称已经存在，保存失败！");
		}

		// 验证标识是否与上级标识相对应

		DataBean bean = this.getBean(dc, "SYS_DEPARTMENT_INFO", "DEPTID");
		bean.set("DEPTID", deptId);
		bean.set("ORGANID", organId);

		if (ConCommon.CMD_A.equals(cmd) || !oldPid.equals(pId)) {
			/* ORDIDX, 计算 */
			int ordidx;
			String selectMaxOrdidxSql = "SELECT MAX(ORDIDX) AS MAXORDIDX FROM SYS_DEPARTMENT_INFO WHERE PARENTID =?";
			paramList.clear();
			paramList.add(pId);
			Object maxOrdidx = this.getOneFiledValue(hmLogSql, selectMaxOrdidxSql, paramList);
			if (maxOrdidx == null) {
				ordidx = 1;
			} else {
				ordidx = Integer.parseInt(String.valueOf(maxOrdidx));
			}

			bean.set("ORDIDX", String.valueOf(ordidx));

			/* DLEVEL,ALLORDIDX 计算 */
			int dlevel = 0;
			String allOrdidx = "";
			String parentAllOrdidx = "";

			String selectAllOrdidxSql = "select DLEVEL,ALLORDIDX from SYS_DEPARTMENT_INFO where DEPTID =?";
			List<Map<String, Object>> levelBean = this.getSelectList(hmLogSql, selectAllOrdidxSql, paramList);
			if (levelBean.size() == 1) {
				Object tempDlevel = levelBean.get(0).get("DLEVEL");
				Object tempAllOrdidx = levelBean.get(0).get("ALLORDIDX");
				if (tempDlevel != null) {
					dlevel = Integer.parseInt(String.valueOf(tempDlevel)) + 1;
				}
				if (tempAllOrdidx != null) {
					parentAllOrdidx = String.valueOf(tempAllOrdidx);
					if (StrUtil.isEmpty(parentAllOrdidx)) {
						parentAllOrdidx = "0";
					}

					allOrdidx = String.valueOf(parentAllOrdidx) + "." + String.valueOf(new java.text.DecimalFormat("0000").format(Integer.parseInt(String.valueOf(ordidx))));
				}
			} else {
				dlevel = dlevel + 1;
				allOrdidx = "0.0001." + String.valueOf(new java.text.DecimalFormat("0000").format(ordidx));
			}
			bean.set("DLEVEL", String.valueOf(dlevel));
			bean.set("ALLORDIDX", allOrdidx);
		}

		/* 处理部门类型 */
		String deleteSql = "DELETE FROM SYS_DEPARTMENT_TYPE WHERE DEPTID=?";
		paramList.clear();
		paramList.add(deptId);

		DataBean tmpBean = new DataBean("SYS_DEPARTMENT_TYPE", "DEPTID");
		tmpBean.set("DEPTID", deptId);
		MqBean mqBean = new MqBean(tmpBean, MqBean.CMD_D, null);
		mqList.add(mqBean);
		if (this.executeSql(hmLogSql, deleteSql, paramList) < 0) {
			return this.setFailInfo(dc, "执行失败");
		}

		
		String[] bselect = (String[]) dc.getRequestObject("BSELECT");
		String[] bvalue = (String[]) dc.getRequestObject("BVALUE");
		String[] btitle = (String[]) dc.getRequestObject("BTITLE");
		for (int i = 0; i < bselect.length; i++) {
			if (ConCommon.NUM_1.equals(bselect[i])) {
				DataBean tyepBean = new DataBean();
				tyepBean.setTableName("SYS_DEPARTMENT_TYPE");
				tyepBean.set("DEPTID", bean.get("DEPTID"));
				tyepBean.set("BVALUE", bvalue[i]);
				tyepBean.set("BTITLE", btitle[i]);

				mqBean = new MqBean(tyepBean, MqBean.CMD_A, null);
				mqList.add(mqBean);
				if (this.insert(hmLogSql, tyepBean) == 0) {
					return this.setFailInfo(dc, "执行失败");
				}
			}
		}
		 

		if (ConCommon.CMD_A.equals(cmd)) {
			mqBean = new MqBean(bean, MqBean.CMD_A, null);
			mqList.add(mqBean);
			result = this.insert(hmLogSql, bean);
		} else {
			mqBean = new MqBean(bean, MqBean.CMD_U, null);
			mqList.add(mqBean);
			result = this.update(hmLogSql, bean);
		}

		if (result > 0) {
			return this.setSuccessInfo(dc, "保存成功！");
		}

		return this.setFailInfo(dc, "保存失败！");
	}

	/**
	 * 验证同级名称是否存在
	 * 
	 * @param deptId
	 * @param name
	 * @param pid
	 * @param unit
	 * @param cmd
	 * @param hmLogSql
	 * @return
	 */
	public boolean checkComLevelNameIfExist(String deptId, String name, String pid, String unit, String cmd, HashMap<String, String> hmLogSql) {

		List<Object> paramList = new ArrayList<Object>();
		StringBuffer csql = new StringBuffer("");
		if (ConCommon.CMD_A.equals(cmd)) {
			csql.append("SELECT COUNT(1) NUM FROM SYS_DEPARTMENT_INFO WHERE ISDEL=0 AND PARENTID=? and ORGANID=? and DEPTNAME =?");
			paramList.add(pid);
			paramList.add(unit);
			paramList.add(name);
		} else {
			csql.append("SELECT COUNT(1) NUM FROM SYS_DEPARTMENT_INFO WHERE ISDEL=0 AND PARENTID=? and ORGANID=? and DEPTNAME =? and DEPTID <> ?");
			paramList.add(pid);
			paramList.add(unit);
			paramList.add(name);
			paramList.add(deptId);
		}

		Object cname = this.getOneFiledValue(hmLogSql, csql.toString(), paramList);
		if (cname != null) {
			int count = Integer.parseInt(String.valueOf(cname));
			if (count > 0) {
				return true;
			}
			return false;
		}
		return false;
	}

	/**
	 * 验证标识是否与上级标识相对应
	 * 
	 * @param dm
	 * @param pId
	 * @param hmLogSql
	 * @return
	 * @throws Exception
	 */
	public boolean checkDMAndPDM(String dm, String pId, HashMap<String, String> hmLogSql) throws Exception {
		// 获取上级部门标识
		String or = "OR", strBegin = pId.substring(0, 2);
		String sql = "";
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(pId);
		if (or.equals(strBegin)) {
			sql = "SELECT ORGANDM FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND ORGANID=?";
		} else {
			sql = "SELECT DEPTDM FROM SYS_DEPARTMENT_INFO WHERE ISDEL=0 AND DEPTID=?";
		}
		String parentDm = String.valueOf(this.getOneFiledValue(hmLogSql, sql.toString(), paramList));
		if (dm.subSequence(0, parentDm.length()).equals(parentDm)) {
			return false;
		}
		return true;
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
