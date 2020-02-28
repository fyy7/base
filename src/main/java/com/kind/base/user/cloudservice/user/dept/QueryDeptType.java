package com.kind.base.user.cloudservice.user.dept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = true, action = "#dept_type_query", description = "获取部门类型", powerCode = "", requireTransaction = false)
public class QueryDeptType extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String organId = dc.getRequestString("ORGAN_ID");
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(organId);

		/* 查询部门类型数据 */
		List<Map<String, Object>> typeList = this.getSelectList(hmLogSql, "SELECT DM BVALUE,DMNR BTITLE,0 BSELECT  FROM ORGAN_DMB WHERE ORGAN_ID=? AND DMLX='DEPT.TYPE' ORDER BY DM", paramList);

		dc.setBusiness("DEPT_TYPE_LIST", typeList);

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
