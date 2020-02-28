package com.kind.base.user.service.dept;

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
 * @author LIUHAORAN
 * 
 *         2018年4月19日
 */

@Service
@Action(requireLogin = true, action = "#user_select_query", description = "人员选择列表查询", powerCode = "", requireTransaction = false)
public class UserSelectQuery extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(UserSelectQuery.class);

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		List<Object> paramData = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("SELECT A.OPNO AS USERID,A.OPNAME AS USERNAME,B.DEPT_ID AS DEPTID,C.DEPTNAME FROM SYS_N_USERS A LEFT JOIN SYS_N_USER_DEPT_INFO B ON A.OPNO=B.OPNO LEFT JOIN SYS_DEPARTMENT_INFO C ON B.DEPT_ID=C.DEPTID WHERE A.ISDEL!=1 and c.ISDEL!=1");

		// 查询条件
		String organid = dc.getRequestString("ORGANID");
		if (StrUtil.isNotEmpty(organid)) {
			sql.append(" AND B.ORGAN_ID=?");
			paramData.add(organid);
		}
		String deptId = dc.getRequestString("DEPT_ID");
		if (StrUtil.isNotEmpty(deptId)) {
			// 包含子部门
			sql.append(" AND C.ALLORDIDX LIKE (SELECT ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" FROM SYS_DEPARTMENT_INFO WHERE DEPTID=?)");
			paramData.add(deptId);
		}
		String username = dc.getRequestString("SEL_NAME");
		if (StrUtil.isNotEmpty(username)) {
			sql.append(" AND A.OPNAME like ?");
			// paramData.add("%" + username + "%");
			ConvertSqlDefault.addLikeEscapeStr(sql, username, "%%", paramData);

		}

		String page = dc.getRequestString("page");
		String pagesize = dc.getRequestString("rows");
		if (StrUtil.isBlank(page)) {
			page = "1";
		}
		if (StrUtil.isBlank(pagesize)) {
			pagesize = "10";
		}
		log.debug("page:" + page + ",pagesize:" + pagesize);
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
