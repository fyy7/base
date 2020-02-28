package com.kind.base.auth.service.auth;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpSession;

import com.kind.base.core.auth.ResourceService;
import com.kind.common.constant.ConCommon;
import com.kind.framework.auth.IAuthService;
import com.kind.framework.auth.ResourceInfo;
import com.kind.framework.auth.SubjectAuthInfo.SubjectType;
import com.kind.framework.auth.SubjectAuthInfo.UserType;
import com.kind.framework.auth.SubjectInfo;
import com.kind.framework.cache.CacheManager;
import com.kind.framework.cache.redis.AbstractRedisClientManager;
import com.kind.framework.cache.redis.IRedisClient;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.utils.SpringUtil;

/**
 * 门户授权管理服务类
 * 
 * @author yanhang
 */
public class PortalAuthServiceImpl extends ResourceService implements IAuthService {

	@Override
	public void init() {
		AbstractRedisClientManager redisClientManager = CacheManager.getRedisClientManager();
		IRedisClient client = redisClientManager.getClient("default");
		client.remove(SpringUtil.getEnvProperty(ConCommon.CFGKEY__REDIS_KEY_PERMISSION, ""));
	}

	/**
	 * 获取主体在当前登录情况下的权限，用于对系统service访问进行权限判断<br>
	 */
	@Override
	public Set<ResourceInfo> getCurrentPermissionResources(HttpSession session, SubjectInfo subjectInfo) {
		Set<ResourceInfo> resources = new HashSet<ResourceInfo>();

		if (SubjectType.USER.equals(subjectInfo.getType()) && !UserType.SUPER_ADMIN.equals(subjectInfo.getUserType())) {
			UserPO user = (UserPO) subjectInfo.get("subjectObj");
			String currentChannelId = "";
			// String currentChannelId = SpringUtil.getEnvProperty(Constant.APP_SYSTEM_APPID, "");
			// String currentPermissionResourceType = SpringUtil.getEnvProperty("app.system.appResourceType", "");
			// String currentPermissionResourceType = ConCommon.RESOURCE_TYPE_PERMISSION;
			String currentPermissionResourceType = ""; // 获取所有资源类型的资源
			resources = this.getUserResourcesByUserPO(user, currentChannelId, currentPermissionResourceType);
		}

		return resources;
	}

}
