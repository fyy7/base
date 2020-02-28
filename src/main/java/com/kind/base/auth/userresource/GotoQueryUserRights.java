package com.kind.base.auth.userresource;

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
 * @author fyy
 *
 *         2019年3月6日
 */
@Service
@Action(requireLogin = true, action = "#sys_user_resource_query", description = "用户查询权限", powerCode = "", requireTransaction = false)
public class GotoQueryUserRights extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		/* 查询语句与条件 */
		List<Object> param = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();

		searchFromDB(dc, sql, param);

		/* 执行查询 */
		String page = dc.getRequestString("page");
		String pagesize = dc.getRequestString("rows");
		if (StrUtil.isBlank(page)) {
			page = "1";
		}
		if (StrUtil.isBlank(pagesize)) {
			pagesize = "10";
		}

		Pagination pdata = this.getPagination(hmLogSql, sql.toString(), Integer.parseInt(page), Integer.parseInt(pagesize), param);

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

	/**
	 * 
	 * @Title: searchFromDB @date 2019年3月5日 @author @Description: 查询语句和条件 @param dc @param sql 查询语句 @param param 查询条件 @throws
	 */
	public static void searchFromDB(DataContext dc, StringBuffer sql, List<Object> param) {
		sql.append(" SELECT A.OPACCOUNT,A.OPNAME,D.NAME as UNNAME, C.DEPTNAME as DEPARTMENTNAME,B.MAIN_DEPT_FLAG,A.ENABLED,C.DEPTID as DEPT_ID ,D.ORGANID as ORG_ID ,A.OPNO FROM SYS_N_USERS A,SYS_N_USER_DEPT_INFO B,SYS_DEPARTMENT_INFO C,SYS_ORGANIZATION_INFO D WHERE A.OPNO=B.OPNO AND B.DEPT_ID=C.DEPTID AND C.ORGANID = D.ORGANID  AND A.ISDEL=0 ");
		String sel_type = dc.getRequestString("SEL_TYPE");
		if (sel_type.equals("1")) {
			sql.append(" AND EXISTS( select 1 from SYS_N_USERRIGHTS where OPNO=B.OPNO AND DEPT_ID=C.DEPTID) ");

		} else {
			sql.append(" AND NOT EXISTS( select 1 from SYS_N_USERRIGHTS where OPNO=B.OPNO AND DEPT_ID=C.DEPTID) ");
		}

		String sel_unit = dc.getRequestString("SEL_UNIT");
		if (!StrUtil.isEmpty(sel_unit)) {
			sql.append(" AND D.ORGANID = ?");
			param.add(sel_unit);
		}

		String sel_opname = dc.getRequestString("SEL_OPNAME");
		if (!StrUtil.isEmpty(sel_opname)) {
			sql.append(" AND A.OPNAME LIKE ?");
			ConvertSqlDefault.addLikeEscapeStr(sql, sel_opname, "%%", param);
		}

		String sel_deptid = dc.getRequestString("SEL_DEPTID");
		if (!StrUtil.isEmpty(sel_deptid)) {
			sql.append(" AND C.DEPTID = ?");
			param.add(sel_deptid);
		}

		String sel_opaccount = dc.getRequestString("SEL_OPACCOUNT");
		if (!StrUtil.isEmpty(sel_opaccount)) {
			sql.append(" AND A.OPACCOUNT LIKE ?");
			ConvertSqlDefault.addLikeEscapeStr(sql, sel_opaccount, "%%", param);
		}

		/* sql.append(" ORDER BY B.PAI_CREATE_TIME DESC"); */
	}
}