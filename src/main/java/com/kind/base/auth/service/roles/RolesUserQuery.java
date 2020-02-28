/**
 * @Title: GotoResourceMain.java 
 * @Package com.kind.portal.service.resource 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月5日 下午4:39:00 
 * @version V1.0   
 */
package com.kind.base.auth.service.roles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
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
 * @author HUANGLEI
 *
 */
@Service
@Action(requireLogin = true, action = "#sys_role_get_role_list", description = "角色授权情况查询", powerCode = "resource.role", requireTransaction = false)

public class RolesUserQuery extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		String organ_id = dc.getRequestString("ORGANID");
		String roleid = dc.getRequestString("ROLEID");
		String dept_id = dc.getRequestString("DEPT_ID");
		// 1用户列表，2为部门列表
		String data_flag = dc.getRequestString("DATA_FLAG");
		// 姓名、部门 名称
		String sel_name = dc.getRequestString("SEL_NAME");
		// 1已分配，0未分配
		String role_type = dc.getRequestString("ROLE_TYPE");

		String p_type = dc.getRequestString("P_TYPE");

		String role_str = "未授权";

		if ("1".equals(role_type)) {
			role_str = "已授权";
		}

		StringBuffer sb_sql = new StringBuffer("");

		if ("2".equals(data_flag)) {
			// 部门列表
			sb_sql.append("select '").append(role_str).append("' ROLE_TYPE,A.*,B.NAME ORGANNAME from SYS_DEPARTMENT_INFO A,SYS_ORGANIZATION_INFO B where B.ORGANID=A.ORGANID AND B.isdel!='1' AND A.isdel!='1' AND A.ORGANID='").append(organ_id).append("'");

			if (!StrUtil.isEmpty(dept_id)) {
				sb_sql.append(" and A.ALLORDIDX like (select ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" from SYS_DEPARTMENT_INFO where DEPTID='").append(dept_id).append("')");
			} else {
				if (!(userpo.getOpType() < 2)) {
					sb_sql.append(" and A.ALLORDIDX like (select ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" from SYS_DEPARTMENT_INFO where DEPTID='").append(userpo.getDeptId()).append("')");
				}
			}

			if (!(userpo.getOpType() < 2)) {
				// 不是admin的，才需要限制,不能对自个部门进行赋值
				sb_sql.append(" and A.DEPTID!='").append(userpo.getDeptId()).append("'");

			}

			if (!StrUtil.isEmpty(sel_name)) {
				sb_sql.append(" and A.DEPTNAME like '%").append(sel_name).append("%'");
			}
			sb_sql.append(" and ");
			if (!"1".equals(role_type)) {
				// 未分配
				sb_sql.append(" not");
			}
			sb_sql.append(" exists(select 1 from SYS_N_ROLE_DEPARTMENT where DEPT_ID=A.DEPTID and ROLEID='").append(roleid).append("')");
		} else {
			// 人员列表
			if ("1".equals(p_type) && (userpo.getOpType() < 2)) {
				// 机构管理员的
				sb_sql.append("select '").append(role_str).append("' ROLE_TYPE, A.*,'ORGAN_ID' ORGAN_ID, 'DEPT_ID' DEPT_ID,'' DEPTNAME,D.NAME ORGANNAME from SYS_N_USERS A,SYS_ORGANIZATION_INFO D where  D.ORGANID='").append(organ_id).append("'");
				sb_sql.append(" and ").append(ConvertSqlDefault.getStrJoinSQL("D.ACCOUNT_PERFIX", "'" + (StrUtil.isEmpty(SpringUtil.getEnvProperty("app.user.super.account")) ? "admin" : SpringUtil.getEnvProperty("app.user.super.account")) + "'")).append("=A.OPACCOUNT");
				if (!StrUtil.isEmpty(sel_name)) {
					sb_sql.append(" and A.OPNAME like '%").append(sel_name).append("%'");
				}

				// 过滤掉自个的
				sb_sql.append(" and A.OPACCOUNT!='").append(userpo.getOpAccount()).append("'");

				sb_sql.append(" and ");
				if (!"1".equals(role_type)) {
					// 未分配
					sb_sql.append(" not");
				}
				sb_sql.append(" exists(select 1 from SYS_N_ROLEUSER where DEPT_ID='DEPT_ID' AND OPNO=A.OPNO and ROLEID='").append(roleid).append("')");

			} else {
				sb_sql.append("select '").append(role_str).append("' ROLE_TYPE, A.*,B.ORGAN_ID,B.DEPT_ID,C.DEPTNAME,D.NAME ORGANNAME from SYS_N_USERS A,SYS_N_USER_DEPT_INFO B,SYS_DEPARTMENT_INFO C,SYS_ORGANIZATION_INFO D where  D.isdel!='1' AND C.isdel!='1' AND A.isdel!='1' AND B.ORGAN_ID=D.ORGANID AND C.DEPTID=B.DEPT_ID AND A.OPNO=B.OPNO AND B.ORGAN_ID='").append(organ_id).append("'");

				if (!StrUtil.isEmpty(dept_id)) {
					sb_sql.append(" and C.ALLORDIDX like (select ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" from SYS_DEPARTMENT_INFO where DEPTID='").append(dept_id).append("')");
				} else {
					// 自个部门下面的
					if (!(userpo.getOpType() < 2)) {
						sb_sql.append(" and C.ALLORDIDX like (select ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" from SYS_DEPARTMENT_INFO where DEPTID='").append(userpo.getDeptId()).append("')");
					}
				}

				// 不是机构管理员
				if (!(userpo.getOpType() < 2)) {
					// 不能对自个进行赋值
					sb_sql.append(" and A.OPNO!='").append(userpo.getOpNo()).append("'");

					// 不能对别人对自已有赋权限的，再对他进行赋权限，造成循环
					sb_sql.append(" and not exists(select 1 from SYS_N_ROLEUSER s where s.DEPT_ID=C.DEPTID and A.OPNO=s.OPNO and s.OPNO='").append(userpo.getOpNo()).append("')");
					sb_sql.append(" and not exists(select 1 from SYS_N_USERRIGHTS s where s.DEPT_ID=C.DEPTID and A.OPNO=s.OPNO and s.OPNO='").append(userpo.getOpNo()).append("')");
				}

				if (!StrUtil.isEmpty(sel_name)) {
					sb_sql.append(" and A.OPNAME like '%").append(sel_name).append("%'");
				}
				sb_sql.append(" and ");
				if (!"1".equals(role_type)) {
					// 未分配
					sb_sql.append(" not");
				}
				sb_sql.append(" exists(select 1 from SYS_N_ROLEUSER where DEPT_ID=B.DEPT_ID AND OPNO=A.OPNO and ROLEID='").append(roleid).append("')");
			}
		}
		List<Object> paramData = new ArrayList<Object>();
		String page = dc.getRequestString("page");
		String rows = dc.getRequestString("rows");
		Pagination pbean = getPagination(hmLogSql, sb_sql.toString(), Integer.parseInt(page), Integer.parseInt(rows), paramData);
		dc.setBusiness("rows", pbean.getResultList() == null ? new JSONArray() : pbean.getResultList());
		dc.setBusiness("total", pbean.getTotalRows());
		dc.setBusiness("pagenum", pbean.getTotalPages());
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext arg0) {
		return null;
	}

	@Override
	public String verifyParameter(DataContext arg0) {
		return null;
	}

}
