package com.kind.base.tablemetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.kind.framework.cache.redis.IRedisClient;
import com.kind.framework.cache.redis.KryoRedisSerializer;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.SqlTableMetaDataBean;
import com.kind.framework.service.BaseActionService;

/**
 * 表结构缓存列表查询
 * 
 * @author yanhang
 *
 */
@Service
@Action(requireLogin = true, action = "#get_table_metadata_list", description = "表结构缓存列表查询", powerCode = "monitor.table.metadata", requireTransaction = false)
public class GetTableMetadataListService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(GetTableMetadataListService.class);

	private KryoRedisSerializer<SqlTableMetaDataBean> serializer = new KryoRedisSerializer<SqlTableMetaDataBean>();

	private String SERVICE_NAME = "表结构缓存列表查询";

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		try {
			String appId = dc.getRequestObject("APP_ID", "").toUpperCase();
			String tableName = dc.getRequestObject("TABLE_NAME", "").toUpperCase();

			IRedisClient client = this.getRedisClient();
			String redisKeyPatten = String.format("__TBL_ROWSET_METADATA__:*%s*:DATASOURCENUM_*:*%s*", appId, tableName);
			Set<String> keys = client.keys(redisKeyPatten);

			// List<Object> values = client.get(keys.toArray(new String[keys.size()]));
			List<Map<String, String>> result = new ArrayList<Map<String, String>>();
			for (String key : keys) {
				SqlTableMetaDataBean metaBean = serializer.deserialize((byte[]) client.get(key));

				String[] keyInfo = key.split(":");

				Map<String, String> map = new HashMap<String, String>();
				map.put("APP_ID", keyInfo[1]);
				map.put("TABLE_NAME", metaBean.getTableName());
				map.put("REDIS_KEY", key);
				map.put("DATASOURCE", keyInfo[2]);
				map.put("FIELDS", getFieldsInfo(metaBean));

				result.add(map);
			}

			dc.setBusiness("rows", result);
			dc.setBusiness("total", result.size());
		} catch (Exception e) {
			String msg = String.format("[%s]异常！", SERVICE_NAME);
			log.info(msg + System.getProperty("line.separator") + getStackTrace(e));

			return setFailInfo(dc, msg);
		}

		return setSuccessInfo(dc);
	}

	private String getFieldsInfo(SqlTableMetaDataBean metaBean) {
		StringBuffer sb = new StringBuffer();
		String[] columnNames = metaBean.getColumnNames();
		String[] columnTypeNames = metaBean.getColumnTypeNames();
		Integer[] columnLengthArray = metaBean.getColumnLengthArray();
		for (int i = 0; i < columnNames.length; i++) {
			sb.append("[").append(columnNames[i]).append(",").append(columnTypeNames[i]).append(",").append(columnLengthArray[i]).append("] \r\n");
		}

		return sb.toString();
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