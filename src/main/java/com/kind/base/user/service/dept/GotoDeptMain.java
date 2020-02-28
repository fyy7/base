package com.kind.base.user.service.dept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

/**
 * @author
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = true, action = "#dept_main", description = "部门管理", powerCode = "userpower.branch", requireTransaction = false)
public class GotoDeptMain extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String deptId = dc.getRequestString("DEPTID");

		UserPO userpo = (UserPO) dc.getSession().get(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		String organId = userpo.getOrgId() == null ? "00000000-0000-0000-1000-000000000000" : userpo.getOrgId();

		/* 查询部门数据 */
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(organId);
		paramList.add(organId);
		StringBuffer sql = new StringBuffer("SELECT DEPTID,DEPTNAME,PARENTID,ALLORDIDX FROM SYS_DEPARTMENT_INFO WHERE ISDEL=0 AND ORGANID=? UNION SELECT ORGANID AS DEPTID,NAME AS DEPTNAME,PARENTID,ALLORDIDX FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND ORGANID=? ORDER BY ALLORDIDX");
		List<Map<String, Object>> list = this.getSelectList(hmLogSql, sql.toString(), paramList);

		JSONArray arrjson = new JSONArray();
		if (list != null) {
			for (Map<String, Object> map : list) {
				map.put("id", map.get("DEPTID"));
				map.put("pId", map.get("PARENTID"));
				map.put("name", map.get("DEPTNAME"));

				arrjson.add(JSON.parseObject(JSON.toJSONString(map)));
			}
		}

		/* 查询机构数据 */
		StringBuffer orgOptions = new StringBuffer("SELECT ORGANID,ORGANDM,(").append(ConvertSqlDefault.getStrJoinSQL(ConvertSqlDefault.getBlankString("ALLORDIDX", "."), "NAME")).append(") NAME FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND ORGANDM!='OrganizationTop'");
		orgOptions.append(" AND ALLORDIDX LIKE (SELECT ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" FROM SYS_ORGANIZATION_INFO where ISDEL=0 AND ORGANID=?)");
		orgOptions.append(" ORDER BY ALLORDIDX");
		paramList.remove(1);
		List<Map<String, Object>> superList = this.getSelectList(hmLogSql, orgOptions.toString(), paramList);
		dc.setBusiness("SUPER_LIST", superList);

		dc.setBusiness("DEPT_LIST", arrjson.toJSONString());
		dc.setBusiness("DEPTID", deptId);
		dc.setBusiness("ORGANID", organId);

		return this.setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "user/dept/dept_main";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
