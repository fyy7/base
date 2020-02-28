package com.kind.base.blackip;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.kind.framework.cache.CacheManager;
import com.kind.framework.cache.redis.AbstractRedisClientManager;
import com.kind.framework.cache.redis.IRedisClient;
import com.kind.framework.core.Constant;
import com.kind.framework.core.DbHelper;

public class ReloadBackIpToRedis {

	/**
	 * 重载
	 */
	public static void reload() {

		DbHelper db = new DbHelper();

		AbstractRedisClientManager redisClientManager = CacheManager.getRedisClientManager();
		IRedisClient client = redisClientManager.getClient("default");

		String sql = "select black_ip  from SYS_BLACK_IP";
		List<Map<String, Object>> list = db.getSelectList(null, sql, new ArrayList<Object>());
		StringBuffer sb_ip = new StringBuffer(",");
		for (Map<String, Object> map : list) {
			sb_ip.append(map.get("BLACK_IP")).append(",");
		}

		client.set(Constant.FRAMEWORK_BLACK_IPS, sb_ip.toString());
	}

}
