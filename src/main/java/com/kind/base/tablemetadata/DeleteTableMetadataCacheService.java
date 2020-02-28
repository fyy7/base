package com.kind.base.tablemetadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.kind.framework.cache.redis.IRedisClient;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * 清除表结构缓存
 * 
 * @author yanhang
 *
 */
@Service
@Action(requireLogin = true, action = "#delete_table_metadata_cache", description = "清除表结构缓存", powerCode = "monitor.table.metadata", requireTransaction = false)
public class DeleteTableMetadataCacheService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(DeleteTableMetadataCacheService.class);

	private String SERVICE_NAME = "清除表结构缓存";

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		try {
			String redisKey = dc.getRequestObject("REIDS_KEY", "").toUpperCase();

			IRedisClient client = this.getRedisClient();
			Set<String> keys = new HashSet<String>();
			if (StrUtil.isBlank(redisKey)) { // 为空则删除所有
				keys = client.keys("__TBL_ROWSET_METADATA__:*");
			} else {
				keys.add(redisKey);
			}

			client.remove(keys.toArray(new String[keys.size()]));
		} catch (Exception e) {
			String msg = String.format("[%s]异常！", SERVICE_NAME);
			log.info(msg + System.getProperty("line.separator") + getStackTrace(e));

			return setFailInfo(dc, msg);
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