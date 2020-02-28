package com.kind.base.dic.service.sysdmb;

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
@Action(requireLogin = true, action = "#sysdmb_option", description = "代码类型选择项查询", powerCode = "userpower.depttype", requireTransaction = false)
public class QuerySysDmbOption extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		List<Object> param = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("SELECT DM,DMNR FROM SYS_DMB WHERE DMLX='DMLX'");
		List<Map<String, Object>> list = this.getSelectList(hmLogSql, sql.toString(), param);
		dc.setBusiness("SYS_DMB_LIST", list);

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
