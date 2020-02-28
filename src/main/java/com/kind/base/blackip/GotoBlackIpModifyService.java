package com.kind.base.blackip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * @author majiantao
 *
 *         2018年2月27日
 */
@Service
@Action(requireLogin = true, action = "#blackip_goto_modify", description = "IP黑名单修改页", powerCode = "", requireTransaction = false)
public class GotoBlackIpModifyService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String ip = dc.getRequestString("BLACK_IP");
		if (StrUtil.isNotBlank(ip)) {
			String sql = "SELECT * FROM SYS_BLACK_IP WHERE BLACK_IP=?";
			List<Object> param = new ArrayList<Object>();
			param.add(ip);
			Map<String, Object> bean = this.getSelectMap(hmLogSql, sql, param);
			dc.setBusiness("BEAN", bean);
		} else {
			dc.setBusiness("BEAN", null);
		}

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "sys/blackip/blackip_modify";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
