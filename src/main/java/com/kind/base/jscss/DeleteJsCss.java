package com.kind.base.jscss;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.common.tag.JsCssPath;
import com.kind.framework.cache.CacheManager;
import com.kind.framework.cache.redis.AbstractRedisClientManager;
import com.kind.framework.cache.redis.IRedisClient;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

@Service
@Action(requireLogin = true, action = "#delete_js_css", description = "JS/CSS文件MD5值管理--删除", powerCode = "monitor.jscss", requireTransaction = false)
public class DeleteJsCss extends BaseActionService {
	// private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(DeleteJsCss.class);

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String filename = dc.getRequestString("FILENAME");
		String flag = dc.getRequestString("flag");

		AbstractRedisClientManager redisClientManager = CacheManager.getRedisClientManager();
		IRedisClient client = redisClientManager.getClient("default");

		if ("1".equals(flag)) {
			// 删除所有
			client.remove(JsCssPath.JSCSS_REDIS_KEY);
		} else {
			client.hDel(JsCssPath.JSCSS_REDIS_KEY, filename);
		}

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
