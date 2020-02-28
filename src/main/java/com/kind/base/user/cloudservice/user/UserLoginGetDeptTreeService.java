package com.kind.base.user.cloudservice.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.auth.SubjectAuthInfo;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author chenzhiwei
 *
 *         2018年4月8日
 */
@Service
@Action(requireLogin = false, action = "#user_login_get_dept_list", description = "获取部门结构列表", powerCode = "", requireTransaction = false)
public class UserLoginGetDeptTreeService extends BaseActionService {
	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// TODO 自动生成的方法存根
		String opAccount = dc.getRequestString("OPACCOUNT");
		String organ_id = dc.getRequestString("ORGANID");
		StringBuffer sb_sql = new StringBuffer("SELECT A.DEPTNAME,A.DEPTID,A.DLEVEL FROM SYS_DEPARTMENT_INFO A WHERE A.ISDEL=0 AND A.ORGANID='").append(organ_id).append("'");
		SubjectAuthInfo sa = getSubjectAuthInfo(dc);
		List<Object> paramList = new ArrayList<Object>();
		if (!sa.isSupperUser()) {
			sb_sql.append(" AND (EXISTS(SELECT 1 FROM SYS_N_USER_DEPT_INFO b,SYS_N_USERS c  WHERE A.ORGANID=b.ORGAN_ID and b.DEPT_ID=A.DEPTID");
			paramList.add(opAccount);
			sb_sql.append(" AND c.OPNO=b.OPNO AND c.OPACCOUNT=? AND c.ISDEL=0)");
			paramList.add(opAccount);
			sb_sql.append(" or exists(select 1 from SYS_ORGANIZATION_INFO b where b.ISDEL=0 AND b.ORGANID=A.ORGANID AND ").append(ConvertSqlDefault.getStrJoinSQL("b.ACCOUNT_PERFIX", "'admin'")).append("=?)"); // 某机构的管理员
			sb_sql.append(")");

			StringBuffer sb_sql1 = new StringBuffer("SELECT b.DEPT_ID FROM SYS_N_USER_DEPT_INFO b,SYS_N_USERS c  WHERE b.ORGAN_ID=?");
			sb_sql1.append(" AND c.OPNO=b.OPNO AND b.MAIN_DEPT_FLAG=1 and c.OPACCOUNT=? AND C.ISDEL=0");
			List<Map<String, Object>> main_dept_id = this.getSelectList(hmLogSql, sb_sql1.toString(), Arrays.asList(organ_id, opAccount));

			// Object main_org_id = DBHelper.selectOneFieldValueBySQL(this.operateLog, ac.getConnection(), sb_sql1.toString(), new Object[] { opAccount });
			if (main_dept_id == null) {

			}
			dc.setBusiness("MAIN_DEPT_ID", main_dept_id);
		}
		sb_sql.append(" order by ALLORDIDX");
		List<Map<String, Object>> arr_resourceId = getSelectList(hmLogSql, sb_sql.toString(), paramList);
		dc.setBusiness("DEPT_TREE", JSON.toJSON(arr_resourceId));
		return this.setSuccessInfo(dc, "获取成功！");
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
