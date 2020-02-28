package com.kind.base.jscss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.tag.JsCssPath;
import com.kind.framework.cache.CacheManager;
import com.kind.framework.cache.redis.AbstractRedisClientManager;
import com.kind.framework.cache.redis.IRedisClient;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

@Service
@Action(requireLogin = true, action = "#query_js_css", description = "JS/CSS文件MD5值管理--查询", powerCode = "monitor.jscss", requireTransaction = false)
public class QueryJsCss extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(QueryJsCss.class);

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		AbstractRedisClientManager redisClientManager = CacheManager.getRedisClientManager();
		IRedisClient client = redisClientManager.getClient("default");
		Set<Object> list = client.hGetAllKeys(JsCssPath.JSCSS_REDIS_KEY);

		String filename = dc.getRequestString("FILENAME");

		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for (Object o : list) {
			if (StrUtil.isNotBlank(filename)) {
				if (String.valueOf(o).indexOf(filename) > -1) {
					Map<String, Object> m = new HashMap<String, Object>();
					m.put("FILENAME", o);
					m.put("MD5", client.hGet(JsCssPath.JSCSS_REDIS_KEY, o));
					result.add(m);
				}
				continue;
			}

			log.debug("-------------" + String.valueOf(o));
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("FILENAME", o);
			m.put("MD5", client.hGet(JsCssPath.JSCSS_REDIS_KEY, o));
			result.add(m);
		}

		dc.setBusiness(ConCommon.PAGINATION_ROWS, result);
		dc.setBusiness(ConCommon.PAGINATION_TOTAL, result.size());

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
