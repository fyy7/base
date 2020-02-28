package com.kind.base.blackip;

import java.util.Arrays;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 *
 */
@Service
@Action(requireLogin = true, action = "#blackip_delete", description = "IP黑名单删除", powerCode = "blackip_delete", requireTransaction = true)
public class DeleteBlackIpService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String black_ip = dc.getRequestString("BLACK_IP");

		int result = this.executeSql(hmLogSql, "DELETE FROM  SYS_BLACK_IP WHERE black_ip=?", Arrays.asList(black_ip));
		if (result != 1) {
			return setFailInfo(dc, "删除失败！");
		} else {
			// 重载黑名单IP相关信息
			ReloadBackIpToRedis.reload();
			return setSuccessInfo(dc, "删除成功！");
		}
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
