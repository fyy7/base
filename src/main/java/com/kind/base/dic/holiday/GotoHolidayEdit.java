package com.kind.base.dic.holiday;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author LIUHAORAN
 *
 *         2018年2月27日
 */
@Service
@Action(requireLogin = true, action = "#sys_holiday_edit", description = "进入节假日管理", powerCode = "dictionary.holiday", requireTransaction = false)
public class GotoHolidayEdit extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		dc.setBusiness("YEAR", dc.getRequestString("YEAR"));
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "dic/holiday/holiday_edit";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
