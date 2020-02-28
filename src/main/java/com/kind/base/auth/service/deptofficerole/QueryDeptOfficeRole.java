package com.kind.base.auth.service.deptofficerole;

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
 * @author LIUHAORAN
 *
 *         2018年3月26日
 */
@Service
@Action(requireLogin = true, action = "#dept_office_role_query", description = "部门、职务权限配置查询", powerCode = "resource.dept_person.power", requireTransaction = false)
public class QueryDeptOfficeRole extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		/* 查询语句与条件 */
		List<Object> param = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("SELECT A.ROLEID,A.REMARK,A.CREATE_OPNAME,A.ROLENAME,A.ORGAN_ID,C.DMNR DEPT_TYPE_NAME,D.DMNR OFFICE_TYPE_NAME,S.NAME AS ORGAN_NAME FROM SYS_N_ROLES_DEPT_OFFICE A");
		sql.append(" JOIN SYS_ORGANIZATION_INFO S ON A.ORGAN_ID=S.ORGANID");
		sql.append(" LEFT JOIN ORGAN_DMB C ON C.DMLX ='DEPT.TYPE' AND A.ORGAN_ID=C.ORGAN_ID AND A.DEPT_TYPE=C.DM");
		sql.append(" LEFT JOIN ORGAN_DMB D ON D.DMLX ='OFFICE.TYPE' AND A.ORGAN_ID=D.ORGAN_ID AND A.OFFICE_TYPE=D.DM WHERE ");

		Boolean needAnd = false;
		String selrRolename = dc.getRequestString("SEL_ROLENAME");
		String organId = dc.getRequestString("ORGAN_ID");
		if (StrUtil.isNotEmpty(selrRolename)) {
			if (needAnd) {
				sql.append(" AND ");
			}
			sql.append("ROLENAME like ?");
			param.add("%" + selrRolename + "%");
			ConvertSqlDefault.addLikeEscapeStr(sql, selrRolename, "%%", param);
			needAnd = true;
		}
		if (StrUtil.isNotEmpty(organId)) {
			if (needAnd) {
				sql.append(" AND ");
			}
			sql.append("A.ORGAN_ID = ?");
			param.add(organId);
			needAnd = true;
		}
		if (StrUtil.isBlank(selrRolename) && StrUtil.isBlank(organId)) {
			sql.append(" 1 = 1 ");
		}
		sql.append(" ORDER BY ROLENAME");

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
}
