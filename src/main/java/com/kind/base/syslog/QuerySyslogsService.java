package com.kind.base.syslog;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author zhengjiayun
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = true, action = "#query_syslogs", description = "系统日志列表查询", powerCode = "", requireTransaction = false)
public class QuerySyslogsService extends BaseActionService {

	// private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(getClass());

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		SelectSyslogsService sss = new SelectSyslogsService();
		return sss.handleData(hmLogSql, dc);

	}

	@Override
	public String pageAddress(DataContext dc) {
		return null;
	}
}
