package com.kind.base.dic.holiday;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

@Service
@Action(requireLogin = true, action = "#sys_holiday_get", description = "获取节假日", powerCode = "dictionary.holiday", requireTransaction = false)

public class GetHolidayDataService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String s_year = dc.getRequestString("SEL_YEAR");
		List<Object> paramData = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("select * from SYS_HOLIDAY_DAY");

		if (StrUtil.isEmpty(s_year)) {
			this.setFailInfo(dc, "没有提供年份！");
			return 0;

		}
		sql.append(" where HOLIDAY_YEAR=?");
		paramData.add(s_year);
		JSONObject json = new JSONObject();

		json.put("flag", "0");

		List<Map<String, Object>> arr_bean = this.getSelectList(hmLogSql, sql.toString(), paramData);
		if (arr_bean != null) {
			SimpleDateFormat currDate_sdf = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy_M_d");

			for (Map<String, Object> o : arr_bean) {
				try {
					String value = "d_";
					if (o.get("HOLIDAY_DAY") != null)
						value += sdf.format(currDate_sdf.parse(o.get("HOLIDAY_DAY").toString()));
					json.put(value, o.get("REMARK") == null ? "" : o.get("REMARK"));
				} catch (ParseException e) {

				}
			}
			json.put("flag", "1");
		}
		dc.setBusiness("HOLIDAYDATA", json);

		return setSuccessInfo(dc, "执行成功");
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
