package com.kind.base.user.cloudservice.user.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.Pagination;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * @author
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = true, action = "#workflow_get_select_user", description = "工作流-用户选择查询", powerCode = "", requireTransaction = false)
public class WFlowGetUserDataService extends BaseActionService {
	// 机构id
	String organid;

	// 部门id
	String deptId;

	// 用户类型 0 个人 1部门 2机关
	String userType;

	// 资源ID
	String resourceId;

	// 所有数据
	String allDataFlag;

	// 查询名称
	String selName;

	UserPO supo;
	/*
	 * 用户标识
	 * 
	 * 本部门[1] 本机关所有部门[2] 上下级所有部门[3] 上级所有部门[4] 下级所有部门[5] 本局加上级部门[6] 本局加下级部门[7] 下一级部门[8] 上一级部门[9] 顶级机构[11] 二级机构[12]
	 */
	String wfActivityUserType;

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		List<Object> paramData = new ArrayList<Object>();

		supo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		dc.setRequestToBusiness();

		// 机构id
		organid = dc.getRequestString("ORGANID");

		// 部门id
		deptId = dc.getRequestString("DEPT_ID");

		// 用户类型 0 个人 1部门 2机关
		userType = dc.getRequestString("USER_TYPE");

		// 资源ID
		resourceId = dc.getRequestString("RESOURCE_ID");

		// 所有数据
		allDataFlag = dc.getRequestString("ALL_DATA_FLAG");

		// 查询名称
		selName = dc.getRequestString("SEL_NAME");

		/*
		 * 用户标识
		 * 
		 * 本部门[1] 本机关所有部门[2] 上下级所有部门[3] 上级所有部门[4] 下级所有部门[5] 本局加上级部门[6] 本局加下级部门[7] 下一级部门[8] 上一级部门[9] 下级不包含工商所[10] 省局[11] 市局[12]
		 */
		wfActivityUserType = dc.getRequestString("WF_ACTIVITY_USER_TYPE");

		String sql = null;
		// 用户类型 0 个人 1部门 2机关
		if ("0".equals(userType)) {
			sql = getPersionSql();
		}
		if ("1".equals(userType)) {
			sql = getDeptSql();
		}
		if ("2".equals(userType)) {
			sql = getOrganSql();
		}

		String page = "1".equals(allDataFlag) ? "1" : dc.getRequestString("page");
		String rows = "1".equals(allDataFlag) ? "1000" : dc.getRequestString("rows");
		if (StrUtil.isBlank(page)) {
			page = "1";
		}
		if (StrUtil.isBlank(rows)) {
			rows = "10";
		}
		Pagination pdata = this.getPagination(hmLogSql, sql.toString(), Integer.parseInt(page), Integer.parseInt(rows), paramData);
		dc.setBusiness("rows", pdata.getResultList());
		dc.setBusiness("total", pdata.getTotalRows());

