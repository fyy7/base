/**
 * @Title: GotoResourceMain.java 
 * @Package com.kind.portal.service.resource 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月5日 下午4:39:00 
 * @version V1.0   
 */
package com.kind.base.auth.userresource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kind.base.auth.service.roles.GotoRolesEdit;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

/**
 * @author HUANGLEI
 *
 */
@Service
@Action(requireLogin = true, action = "#sys_user_rights", description = "进入用户直接授权界面", powerCode = "resource.user", requireTransaction = false)
public class GotoUserRights extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));

		String opno = dc.getRequestString("OPNO");
		String dept_id = dc.getRequestString("DEPT_ID");
		Map<String, Object> arr_user = getSelectMap(hmLogSql, "select a.opname,a.opno,c.organid,b.deptid,c.name organName,b.deptname from sys_n_users a,sys_department_info b,sys_organization_info c where a.opno=? and b.deptid=? and b.organid=c.organid AND A.ISDEL=0 AND B.ISDEL=0 AND C.ISDEL=0".toUpperCase(), Arrays.asList(opno, dept_id));
		if (arr_user != null) {
			dc.setBusiness("USER_BEAN", arr_user);
		} else {
			return this.setFailInfo(dc, "未找到用户信息！");
		}

		List<Object> data = new ArrayList<Object>();
		data.add(opno);
		data.add(dept_id);

		data.add(userpo.getOpNo());
		StringBuffer sql = new StringBuffer("SELECT RESOURCEID FROM SYS_N_USERRIGHTS WHERE OPNO=? AND DEPT_ID=? AND CREATOR=? ");

		if (userpo.getOpType() < 2) {
			// 超级管理员、机构管理员
			sql.append(" and CREATOR_DEPT_ID = 'DEPT_ID' ");
		} else {
			sql.append(" and CREATOR_DEPT_ID = ? ");
			data.add(userpo.getDeptId());
		}

		List<Map<String, Object>> arr_resourceId = this.getSelectList(hmLogSql, sql.toString(), data);
		JSONObject jsonData = new JSONObject();
		for (int i = 0; i < arr_resourceId.size(); i++) {
			jsonData.put(arr_resourceId.get(i).get("RESOURCEID").toString(), true);
		}
		dc.setBusiness("USERRIGHTS_JSON", jsonData);
		// 获取已保存的系统、类型数据
		JSONArray backJson = GotoRolesEdit.getResourcesAppinfo(hmLogSql, data, "SYS_N_USERRIGHTS", dc, getSubjectAuthInfo(dc));

		dc.setBusiness("USERRIGHTS_TYPE", backJson);
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext arg0) {
		return "auth/userresource/sys_user_resource_modify";
	}

	@Override
	public String verifyParameter(DataContext arg0) {
		return null;
	}

}
