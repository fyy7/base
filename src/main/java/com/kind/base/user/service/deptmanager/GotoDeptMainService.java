package com.kind.base.user.service.deptmanager;

import java.util.Arrays;
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
 * @author chenzhiwei
 *
 *         2018年4月3日
 */
@Service
@Action(requireLogin = true, action = "#user_dept_main", description = "用户排序", powerCode = "userpower.user.order", requireTransaction = false)
public class GotoDeptMainService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		// 获取登陆者Session信息
		String unit = userpo.getOrgId() == null ? "00000000-0000-0000-1000-000000000000" : userpo.getOrgId(); // 组织结构
		StringBuffer sql = new StringBuffer("select DEPTID,DEPTNAME,PARENTID,ALLORDIDX from SYS_DEPARTMENT_INFO where ISDEL=0 AND ORGANID=? union select ORGANID as DEPTID,NAME as DEPTNAME,PARENTID,ALLORDIDX from SYS_ORGANIZATION_INFO where ISDEL=0 AND ORGANID=? order by ALLORDIDX");
		List<Map<String, Object>> list = getSelectList(hmLogSql, sql.toString(), Arrays.asList(unit, unit));
		JSONArray arrjson = new JSONArray();
		if (list != null) {
			for (Map<String, Object> bean : list) {
				bean.put("id", bean.get("DEPTID"));
				bean.put("pId", bean.get("PARENTID"));
				bean.put("name", bean.get("DEPTNAME"));

				arrjson.add(JSON.toJSON(bean));
			}
		}

		/* 查询机构数据 */
		StringBuffer orgOptions = new StringBuffer("SELECT ORGANID,ORGANDM,(").append(ConvertSqlDefault.getStrJoinSQL(ConvertSqlDefault.getBlankString("ALLORDIDX", "."), "NAME")).append(") NAME FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND ORGANDM!='OrganizationTop'");
		orgOptions.append(" AND ALLORDIDX LIKE (SELECT ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" FROM SYS_ORGANIZATION_INFO where ISDEL=0 AND ORGANID=?)");
		orgOptions.append(" ORDER BY ALLORDIDX");
		List<Map<String, Object>> superList = this.getSelectList(hmLogSql, orgOptions.toString(), Arrays.asList(unit));
		dc.setBusiness("SUPER_LIST", superList);

		dc.setBusiness("DEPT_LIST", arrjson.toJSONString());
		dc.setBusiness("DEPTID", dc.getRequestString("DEPTID"));
		dc.setBusiness("ORGANID", unit);
		return this.setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		String type = dc.getRequestString("type");
		if ("U".equals(type)) {
			return "user/userorder/user_dept_main";
		}
		return "user/dept/dept_main";
	}

	@Override
	public String verifyParameter(DataContext arg0) {
		return null;
	}

}
