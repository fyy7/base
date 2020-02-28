package com.kind.base.user.service.organ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.service.BaseActionService;

public class OrganHandle {
	public static List<Map<String, Object>> getOrganAndLowerList(HashMap<String, String> hmLogSql, BaseActionService bas, String organId) {
		StringBuffer orgOptions = new StringBuffer("SELECT ORGANID,ORGANDM,(").append(ConvertSqlDefault.getStrJoinSQL(ConvertSqlDefault.getBlankString("ALLORDIDX", "."), "NAME")).append(") NAME FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND ORGANDM!='OrganizationTop'");
		orgOptions.append(" AND ALLORDIDX LIKE (SELECT ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" FROM SYS_ORGANIZATION_INFO where ISDEL=0 AND ORGANID=?)");
		orgOptions.append(" ORDER BY ALLORDIDX");
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(organId);
		List<Map<String, Object>> superList = bas.getSelectList(hmLogSql, orgOptions.toString(), paramList);
		return superList;
	}
}
