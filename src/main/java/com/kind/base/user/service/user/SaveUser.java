package com.kind.base.user.service.user;

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
import com.kind.common.constant.ConCommon;
import com.kind.common.constant.ConProperties;
import com.kind.common.stream.MqBean;
import com.kind.common.utils.Encrypt;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.GenID;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年2月22日
 */
@Service
@Action(requireLogin = true, action = "#user_save", description = "保存用户", powerCode = "register.user", requireTransaction = true)
public class SaveUser extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		List<MqBean> mqList = new ArrayList<>();
		MqBean mqBean = new MqBean();
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

		// 检查用户身份证唯一
		List<Object> param = new ArrayList<>();
		String selectPerfixNumSql = "select count(1) from SYS_N_USERS WHERE ISDEL = 0 AND PKI = ? ";
		param.add(dc.getRequestString("PKI"));

		if (!StrUtil.isEmpty(opno)) {
			param.add(opno);
			selectPerfixNumSql += " AND OPNO !=? ";
		}

		Object perfixNum = this.getOneFiledValue(hmLogSql, selectPerfixNumSql, param);
		if (Integer.valueOf(perfixNum.toString()) != 0) {
			return this.setFailInfo(dc, "该人员身份证已经注册过，请核实！");
		}

		// 前缀与后缀相加
		userBean.set("OPACCOUNT", userBean.get("OPACCOUNT1").toString() + userBean.get("OPACCOUNT2").toString());
		String cmd = "A";
		if (StrUtil.isEmpty(opno)) {
			opno = GenID.gen(36);
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
			mqBean = new MqBean(userBean, MqBean.CMD_A, null);
			mqList.add(mqBean);
			result = this.insert(hmLogSql, userBean);
		} else { // 修改
			cmd = "U";
			userBean.set("UPDATEDATE", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			result = this.update(hmLogSql, userBean);
			mqBean = new MqBean(userBean, MqBean.CMD_U, null);
			mqList.add(mqBean);
			/*
			 * 权限移交先注释掉 for (Object obj : jsonArr) { JSONObject jobj = (JSONObject) obj; int rightsHandleResult; if ("1".equals((String)jobj.get("ISCHANGE"))) { if(!StrUtil.isEmpty((String)jobj.get("PEOPLE_ID"))){ rightsHandleResult = PowerDAO.transferRights(ac.getConnection(), opno, (String)jobj.get("PEOPLE_ID"),(String) jobj.get("DEPT_OLDID")); }else{ rightsHandleResult = PowerDAO.transferRights(ac.getConnection(), opno, "",(String) jobj.get("DEPT_OLDID")); if(rightsHandleResult!=0){ rightsHandleResult = PowerDAO.recoveryRights(ac.getConnection()); } } if(rightsHandleResult==0){ this.setFailInfo(dc, "权限移交时出错了!"); return 0; } } }
			 */
			paramList.add(opno);
			String deleteDeptSql = "DELETE FROM SYS_N_USER_DEPT_INFO WHERE OPNO=?";
			mqBean = new MqBean(deleteDeptSql, paramList, null);
			mqList.add(mqBean);
			if (this.executeSql(hmLogSql, deleteDeptSql, paramList) == 0) {
				return this.setFailInfo(dc, "账号：" + userBean.get("OPACCOUNT") + "删除部门时出错了！");
			}

			String deleteOfficeSql = "DELETE FROM SYS_N_USER_DEPT_OFFICE_TYPE WHERE OPNO=?";
			mqBean = new MqBean(deleteOfficeSql, paramList, null);
			mqList.add(mqBean);
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

		// 部门信息
		String mainDeptId = "";
		DataBean deptInsertBean = new DataBean("SYS_N_USER_DEPT_INFO", "OPNO");
		if (jsonArr != null && jsonArr.size() > 0) {
			for (Object obj : jsonArr) {
				JSONObject jsonObj = (JSONObject) obj;
				if ("1".equals(jsonObj.get("ISCHANGE")) && !StrUtil.isEmpty(jsonObj.getString("PEOPLE_ID"))) {
					UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty("app.user.session.id.param"));

					transferRights(this, hmLogSql, userpo, opno, jsonObj.getString("PEOPLE_ID"), jsonObj.getString("DEPT_OLDID"));
				}
				if (!ConCommon.CMD_D.equals(jsonObj.getString("CMD"))) {
					deptInsertBean.set("OPNO", opno);
					deptInsertBean.set("ORGAN_ID", jsonObj.getString("ORGAN_ID"));
					deptInsertBean.set("ORDIDX", jsonObj.getString("ORDIDX"));
					deptInsertBean.set("DEPT_ID", jsonObj.getString("DEPT_ID"));
					String[] officeTypeInOneDept = jsonObj.getString("OFFICE_TYPE").split(",");
					for (String officeType : officeTypeInOneDept) {
						if (!StrUtil.isEmpty(officeType)) {
							DataBean officeTypeBean = new DataBean("SYS_N_USER_DEPT_OFFICE_TYPE", "DEPT_ID,OPNO");
							officeTypeBean.set("DEPT_ID", jsonObj.getString("DEPT_ID"));
							officeTypeBean.set("OPNO", opno);
							officeTypeBean.set("OFFICE_TYPE", officeType);

							mqBean = new MqBean(officeTypeBean, MqBean.CMD_A, null);
							mqList.add(mqBean);

							if (this.insert(hmLogSql, officeTypeBean) != 1) {
								return this.setFailInfo(dc, "添加部门职务信息失败！");
							}
						}
					}

					deptInsertBean.set("MAIN_DEPT_FLAG", jsonObj.getString("MAIN_DEPT_FLAG"));
					if ("1".equals(jsonObj.getString("MAIN_DEPT_FLAG"))) {
						mainDeptId = jsonObj.getString("DEPT_ID");
					}
					mqBean = new MqBean(deptInsertBean, MqBean.CMD_A, null);
					mqList.add(mqBean);
					if (this.insert(hmLogSql, deptInsertBean) != 1) {
						return this.setFailInfo(dc, "添加部门信息保存失败！");
					}
				}
			}
		}

		if (mqList.size() > 0) {
			return this.setSuccessInfo(dc, "保存成功！");
		}

		return this.setSuccessInfo(dc, "保存失败！");
	}

	@Override
	public String pageAddress(DataContext dc) {
		return null;
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	/**
	 * 权限移交
	 * 
	 * @param conn
	 *            数据库连接
	 * @param original
	 *            原有的id
	 * @param target
	 *            目标id
	 * @param deptId
	 *            部门id
	 * @return
	 */
	public static int transferRights(BaseActionService service, HashMap<String, String> hmLogSql, UserPO userpo, String origin, String target, String deptId) {

		try {
			if (!StrUtil.isEmpty(target)) {
				int result = 0;
				String updateUserRights = "UPDATE SYS_N_USERRIGHTS A SET OPNO=? WHERE OPNO=? AND DEPT_ID=? AND NOT EXISTS(SELECT 1 FROM (SELECT RESOURCEID,DEPT_ID,OPNO,CREATOR_DEPT_ID,CREATOR FROM SYS_N_USERRIGHTS WHERE A.PID<>PID) B WHERE A.RESOURCEID=B.RESOURCEID AND A.DEPT_ID=B.DEPT_ID AND A.CREATOR=B.CREATOR AND A.CREATOR_DEPT_ID=B.CREATOR_DEPT_ID AND B.OPNO=?) AND CREATOR<>?";
				String updateUserRightsCreator = "UPDATE SYS_N_USERRIGHTS A SET CREATOR=? WHERE CREATOR=? AND CREATOR_DEPT_ID=? AND NOT EXISTS(SELECT 1 FROM (SELECT RESOURCEID,DEPT_ID,OPNO,CREATOR_DEPT_ID,CREATOR FROM SYS_N_USERRIGHTS WHERE A.PID<>PID) B WHERE A.RESOURCEID=B.RESOURCEID AND A.CREATOR_DEPT_ID=B.CREATOR_DEPT_ID AND A.DEPT_ID=B.DEPT_ID AND A.OPNO=B.OPNO AND B.CREATOR=?) AND OPNO<>?";
				String deleteUserRights = "DELETE FROM SYS_N_USERRIGHTS WHERE DEPT_ID=? AND (OPNO=? OR CREATOR=?)";
				result += service.executeSql(hmLogSql, updateUserRights, Arrays.asList(target, origin, deptId, target, target));
				result += service.executeSql(hmLogSql, updateUserRightsCreator, Arrays.asList(target, origin, deptId, target, target));
				result += service.executeSql(hmLogSql, deleteUserRights, Arrays.asList(deptId, origin, origin));

				String updateRoleUser = "UPDATE SYS_N_ROLEUSER A SET OPNO=? WHERE OPNO=? AND DEPT_ID=? AND NOT EXISTS(SELECT 1 FROM (SELECT ROLEID,DEPT_ID,OPNO FROM SYS_N_ROLEUSER where A.PID<>PID) B WHERE A.ROLEID=B.ROLEID AND A.DEPT_ID=B.DEPT_ID AND B.OPNO=?) AND NOT EXISTS(SELECT 1 FROM SYS_N_ROLES C WHERE A.ROLEID=C.ROLEID AND A.DEPT_ID=C.DEPT_ID AND C.CREATOR=?)";
				String updateRoleUserCreator = "UPDATE SYS_N_ROLES A SET CREATOR=? WHERE CREATOR=? AND DEPT_ID=?";
				String deleteRoleUser = "DELETE FROM SYS_N_ROLEUSER WHERE (DEPT_ID=? AND OPNO=?) OR EXISTS(SELECT 1 FROM SYS_N_ROLES WHERE SYS_N_ROLEUSER.OPNO=SYS_N_ROLES.CREATOR AND SYS_N_ROLEUSER.ROLEID=SYS_N_ROLES.ROLEID)";
				result += service.executeSql(hmLogSql, updateRoleUser, Arrays.asList(target, origin, deptId, target, target));
				result += service.executeSql(hmLogSql, updateRoleUserCreator, Arrays.asList(target, origin, deptId));
				result += service.executeSql(hmLogSql, deleteRoleUser, Arrays.asList(deptId, origin));
				DataBean transferBean = new DataBean("SYS_USER_AUTHORITY_TRANSFER", "ID");

				transferBean.set("ID", GenID.getUuid32());
				transferBean.set("FROMUSER", origin);
				transferBean.set("TOUSER", target);
				transferBean.set("DEPTID", deptId);
				transferBean.set("OPERATORID", userpo.getOpNo());
				transferBean.set("OPERATORNAME", userpo.getOpName());
				transferBean.set("TRANSFERTIME", DateUtil.now());
				result += service.insert(hmLogSql, transferBean);

				if (result != 7) {
					return 0;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

		return 1;
	}
}
