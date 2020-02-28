package com.kind.base.auth.service.sysicon;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author LIUHAORAN
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = true, action = "#sys_icon_main", description = "系统图标选择", powerCode = "", requireTransaction = false)
public class GotoSysiconMain2 extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		dc.setBusiness("SET_INPUT", dc.getRequestString("SET_INPUT"));
		return this.setSuccessInfo(dc);

	}

	@Override
	public String pageAddress(DataContext dc) {
		return "auth/sysicon/sys_icon_main";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
