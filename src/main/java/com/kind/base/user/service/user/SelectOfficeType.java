package com.kind.base.user.service.user;

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
@Action(requireLogin = true, action = "#select_office_type", description = "职务选择", powerCode = "", requireTransaction = false)
public class SelectOfficeType extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		dc.setBusiness("NUM", dc.getRequestString("NUM"));
		String organId = dc.getRequestString("ORGAN_ID");

		List<Object> paramList = new ArrayList<Object>();
		StringBuffer sbInfoIds = new StringBuffer("");
		StringBuffer sbInfoNames = new StringBuffer("");
		String selectDmSql = "SELECT DM,DMNR FROM ORGAN_DMB WHERE ORGAN_ID=? AND DMLX='OFFICE.TYPE' ORDER BY DM";
		paramList.add(organId);
		List<Map<String, Object>> officeTypeArrDm = this.getSelectList(hmLogSql, selectDmSql, paramList);
		if (officeTypeArrDm != null & officeTypeArrDm.size() > 0) {
			for (Map<String, Object> d : officeTypeArrDm) {
				sbInfoIds.append(",").append(String.valueOf(d.get("DM")));
				sbInfoNames.append(",").append(String.valueOf(d.get("DMNR")));
			}
			dc.setBusiness("OFFICE_TYPE_IDS", sbInfoIds.substring(1));
			dc.setBusiness("OFFICE_TYPE_NAMES", sbInfoNames.substring(1));
		}
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "user/user/select_office_type";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
