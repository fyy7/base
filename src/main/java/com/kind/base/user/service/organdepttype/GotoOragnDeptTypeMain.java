package com.kind.base.user.service.organdepttype;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author chenzhiwei
 *
 *         2018年2月28日
 */
@Service
@Action(requireLogin = true, action = "#organ_dept_type_main", description = "机构部门类型管理", powerCode = "", requireTransaction = false)
public class GotoOragnDeptTypeMain extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		dc.setBusiness("FLAG", "DEPT.TYPE");
		/*
		 * String cmd = dc.getRequestString("CMD"); String flag = dc.getRequestString("FLAG"); if ("U".equals(cmd)) { String organ_id = dc.getRequestString("ORGAN_ID"); String dm = dc.getRequestString("DM"); String sql = "SELECT * FROM SYS_DMB WHERE DMLX=? AND DM=? AND ORGAN_ID=?"; List<Object> param = new ArrayList<Object>(); param.add(flag); param.add(dm); param.add(organ_id); dc.setBusiness("SYS_DMB_INFO",this.getSelectMap(hmLogSql, sql, param)); } dc.setBusiness("CMD", cmd); dc.setBusiness("FLAG", flag);
		 */
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "user/sysdmb/organdeptoffice/organ_dmb_dep_main";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
