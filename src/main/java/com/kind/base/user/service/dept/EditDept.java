package com.kind.base.user.service.dept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年2月23日
 */
@Service
@Action(requireLogin = true, action = "#dept_edit", description = "进入编辑部门页面", powerCode = "userpower.branch", requireTransaction = false)
public class EditDept extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String deptId = dc.getRequestString("DEPTID");
		String pId = dc.getRequestString("PID");
		String organid = dc.getRequestString("ORGANID");

		List<Object> paramList = new ArrayList<Object>();
		StringBuffer sbSql = new StringBuffer();
		sbSql.append("select * FROM (SELECT DEPTID,DEPTNAME,DLEVEL,ALLORDIDX AS temp_ORDER,").append(ConvertSqlDefault.getBlankString("ALLORDIDX", ".")).append(" ALLORDIDX FROM SYS_DEPARTMENT_INFO WHERE ISDEL=0 AND ORGANID=?");
		paramList.add(organid);
		if (!StrUtil.isEmpty(deptId)) {
			sbSql.append(" AND ALLORDIDX NOT LIKE (SELECT ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" FROM SYS_DEPARTMENT_INFO WHERE ISDEL=0 AND DEPTID=?)");
			paramList.add(deptId);
		}
		sbSql.append(" UNION SELECT ORGANID ,NAME ,NLEVEL,'0' AS temp_ORDER,'--' ALLORDIDX FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND ORGANID=?) AAA ORDER BY temp_ORDER");
		paramList.add(organid);
		// 获取上级部门列表
		List<Map<String, Object>> superList = this.getSelectList(hmLogSql, sbSql.toString(), paramList);
		dc.setBusiness("SUPER_LIST", superList);

		if (!StrUtil.isEmpty(deptId)) {
			paramList.clear();
			paramList.add(deptId);
			String selectOrgan = "SELECT A.*, (SELECT OPNAME FROM  SYS_N_USERS B WHERE A.LEADER_USERID=OPNO) AS LEADER_USERNAME,(SELECT OPNAME FROM  SYS_N_USERS B WHERE A.HEAD_USERID=OPNO) AS HEAD_USERNAME,(SELECT DEPTNAME FROM SYS_DEPARTMENT_INFO  WHERE A.LEADER_DEPTID=DEPTID) AS LEADER_DEPTNAME,(SELECT DEPTNAME FROM SYS_DEPARTMENT_INFO  WHERE A.HEAD_DEPTID=DEPTID) AS HEAD_DEPTNAME FROM SYS_DEPARTMENT_INFO A   where A.ISDEL=0 AND A.DEPTID = ?";
			List<Map<String, Object>> organBean = this.getSelectList(hmLogSql, selectOrgan.toString(), paramList);
			if (organBean.size() > 0) {
				dc.setBusiness("BEAN", organBean.get(0));
			}
			dc.setBusiness("CMD", "U");

			sbSql = new StringBuffer("select * from (SELECT BVALUE,BTITLE,1 BSELECT FROM SYS_DEPARTMENT_TYPE WHERE DEPTID=? AND BVALUE!=0 UNION ALL SELECT DM,DMNR,0 FROM ORGAN_DMB WHERE ORGAN_ID=? AND DMLX='DEPT.TYPE' AND NOT EXISTS(SELECT 1 FROM SYS_DEPARTMENT_TYPE WHERE DEPTID=? AND BVALUE=ORGAN_DMB.DM)) AAA ORDER BY " + ConvertSqlDefault.toNumberSQL("BVALUE") + "");
			paramList.clear();
			paramList.add(deptId);
			paramList.add(organid);
			paramList.add(deptId);
			List<Map<String, Object>> typeList = this.getSelectList(hmLogSql, sbSql.toString(), paramList);
			dc.setBusiness("DEPT_TYPE_LIST", typeList);
			if (superList != null && typeList.size() >= 2) {
				dc.setBusiness("TYPE", 1);
			} else {
				dc.setBusiness("TYPE", 0);
			}
		} else {
			dc.setBusiness("CMD", "A");
			paramList.clear();
			paramList.add(organid);
			List<Map<String, Object>> typeList = this.getSelectList(hmLogSql, "SELECT DM BVALUE,DMNR BTITLE,0 BSELECT  FROM ORGAN_DMB WHERE ORGAN_ID=? AND DMLX='DEPT.TYPE' ORDER BY DM", paramList);
			dc.setBusiness("DEPT_TYPE_LIST", typeList);
			if (superList != null && typeList.size() >= 2) {
				dc.setBusiness("TYPE", 1);
			} else {
				dc.setBusiness("TYPE", 0);
			}
		}

		dc.setBusiness("DEPTID", deptId);
		dc.setBusiness("PARENTID", pId);
		dc.setBusiness("ORGANID", organid);

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "user/dept/dept_edit";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
