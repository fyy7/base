package com.kind.base.user.service.deptmanager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author chenzhiwei
 *
 *         2018年4月9日
 */
@Service
@Action(requireLogin = true, action = "#user_order_edit", description = "用户排序编辑", powerCode = "", requireTransaction = false)
public class UserOrderService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// TODO 自动生成的方法存根
		String deptId = dc.getRequestString("DEPTID");
		String pId = dc.getRequestString("PID");
		String organid = dc.getRequestString("ORGANID");

		StringBuffer sql = new StringBuffer();
		sql.append("select DEPTID,DEPTNAME,DLEVEL  from SYS_DEPARTMENT_INFO where ISDEL=0 AND DEPTID=?");
		List<Map<String, Object>> list = getSelectList(hmLogSql, sql.toString(), Arrays.asList(deptId));

		StringBuffer sb_sql = new StringBuffer();
		sb_sql.append("SELECT A.OPNO,A.OPACCOUNT,A.OPNAME,B.ORDIDX,C.DEPTNAME,C.DEPTID DEPT_ID FROM SYS_N_USERS A ,SYS_N_USER_DEPT_INFO B,SYS_DEPARTMENT_INFO C ,SYS_ORGANIZATION_INFO D where A.OPNO = B.OPNO and B.DEPT_ID = C.DEPTID and A.ISDEL = '0' and B.ORGAN_ID = D.ORGANID");
		sb_sql.append(" and B.ORGAN_ID = ? and B.DEPT_ID =?");
		sb_sql.append(" order by B.ORDIDX");
		List<Map<String, Object>> type_list = getSelectList(hmLogSql, sb_sql.toString(), Arrays.asList(organid, deptId));

		dc.setBusiness("DEPT_TYPE_LIST", type_list);
		dc.setBusiness("LIST", list);
		dc.setBusiness("DEPTID", deptId);
		dc.setBusiness("PARENTID", pId);
		dc.setBusiness("ORGANID", organid);
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext arg0) {
		// TODO 自动生成的方法存根
		return "user/userorder/user_order_edit";
	}

	@Override
	public String verifyParameter(DataContext arg0) {
		// TODO 自动生成的方法存根
		return null;
	}
}
