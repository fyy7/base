package com.kind.base.auth.service.deptofficerole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author LIUHAORAN
 *
 *         2018年3月13日
 */
@Service
@Action(requireLogin = true, action = "#organdmb_option", description = "部门类型与职务类型可选项查询", powerCode = "", requireTransaction = false)
public class QueryOrganDmbOption extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String organid = dc.getRequestString("ORGAN_ID");

		List<Object> param = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("SELECT DM,DMNR,DMLX FROM ORGAN_DMB WHERE ORGAN_ID=? ORDER BY DM");
		param.add(organid);
		List<Map<String, Object>> list = this.getSelectList(hmLogSql, sql.toString(), param);
		dc.setBusiness("ORGAN_DMB_LIST", list);

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
