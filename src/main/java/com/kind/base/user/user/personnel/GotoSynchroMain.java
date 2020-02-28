package com.kind.base.user.user.personnel;

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
@Action(requireLogin = true, action = "#goto_personnel_synchro_main", description = "部门管理", powerCode = "userpower.personnel.synchro", requireTransaction = false)
public class GotoSynchroMain extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		return this.setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "sys/personnel/synchro_main";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
