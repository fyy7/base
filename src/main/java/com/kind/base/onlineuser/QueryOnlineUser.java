package com.kind.base.onlineuser;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.OnlineUserUtil;

@Service
@Action(requireLogin = true, action = "#query_online_user", description = "在线用户查询", powerCode = "monitor.online.user", requireTransaction = false)
public class QueryOnlineUser extends BaseActionService {
	// private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(QueryOnlineUser.class);

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		List<JSONObject> _list = OnlineUserUtil.getOnlineUserList();
		dc.setBusiness("onlineUser", _list);

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
