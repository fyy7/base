package com.kind.base.auth.service.auth;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.framework.auth.ResourceInfo;
import com.kind.framework.auth.SubjectAuthInfo;
import com.kind.framework.cache.CacheManager;
import com.kind.framework.cache.redis.AbstractRedisClientManager;
import com.kind.framework.cache.redis.IRedisClient;
import com.kind.framework.cache.redis.KryoRedisSerializer;
import com.kind.framework.core.Action;
import com.kind.framework.core.DbHelper;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

/**
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "goto_sys_permission_admin_page", description = "test", powerCode = "", requireTransaction = false)
public class GotoSysPermissionAdminPageService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(GotoSysPermissionAdminPageService.class);

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		Map<String, Object> values = new HashMap<String, Object>();
		values.put("C_BIGINT", 11);
		values.put("C_INT", "3");
		values.put("C_DOUBLE", 11.22);
		values.put("C_FLOAT", 22.333);
		values.put("C_BIGDECIMAL", 99.6923);
		values.put("C_CHAR", "AAAA");
		values.put("C_VARCHAR", "varD哈");
		values.put("C_LONGVARCHAR", "KFF麦冬");
		values.put("C_NVARCHAR", "G你好");
		values.put("C_CLOB", "clobHHHHH品牌");
		values.put("C_DATE", new Date());
		values.put("C_TIMESTAMP", new Date());

		DataBean db = new DataBean("TEST_TEST", "", values);
		int i = insert(hmLogSql, db);

		if (i <= 0) {
			return i;
		}

		// Map<String, Object> values = new HashMap<String, Object>();
		// values.put("UID", "uid0001");
		// values.put("APP_ID", "app0001");
		// values.put("ATTACHMENT_TYPE", "01");
		// values.put("ATTACHMENT_SUFFIX", "rar");
		// values.put("ATTACHMENT_SIZE", 222);
		// values.put("CREATOR_ID", "");
		// values.put("CREATE_TIME", DateUtil.now());
		// values.put("CHECK_TYPE", "00");
		// values.put("CHECK_VALUE", "");
		// values.put("SAVE_PATH", "");
		// values.put("STATUS", "00");
		// values.put("COMMENTS", "");
		//
		// // 添加附件表记录和附件关联表记录
		// DataBean db = new DataBean("ATTACHMENT_INFO", "UID", values);
		// insert(hmLogSql, db);
		//
		// DataSourceBean dsb1 = (DataSourceBean) SpringUtil.getBean("DataSourceBean");
		// dsb1.setName("dsb1111");
		// DataSourceBean dsb2 = (DataSourceBean) SpringUtil.getBean("DataSourceBean");
		// dsb2.setName("dsb2222");

		List<Object> params0 = new ArrayList<Object>();
		params0.add("tetetest");

		List<Integer> outParameterTypes0 = new ArrayList<Integer>();
		outParameterTypes0.add(Types.VARCHAR);
		outParameterTypes0.add(Types.LONGVARCHAR);

		List<Object> outParameters0 = new ArrayList<Object>();

		this.executeProcedure(null, "{call test_proc(?, ?, ?)}", params0, outParameterTypes0, outParameters0);

		List<Object> params = new ArrayList<Object>();
		params.add("05370572-4bc6-4720-b17e-5aa9b7172f98");
		params.add("000001001400000000000000000000000000");
		params.add("R1311220919234240001");

		List<Integer> outParameterTypes = new ArrayList<Integer>();
		outParameterTypes.add(Types.LONGNVARCHAR);

		List<Object> outParameters = new ArrayList<Object>();

		this.executeProcedure(null, "{call revoke_user_right(?, ?, ?)}", params, outParameterTypes, outParameters);

		log.info("=============== enter goto_sys_permission_admin_page ================== ");

		// SubjectAuthInfo authInfo = getSubjectAuthInfo(dc);
		// Set<String> appIds = authInfo.getCurrentAppIds();

		// clearSubjectAuthInfo(dc);

		// SubjectAuthInfo authInfo = getSubjectAuthInfo(dc);
		// SubjectInfo subjectInfo = authInfo.getSubjectInfo();
		// Set<ResourceInfo> resources = authInfo.getCurrentResources();
		// Map<ResourceGroupType, List<ResourceInfo>> groupedResources = authInfo.getGroupedCurrentResources();

		// clearSubjectAuthInfo(dc);

		// ResourceService rs = new ResourceService();
		// Set<ResourceInfo> resources2 = rs.getUserResources("05370572-4bc6-4720-b17e-5aa9b7172f98", "APP0001", "00000", "000001001200000000000000000000000000");

		IRedisClient client0 = getRedisClient("default-db-5");
		String key = "zzz";
		client0.set(key, "沃尔特www1");
		System.out.println(client0.get(key));
		client0.set(key, "阿斯蒂芬22331");

		DbHelper dh = new DbHelper();
		BigDecimal count = (BigDecimal) dh.getOneFiledValue(null, " SELECT COUNT(1) FROM SYS_LOGS ", new ArrayList<Object>());
		log.info("=============== log count : " + count.intValue());

		KryoRedisSerializer<Map<String, Set<ResourceInfo>>> serializer = new KryoRedisSerializer<Map<String, Set<ResourceInfo>>>();
		AbstractRedisClientManager redisClientManager = CacheManager.getRedisClientManager();
		IRedisClient client = redisClientManager.getClient("default");
		// cache: <ConCommon.REDIS_KEY_PERMISSION, <subjectId, <departmentId, <ResourceInfoSet>>>>
		Object cache = client.hGet(SpringUtil.getEnvProperty(ConCommon.CFGKEY__REDIS_KEY_PERMISSION, ""), "jtjb");
		if (cache != null) {
			Map<String, Set<ResourceInfo>> deptResources = serializer.deserialize((byte[]) cache);
			System.out.println(deptResources);
		}

		Object authObj = dc.getSessionObject(SpringUtil.getEnvProperty(SubjectAuthInfo.CFGKEY__SESSION_SUBJECT_AUTH_INFO_KEY, ""));
		if (authObj != null) {
			SubjectAuthInfo auth = (SubjectAuthInfo) authObj;
			System.out.println(auth);
		}

		log.info("=============== end goto_sys_permission_admin_page ================== ");

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "sys/auth/sys_permission_admin";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

}
