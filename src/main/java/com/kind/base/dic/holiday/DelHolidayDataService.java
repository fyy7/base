package com.kind.base.dic.holiday;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

@Service
@Action(requireLogin = true, action = "#sys_holiday_del", description = "节假日删除", powerCode = "dictionary.holiday", requireTransaction = true)

public class DelHolidayDataService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String s_year = dc.getRequestString("SEL_YEAR");

		if (StrUtil.isEmpty(s_year)) {
			this.setFailInfo(dc, "没有找到年份！");
		}

		DataBean bean = new DataBean("SYS_HOLIDAY_YEAR", "HOLIDAY_YEAR");
		bean.set("HOLIDAY_YEAR", s_year);

		if (this.delete(hmLogSql, bean) == 0) {
			this.setFailInfo(dc, "删除数据失败！");
			return 0;
		}

		bean.setTableName("SYS_HOLIDAY_DAY");
		if (this.delete(hmLogSql, bean) == 0) {
			this.setFailInfo(dc, "删除数据失败！");
			return 0;
		}

		this.setSuccessInfo(dc, "删除成功");
		return 1;
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
