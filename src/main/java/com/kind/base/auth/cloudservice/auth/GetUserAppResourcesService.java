package com.kind.base.auth.cloudservice.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.base.auth.service.auth.PowerResourceUtil;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.KryoUtils;

/**
 * 获取登录用户的站点菜单列表
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "#get_user_app_resources", description = "获取登录用户的站点菜单列表", powerCode = "", requireTransaction = false)
public class GetUserAppResourcesService extends BaseActionService {
	// private KryoRedisSerializer<List<Map<String, Object>>> serializer = new KryoRedisSerializer<List<Map<String, Object>>>();

	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(GetUserAppResourcesService.class);

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		ResourcesResetRedis rrr = new ResourcesResetRedis();
		Map<String, Object> rm = this.getSuccessBaseResultMap();
		String msg = "";
		try {
			// String token = dc.getRequestObject("TOKEN", "");
			String appId = dc.getRequestObject("APP_ID", "");
			String sUserpo = dc.getRequestObject("USERPO", "");
			List<Map<String, Object>> reoust;
			// String redisPermissionKey = rrr.getPermissionKey(appId);
			// 获取redios缓存
			reoust = rrr.getRedis(hmLogSql, appId);

			// 超级管理员默认拥有所有权限
			UserPO userpo = KryoUtils.deserializationObject(sUserpo, UserPO.class);
			if (userpo.getOpType() > 0) {
				reoust = PowerResourceUtil.getPowerList(reoust, userpo, appId, "00001", "RID");
			}

			dc.setBusiness("MENUS", reoust);
		} catch (Exception e) {
			msg = "获取登录用户的站点菜单列表！";
			rm = this.getBaseResultMap(0, msg);
			log.info(msg + System.getProperty("line.separator") + getStackTrace(e));

			dc.getBusiness().putAll(rm);
			// throw new RuntimeException(msg + e.getMessage());
			return setFailInfo(dc);
		}

		dc.getBusiness().putAll(rm);

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
