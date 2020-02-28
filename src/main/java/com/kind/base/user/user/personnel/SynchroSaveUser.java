package com.kind.base.user.user.personnel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kind.base.user.service.user.IMCommonOperate;
import com.kind.base.user.service.user.SaveUser;
import com.kind.common.constant.ConCommon;
import com.kind.common.constant.ConProperties;
import com.kind.common.utils.Encrypt;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年2月22日
 */
@Service
@Action(requireLogin = true, action = "#synchro_user_save", description = "保存（比对人事信息后的）账户信息", powerCode = "userpower.personnel.synchro_3", requireTransaction = true)
public class SynchroSaveUser extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		int result;
		List<Object> paramList = new ArrayList<Object>();

		DataBean userBean = this.getBean(dc, "SYS_N_USERS", "OPNO");
		String opno = dc.getRequestString("OPNO");
		String pwd = dc.getRequestString("PWD");

		String jsonStr = dc.getRequestString("JSONDATA");
		if (StrUtil.isEmpty(jsonStr)) {
			return this.setFailInfo(dc, "非法操作！");
		}
		JSONArray jsonArr = JSON.parseArray(jsonStr);

		// 前缀与后缀相加
		userBean.set("OPACCOUNT", userBean.get("OPACCOUNT1").toString() + userBean.get("OPACCOUNT2").toString());
		String count = this.getOneFiledValue(hmLogSql, "select count(1) from sys_n_users where opno=?", Arrays.asList(opno)).toString();
		if (Integer.valueOf(count) == 0) {

			userBean.set("CREATETIME", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			userBean.set("UPDATEDATE", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

			// md5加密
			boolean useSimplePwd = false;
			String defaultPwdStr = "1";
			if (useSimplePwd) {
				pwd = Encrypt.getAspMD5(defaultPwdStr, ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE);
			} else {
				StringBuffer opnoMD5 = new StringBuffer(Encrypt.getAspMD5(opno, ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE));
				String defaultPwdMD5 = Encrypt.getAspMD5(defaultPwdStr, ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE);
				pwd = Encrypt.getAspMD5(opnoMD5.append(defaultPwdMD5).toString(), ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE);
			}

			userBean.set("PWD", pwd);
			userBean.set("OPNO", opno);
			result = this.insert(hmLogSql, userBean);
		} else { // 修改
			userBean.set("UPDATEDATE", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			result = this.update(hmLogSql, userBean);
			/*
			 * 权限移交先注释掉 for (Object obj : jsonArr) { JSONObject jobj = (JSONObject) obj; int rightsHandleResult; if ("1".equals((String)jobj.get("ISCHANGE"))) { if(!StrUtil.isEmpty((String)jobj.get("PEOPLE_ID"))){ rightsHandleResult = PowerDAO.transferRights(ac.getConnection(), opno, (String)jobj.get("PEOPLE_ID"),(String) jobj.get("DEPT_OLDID")); }else{ rightsHandleResult = PowerDAO.transferRights(ac.getConnection(), opno, "",(String) jobj.get("DEPT_OLDID")); if(rightsHandleResult!=0){ rightsHandleResult = PowerDAO.recoveryRights(ac.getConnection()); } } if(rightsHandleResult==0){ this.setFailInfo(dc, "权限移交时出错了!"); return 0; } } }
			 */
			paramList.add(opno);
			String deleteDeptSql = "DELETE FROM SYS_N_USER_DEPT_INFO WHERE OPNO=?";
			if (this.executeSql(hmLogSql, deleteDeptSql, paramList) == 0) {
				return this.setFailInfo(dc, "账号：" + userBean.get("OPACCOUNT") + "删除部门时出错了！");
			}

			String deleteOfficeSql = "DELETE FROM SYS_N_USER_DEPT_OFFICE_TYPE WHERE OPNO=?";
			if (this.executeSql(hmLogSql, deleteOfficeSql, paramList) == 0) {
				return this.setFailInfo(dc, "账号：" + userBean.get("OPACCOUNT") + "删除部门职务时出错了！");
			}
		}

		// 查询账号是否已经重复
		paramList = new ArrayList<>();
		paramList.add(userBean.getString("OPACCOUNT"));
		paramList.add(opno);
		Object oldNum = this.getOneFiledValue(hmLogSql, "SELECT COUNT(1) FROM SYS_N_USERS WHERE OPACCOUNT=? AND OPNO<>? AND ISDEL=0", paramList);
		if (!ConCommon.NUM_0.equals(oldNum.toString())) {
			return this.setFailInfo(dc, "账号：" + userBean.get("OPACCOUNT") + "已经重复了，请检查！");
		}

		// 保存结果提示
		if (result != 1) {
			return this.setFailInfo(dc, "保存失败！");
		}
		String mainDeptId = "";
		boolean haveMainDept = false;// 是否存在主要部门
		// 部门信息
		DataBean deptInsertBean = new DataBean("SYS_N_USER_DEPT_INFO", "OPNO");
		if (jsonArr != null && jsonArr.size() > 0) {
			for (Object obj : jsonArr) {
				JSONObject jsonObj = (JSONObject) obj;
				if ("1".equals(jsonObj.get("ISCHANGE")) && !StrUtil.isEmpty(jsonObj.getString("PEOPLE_ID"))) {
					UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty("app.user.session.id.param"));

					SaveUser.transferRights(this, hmLogSql, userpo, opno, jsonObj.getString("PEOPLE_ID"), jsonObj.getString("DEPT_OLDID"));
				}
				if (!ConCommon.CMD_D.equals(jsonObj.getString("CMD"))) {
					deptInsertBean.set("OPNO", opno);
					deptInsertBean.set("ORGAN_ID", jsonObj.getString("ORGAN_ID"));
					deptInsertBean.set("DEPT_ID", jsonObj.getString("DEPT_ID"));
					String[] officeTypeInOneDept = jsonObj.getString("OFFICE_TYPE").split(",");
					for (String officeType : officeTypeInOneDept) {
						if (!StrUtil.isEmpty(officeType)) {
							DataBean officeTypeBean = new DataBean("SYS_N_USER_DEPT_OFFICE_TYPE", "DEPT_ID,OPNO");
							officeTypeBean.set("DEPT_ID", jsonObj.getString("DEPT_ID"));
							officeTypeBean.set("OPNO", opno);
							officeTypeBean.set("OFFICE_TYPE", officeType);

							if (this.insert(hmLogSql, officeTypeBean) != 1) {
								return this.setFailInfo(dc, "添加部门职务信息失败！");
							}
						}
					}
					if ("1".equals(jsonObj.getString("MAIN_DEPT_FLAG"))) {
						haveMainDept = true;
					}
					deptInsertBean.set("MAIN_DEPT_FLAG", jsonObj.getString("MAIN_DEPT_FLAG"));
					if ("1".equals(jsonObj.getString("MAIN_DEPT_FLAG"))) {
						mainDeptId = jsonObj.getString("DEPT_ID");
					}
					if (this.insert(hmLogSql, deptInsertBean) != 1) {
						return this.setFailInfo(dc, "添加部门信息保存失败！");
					}
				}
			}
		}
		if (!haveMainDept) {
			return this.setFailInfo(dc, "未检测到有效的主要部门！");
		}
		// 这里还应该处理有赋部门角色的删除
		/*
		 * 待续
		 */
		if (this.executeSql(hmLogSql, "update hr_personnel_base_image set data_exchange_flag='0' where personid=?", Arrays.asList(opno)) != 1) {
			return this.setFailInfo(dc, "跟新同步信息失败！");
		}
		DataBean selectDataBean = this.getSelectDataBean(hmLogSql, "select u.*,nvl(d.dept_id,'350000') dept_id from SYS_N_USERS u left join sys_n_user_dept_info d on u.OPNO =d.OPNO and d.main_dept_flag =1 where u.OPNO =? AND u.ISDEL=0", java.util.Arrays.asList(opno));

		if (IMCommonOperate.updateUserByBean(this, dc, selectDataBean, Integer.valueOf(count) == 0 ? "A" : "U", mainDeptId) == 0) {
			return 0;
		}
		return this.setSuccessInfo(dc, "保存成功！");
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
