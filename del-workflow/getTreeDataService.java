package com.kind.base.user.cloudservice.user.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

@Service
@Action(requireLogin = true, action = "#workflow_dept_get_tree_data", description = "工作流-实例用户根据机构选择部门", powerCode = "", requireTransaction = false)
public class getTreeDataService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String organid = dc.getRequestString("ORGANID");
		StringBuffer sql = new StringBuffer("select DEPTID,PARENTID,DEPTNAME TITLE,ALLORDIDX,'' ACCOUNT_PERFIX from SYS_DEPARTMENT_INFO a ");
		sql.append(" where a.ORGANID='").append(organid).append("'");

		// 用户标识，1为本部门，2为本机关所有部门，3为上下级所有，4为上级所有部门，
		// 5为上级加本局部门，6本局加下级部门,7下级部门,8下一级,9下级不包含工商所,10为省局，11为市局,12上一级
		String WF_ACTIVITY_USER_TYPE = dc.getRequestString("WF_ACTIVITY_USER_TYPE");

		// 权限
		String resource_id = dc.getRequestString("RESOURCE_ID");
		// 查出有权限的部门
		if (StrUtil.isNotBlank(resource_id)) {
			sql.append(" and exists(select 1 from V_WF_USERRIGHTS t,SYS_DEPARTMENT_INFO d where ").append(ConvertSqlDefault.charIndexSQL("d.ALLORDIDX", "a.ALLORDIDX")).append(">0 and d.ORGANID='").append(organid).append("' and t.DEPT_ID=d.DEPTID and t.RESOURCEID='").append(resource_id).append("')");
		}

		if ("1".equals(WF_ACTIVITY_USER_TYPE)) {
			// 本部门，为了一棵树的完整性，需要包含父部门
			sql.append(" and exists(select 1 from  SYS_DEPARTMENT_INFO b where ").append(ConvertSqlDefault.charIndexSQL("b.ALLORDIDX", "a.ALLORDIDX")).append(">0 and b.DEPTID='").append(((UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM))).getDeptId()).append("')");
		}

		sql.append(" union all select ORGANID,'ResourceTop',NAME TITLE,'0',ACCOUNT_PERFIX from SYS_ORGANIZATION_INFO where ORGANID='");
		sql.append(organid).append("' order by ALLORDIDX");

		List<Map<String, Object>> list = this.getSelectList(hmLogSql, sql.toString(), new ArrayList<Object>());
		JSONArray arr_json = new JSONArray();
		if (list != null && list.size() > 0) {
			for (Map<String, Object> bean : list) {
				bean.put("id", bean.get("DEPTID"));
				bean.put("pId", bean.get("PARENTID"));
				bean.put("name", bean.get("TITLE"));
				arr_json.add(bean);
			}
		}

		dc.setBusiness("TREE_DATAS", arr_json.toJSONString());
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