		return this.setSuccessInfo(dc);
	}

	private String getOrganSql() {
		// 0 机关
		StringBuffer sb_sql = new StringBuffer("select D.ORGANID OPNO, D.NAME OPNAME, D.ORGANID DEPTID, D.NAME DEPTNAME,D.ORGANID, D.NAME ORGANNAME from SYS_ORGANIZATION_INFO D where NLEVEL!=1");

		// 这里需要设置wfActivityUserType的过滤
		// 用户标识，1为本部门，2为本机关所有部门，3为上下级所有，4为上级所有部门，
		// 5为上级加本局部门，6本局加下级部门,7下级部门,8下一级,9下级不包含工商所,10为省局，11为市局,12上一级

		/*
		 * 用户标识
		 * 
		 * 本部门[1] 本机关所有部门[2] 上下级所有部门[3] 上级所有部门[4] 下级所有部门[5] 本局加上级部门[6] 本局加下级部门[7] 下一级部门[8] 上一级部门[9] 下级不包含工商所[10] 省局[11] 市局[12]
		 */

		if ("1".equals(wfActivityUserType) //
				|| "2".equals(wfActivityUserType)//
				|| "3".equals(wfActivityUserType)//
				|| "4".equals(wfActivityUserType)//
				|| "5".equals(wfActivityUserType)//
				|| "9".equals(wfActivityUserType)//
				|| "10".equals(wfActivityUserType)//
				|| "11".equals(wfActivityUserType)//
				|| "12".equals(wfActivityUserType)//

		) {
			if (StrUtil.isNotBlank(this.deptId)) {
				// 本局的
				sb_sql.append(" and D.ORGANID='").append(this.deptId).append("'");
			}
		} else {
			if (StrUtil.isNotBlank(this.deptId)) {
				// 包含子部门
				sb_sql.append(" and D.ALLORDIDX like (select ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" from SYS_ORGANIZATION_INFO where ORGANID='").append(this.deptId).append("')");
			}
		}

		if (StrUtil.isNotBlank(this.selName)) {
			sb_sql.append(" and D.NAME like '%").append(this.selName).append("%'");
		}

		sb_sql.append(" order by D.ALLORDIDX");
		return sb_sql.toString();
	}

	private String getDeptSql() {
		// 0 部门
		StringBuffer sb_sql = new StringBuffer("select C.DEPTID OPNO, C.DEPTNAME OPNAME, C.DEPTID, C.DEPTNAME, D.ORGANID, D.NAME ORGANNAME from SYS_DEPARTMENT_INFO C, SYS_ORGANIZATION_INFO D ");

		sb_sql.append(" where D.ORGANID = C.ORGANID");

		if (StrUtil.isNotBlank(this.resourceId)) {
			// 有权限
			sb_sql.append(" and exists(select 1 from V_WF_USERRIGHTS V where ");
			sb_sql.append(" C.DEPTID=V.DEPT_ID and V.RESOURCEID='").append(this.resourceId).append("')");
		}
		if (StrUtil.isNotBlank(this.organid)) {
			sb_sql.append(" and D.ORGANID='").append(organid).append("'");
		}

		if ("1".equals(wfActivityUserType)) {
			// 本部门的
			sb_sql.append(" and C.DEPTID='").append(supo.getDeptId()).append("'");
		} else {
			if (StrUtil.isNotBlank(this.deptId)) {
				// 包含子部门
				sb_sql.append(" and C.ALLORDIDX like (select ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" from SYS_DEPARTMENT_INFO where DEPTID='").append(this.deptId).append("')");
			}
		}

		if (StrUtil.isNotBlank(this.selName)) {
			sb_sql.append(" and C.DEPTNAME like '%").append(this.selName).append("%'");
		}

		sb_sql.append(" order by C.ALLORDIDX");

		return sb_sql.toString();
	}

	private String getPersionSql() {
		// 0 个人
		StringBuffer sb_sql = new StringBuffer("select distinct A.OPNO, A.OPNAME, C.DEPTID, C.DEPTNAME, D.ORGANID, D.NAME ORGANNAME from SYS_N_USERS A, SYS_N_USER_DEPT_INFO B, SYS_DEPARTMENT_INFO C, SYS_ORGANIZATION_INFO D ");

		sb_sql.append(" where A.OPNO = B.OPNO and C.DEPTID = B.DEPT_ID and D.ORGANID = C.ORGANID");

		if (StrUtil.isNotBlank(this.resourceId)) {
			// 有权限
			sb_sql.append(" and exists(select 1 from V_WF_USERRIGHTS V");
			sb_sql.append(" where V.OPNO=A.OPNO and C.DEPTID=V.DEPT_ID and V.RESOURCEID='").append(this.resourceId).append("')");
		}
		if (StrUtil.isNotBlank(this.organid)) {
			sb_sql.append(" and D.ORGANID='").append(organid).append("'");
		}
		if ("1".equals(wfActivityUserType)) {
			// 本部门的
			sb_sql.append(" and C.DEPTID='").append(supo.getDeptId()).append("'");
		} else {
			if (StrUtil.isNotBlank(this.deptId)) {
				// 包含子部门
				sb_sql.append(" and C.ALLORDIDX like (select ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" from SYS_DEPARTMENT_INFO where DEPTID='").append(this.deptId).append("')");
			}
		}

		if (StrUtil.isNotBlank(this.selName)) {
			sb_sql.append(" and A.OPNAME like '%").append(this.selName).append("%'");
		}

		return sb_sql.toString();
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
