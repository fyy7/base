package com.kind.base.user.cloudservice.user.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

@Service
@Action(requireLogin = true, action = "#workflow_get_user_resourceid", description = "工作流-获取用户权限角色id", powerCode = "", requireTransaction = false)
public class getWflowResource extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty("app.user.session.id.param"));
		List<Object> paramList = new ArrayList<>();

		String type = dc.getRequestString("TYPE");
		paramList.add(userpo.getOpNo());
		StringBuffer sql = new StringBuffer("select RESOURCEID,DEPT_ID as DEPTID,ORGANID from V_WF_USERRIGHTS where OPNO=? ");
		switch (type.toUpperCase()) {
		case "DEPTID":

			sql.append(" and DEPT_ID =?");
			paramList.add(userpo.getDeptId());
			break;
		case "ORGANID":
			sql.append(" and ORGANID =?");
			paramList.add(userpo.getOrgId());
			break;
		default:
			break;
		}
		List<Map<String, Object>> resourceList = getSelectList(hmLogSql, sql.toString(), paramList);
		dc.setBusiness("RESOURCE_LIST", resourceList);
		return this.setSuccessInfo(dc);
	}

	@Override
	public String verifyParameter(DataContext arg0) {
		return null;
	}

	@Override
	public String pageAddress(DataContext arg0) {
		return null;
	}
}
