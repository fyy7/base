package com.kind.base.user.service.organdepttype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.Pagination;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * 
 * @author chenzhiwei
 * 
 *         2018年4月23日
 */

@Service
@Action(requireLogin = true, action = "#organ_dept_type_query", description = "机构部门类型列表查询", powerCode = "", requireTransaction = false)
public class QueryOrganDeptTypeListService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		StringBuffer sql = new StringBuffer("SELECT D.ORGAN_ID,O.NAME AS ORGAN,D.DMLX,D.DM,D.DMNR,D.BZ FROM ORGAN_DMB D,SYS_ORGANIZATION_INFO O WHERE D.ORGAN_ID = O.ORGANID");
		String organ_id = dc.getRequestString("SEL_UNIT");
		List<Object> paramData = new ArrayList<Object>();
		if (!StrUtil.isEmpty(organ_id)) {
			sql.append(" AND D.ORGAN_ID = ?");
			paramData.add(organ_id);
		}
		String dmlx = dc.getRequestString("DMLX");
		if (!StrUtil.isEmpty(dmlx)) {
			sql.append(" AND D.DMLX = ?");
			paramData.add(dmlx);
		}
		String dm = dc.getRequestString("DM");
		if (!StrUtil.isEmpty(dm)) {
			sql.append(" AND D.DM = ?");
			paramData.add(dm);
		}
		String dmnr = dc.getRequestString("DMNR");
		if (!StrUtil.isEmpty(dmnr)) {
			sql.append(" AND D.DMNR like ?");
			// paramData.add("%" + dmnr + "%");
			ConvertSqlDefault.addLikeEscapeStr(sql, dmnr, "%%", paramData);
		}
		sql.append(" ORDER BY DMLX,DM");
		/* 执行查询 */
		String page = dc.getRequestString("page");
		String pagesize = dc.getRequestString("rows");
		if (StrUtil.isBlank(page)) {
			page = "1";
		}
		if (StrUtil.isBlank(pagesize)) {
			pagesize = "10";
		}
		Pagination pdata = this.getPagination(hmLogSql, sql.toString(), Integer.parseInt(page), Integer.parseInt(pagesize), paramData);
		dc.setBusiness(ConCommon.PAGINATION_ROWS, pdata.getResultList());
		dc.setBusiness(ConCommon.PAGINATION_TOTAL, pdata.getTotalRows());
		return setSuccessInfo(dc);
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
