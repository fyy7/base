/**
 * @Title: ResourcesResetRedios.java 
 * @Package com.kind.auth.cloudservice.auth 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年5月21日 下午4:55:59 
 * @version V1.0   
 */
package com.kind.base.auth.cloudservice.auth;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kind.framework.cache.CacheManager;
import com.kind.framework.cache.redis.AbstractRedisClientManager;
import com.kind.framework.cache.redis.IRedisClient;
import com.kind.framework.cache.redis.KryoRedisSerializer;
import com.kind.framework.core.DbHelper;

import cn.hutool.core.util.ObjectUtil;

/**
 * @author huanglei
 *
 */
public class ResourcesResetRedis extends DbHelper {

	/**
	 * 重设Redis中的资源缓存
	 * 
	 * @param appid
	 *            应用IP
	 */
	private String REDIOS_KEY_NAME = "menuList:";
	private AbstractRedisClientManager redisClientManager;
	private IRedisClient client;
	private KryoRedisSerializer<List<Map<String, Object>>> serializer = new KryoRedisSerializer<List<Map<String, Object>>>();

	public String getPermissionKey(String appid) {
		return REDIOS_KEY_NAME + appid;
	}

	public void init() {
		if (ObjectUtil.isNull(redisClientManager)) {
			redisClientManager = CacheManager.getRedisClientManager();
		}
		if (ObjectUtil.isNull(client)) {
			client = redisClientManager.getClient("default");
		}
	}

	/**
	 * 获取资源内容
	 * 
	 * @param hmLogSql
	 * @param appid
	 * @return
	 */
	public List<Map<String, Object>> getRedis(HashMap<String, String> hmLogSql, String appid) {
		init();
		List<Map<String, Object>> reoust;
		Object cache = client.get(getPermissionKey(appid));
		if (cache != null) {
			reoust = serializer.deserialize((byte[]) cache);
		} else {
			// List<Object> paramList = new ArrayList<Object>();
			String querySql = "SELECT '1' TYPE,EXTITEM1,EXTITEM3,ISGROUP,RLEVEL,RID,PARENTID,TITLE,RLOGO,ALLORDIDX,0 BVALUE,APP_ID FROM SYS_RESOURCES A WHERE VISIBLE=1 AND CHANNEL_RTYPE='00001' AND APP_ID=?  order by ALLORDIDX";

			reoust = getSelectList(hmLogSql, querySql, Arrays.asList(appid));
			client.set(getPermissionKey(appid), serializer.serialize(reoust));
		}
		return reoust;

	}

	/**
	 * 重置资源内容
	 * 
	 * @param hmLogSql
	 * @param appid
	 */
	public void resetRedis(HashMap<String, String> hmLogSql, String appid) {
		init();
		String querySql = "SELECT '1' TYPE,EXTITEM1,EXTITEM3,ISGROUP,RLEVEL,RID,PARENTID,TITLE,RLOGO,ALLORDIDX,0 BVALUE,APP_ID FROM SYS_RESOURCES A WHERE VISIBLE=1 AND CHANNEL_RTYPE='00001' AND APP_ID=?  order by ALLORDIDX";
		List<Map<String, Object>> reoust = getSelectList(hmLogSql, querySql, Arrays.asList(appid));
		client.set(REDIOS_KEY_NAME + appid, serializer.serialize(reoust));
	}
}
