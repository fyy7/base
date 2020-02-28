package com.kind.base.core.auth.impl;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpSession;

import com.kind.base.core.auth.ResourceService;
import com.kind.framework.auth.IAuthService;
import com.kind.framework.auth.ResourceInfo;
import com.kind.framework.auth.SubjectAuthInfo.SubjectType;
import com.kind.framework.auth.SubjectAuthInfo.UserType;
import com.kind.framework.auth.SubjectInfo;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.utils.SpringUtil;

/**
 * 通用通过微服务获取权限信息 Copyright (c) 2017-2018 KIND Corp. 2017-2018,All Rights Reserved. This software is published under the KIND Team. License version 1.0, a copy of which has been included with this distribution in the LICENSE.txt file.
 *
 * @File name: MicroServerAuthServiceImpl.java
 * @Description: 通用通过微服务获取权限信息
 * @Create on: 2018年5月6日
 * @author: yanhang
 *
 * @ChangeList --------------------------------------------------- Date Editor ChangeReasons
 *
 */
public class SingleServerAuthServiceImpl implements IAuthService {
	private static com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(SingleServerAuthServiceImpl.class);

	@Override
	public void init() {
		// AbstractRedisClientManager redisClientManager = CacheManager.getRedisClientManager();
		// IRedisClient client = redisClientManager.getClient("default");
		// client.remove(SpringUtil.getEnvProperty(ConCommon.CFGKEY__REDIS_KEY_PERMISSION, ""));
	}

	/**
	 * 获取主体在当前登录情况下的权限，用于对系统service访问进行权限判断<br>
	 */
	@Override
	public Set<ResourceInfo> getCurrentPermissionResources(HttpSession session, SubjectInfo subjectInfo) {
		ResourceService resourceService = SpringUtil.getBean(ResourceService.class);
		Set<ResourceInfo> resources = new HashSet<ResourceInfo>();

		if (SubjectType.USER.equals(subjectInfo.getType()) && !UserType.SUPER_ADMIN.equals(subjectInfo.getUserType())) {
			Object obj = subjectInfo.get("subjectObj");
			if (obj != null && obj instanceof UserPO) {
				UserPO user = (UserPO) subjectInfo.get("subjectObj");

				String userOpNo = user.getOpNo();
				String deptId = user.getDeptId();
				String organId = user.getOrgId();

				resources = resourceService.getUserResources(userOpNo, "", "", deptId, organId);
			}
		}

		return resources;
	}

	public static void main(String[] args) {
	}

}
