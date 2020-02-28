package com.kind.base.dic.holiday;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

@Service
@Action(requireLogin = true, action = "#sys_holiday_save", description = "节假日保存", powerCode = "dictionary.holiday", requireTransaction = true)

public class SaveHolidayDataService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String s_year = dc.getRequestString("SEL_YEAR");
		String holiday_data = dc.getRequestString("HOLIDAY_DATA");

		if (StrUtil.isEmpty(s_year)) {
			this.setFailInfo(dc, "没有找到年份！");
		}

		DataBean bean = new DataBean("SYS_HOLIDAY_YEAR", "HOLIDAY_YEAR");
		bean.set("HOLIDAY_YEAR", s_year);

		if (this.delete(hmLogSql, bean) == 0) {
			this.setFailInfo(dc, "写入数据失败！");
			return 0;
		}

		if (this.insert(hmLogSql, bean) == 0) {
			this.setFailInfo(dc, "写入数据失败！");
			return 0;
		}

		bean = new DataBean("SYS_HOLIDAY_DAY", "HOLIDAY_YEAR");
		bean.set("HOLIDAY_YEAR", s_year);
		if (this.delete(hmLogSql, bean) == 0) {
			this.setFailInfo(dc, "删除数据失败！");
			return 0;
		}
		JSONObject json = JSONObject.parseObject(holiday_data);
		int i_num = json.getInteger("num");
		SimpleDateFormat currDate_sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_M_d");
		for (int i = 0; i < i_num; i++) {

			try {

				bean.set("HOLIDAY_DAY", sdf.parse(json.getJSONObject("d_" + i).getString("value")));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bean.set("REMARK", json.getJSONObject("d_" + i).getString("remark"));
			if (this.insert(hmLogSql, bean) == 0) {
				this.setFailInfo(dc, "写入数据失败！");
				return 0;
			}
		}

		this.setSuccessInfo(dc, "执行成功");
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
