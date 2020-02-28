package com.kind.base.user.service.user;

import java.util.HashMap;

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
@Action(requireLogin = true, action = "#user_main", description = "用户管理", powerCode = "register.user", requireTransaction = false)
public class GotoUserManageMain extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "user/user/user_main";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
