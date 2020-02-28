package com.kind.base.user.cloudservice.user.organ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = true, action = "#organ_query", description = "机构查询", powerCode = "", requireTransaction = false)
public class QueryOrgan extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String organId = dc.getRequestString("ORGID");
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(organId);

		/* 查询机构数据 */
		StringBuffer orgOptions = new StringBuffer("SELECT ORGANID,ORGANDM,(").append(ConvertSqlDefault.getStrJoinSQL(ConvertSqlDefault.getBlankString("ALLORDIDX", "."), "NAME")).append(") NAME2,NAME FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND ORGANDM!='OrganizationTop'");
		orgOptions.append(" AND ALLORDIDX LIKE (SELECT ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" FROM SYS_ORGANIZATION_INFO where ISDEL=0 AND ORGANID=?)");
		orgOptions.append(" ORDER BY ALLORDIDX");
		List<Map<String, Object>> orgList = this.getSelectList(hmLogSql, orgOptions.toString(), paramList);
		dc.setBusiness("ORG_LIST", orgList);

		return this.setSuccessInfo(dc);
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
