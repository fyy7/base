package com.kind.base.dic.holiday;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.Pagination;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

@Service
@Action(requireLogin = true, action = "#sys_holiday_list", description = "节假日列表展示", powerCode = "dictionary.holiday", requireTransaction = false)
public class QueryHolidayListService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String s_year = dc.getRequestString("SEL_YEAR");
		List<Object> paramData = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("select HOLIDAY_YEAR from SYS_HOLIDAY_YEAR");
		String page = dc.getRequestString("page");
		String rows = dc.getRequestString("rows");
		if (!StrUtil.isEmpty(s_year)) {
			sql.append(" where HOLIDAY_YEAR=?");
			paramData.add(s_year);
		}
		sql.append(" order by HOLIDAY_YEAR desc");

		Pagination pdata = this.getPagination(hmLogSql, sql.toString(), Integer.parseInt(page), Integer.parseInt(rows), paramData);
		dc.setBusiness(ConCommon.PAGINATION_ROWS, pdata.getResultList());
		dc.setBusiness(ConCommon.PAGINATION_TOTAL, pdata.getTotalRows());

		return setSuccessInfo(dc, "执行成功");

	}

	@Override
	public String pageAddress(DataContext dc) {
		return null;
	}

	@Override
	public String verifyParameter(DataContext arg0) {

		return null;
	}

}
