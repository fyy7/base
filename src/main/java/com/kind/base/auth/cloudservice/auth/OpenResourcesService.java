package com.kind.base.auth.cloudservice.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.kind.HandleSpringCloudService;
import com.kind.common.constant.ConCommon;
import com.kind.common.service.MicroServerUrlManager;
import com.kind.framework.cache.CacheManager;
import com.kind.framework.cache.redis.AbstractRedisClientManager;
import com.kind.framework.cache.redis.IRedisClient;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 资源开放接口，供外部第三方系统调用<br>
 * 说明： 1、内部调用时，无需传入TOKEN，不做token和ip验证，所需参数根据开放接口支持的各内部相关接口文档定义。 2、外部调用时，无需传入APP_ID，需传入token，接口做token和ip验证。 3、内外部调用均需传入MSG_TYPE参数，定义：KD0001:资源注入, KD0002:删除资源, KD0003: 获取用户资源
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "#open_resource_service", description = "资源开放接口", powerCode = "", requireTransaction = true)
public class OpenResourcesService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(OpenResourcesService.class);

	private CommonService commonService = new CommonService();

	// 记录最后一次获取用户资源的时间，用于判断时间间隔来删除第三方用户资源缓存
	private static Map<String, Long> lastFetchUserResourcesTime = new HashMap<String, Long>();
	private static Map<String, String> userOpAccounts = new HashMap<String, String>();

	/**
	 * HTTP需post请求<br>
	 */
	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String msg = "";
		String msgType = "";
		try {
			msgType = dc.getRequestObject("MSG_TYPE", "");
			String token = dc.getRequestObject("TOKEN", "");

			if (StrUtil.isBlank(msgType)) {
				return setFailInfo(dc, "MSG_TYPE不能为空");
			}

			Map<String, Object> appInfo = null;
			Object userpoObj = dc.getSession().get(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
			UserPO userpo = userpoObj == null ? null : (UserPO) userpoObj;
			if (userpo == null) { // 有登录用户信息则视为内部系统，否则为外部系统，对外部系统调用进行token和ip验证
				if (StrUtil.isBlank(token)) {
					return setFailInfo(dc, "token为空，请求失败！");
				}

				appInfo = commonService.getAppInfoByToken(token);
				if (appInfo == null) {
					return setFailInfo(dc, "未获取到APP信息[TOKEN: " + token + "]");
				}
				dc.setReqeust("APP_ID", MapUtil.getStr(appInfo, "APP_ID"));

				String _ips = MapUtil.getStr(appInfo, "IP");
				if (StrUtil.isNotBlank(_ips) && !commonService.isIpValid(dc, _ips, "|")) {
					return setFailInfo(dc, "IP校验失败[requestIp: " + commonService.getRequestIp(dc) + "]");
				}

				// 相关值预置
				if ("KD0001".equalsIgnoreCase(msgType)) {
				} else if ("KD0002".equalsIgnoreCase(msgType)) {
				} else if ("KD0003".equalsIgnoreCase(msgType)) {
				}
			}

			String action = "";
			// 0001:资源注入, 0002:删除资源, 0003: 获取用户资源,0004:获取用户信息
			if ("KD0001".equalsIgnoreCase(msgType)) {
				action = "inject_channel_resource";
			} else if ("KD0002".equalsIgnoreCase(msgType)) {
				action = "delete_resources_service";
			} else if ("KD0003".equalsIgnoreCase(msgType)) {
				action = "get_resources_service";
				dc.setReqeust("RESOURCE_TYPE", "00003");
				dc.setReqeust("THIRD_PART", "1");

				// 第三方调用由于用户没做退出操作，可能缓存会一直存在，此处先直接清除缓存
				this.clearThirdResourceCache(dc.getRequestObject("USER_ID", ""));
			} else if ("KD0004".equalsIgnoreCase(msgType)) {
				action = "get_login_info";
			} else {
				return setFailInfo(dc, "MSG_TYPE非法");
			}

			HandleSpringCloudService hscs = SpringUtil.getBean("handleSpringCloudService", HandleSpringCloudService.class);
			JSONObject json = hscs.handleService(MicroServerUrlManager.getAuthServerUrl(), action, dc);
			log.debug("调用微服务[get_appinfo_by_token][token: " + token + "]返回结果：" + json.toJSONString());

			dc.getBusiness().putAll(json);

			return json.getShort(Constant.FRAMEWORK_G_RESULT);
		} catch (Exception e) {
			msg = String.format("资源开放接口处理异常[msgType: %s]！%s", msgType, e.getMessage());
			log.error(msg, e);

			return setFailInfo(dc, msg);
		}
	}

	private void clearThirdResourceCache(String userId) {
		String userOpAccount = getUserOpAccount(userId);
		if (StrUtil.isBlank(userOpAccount)) {
			return;
		}

		// Long time = lastFetchUserResourcesTime.get(userOpAccount);
		// if (time == null) {
		// time = 0L;
		// }
		// if (new Date().getTime() - time < 1) {
		// lastFetchUserResourcesTime.put(userOpAccount, new Date().getTime());
		// return;
		// }

		// Redis里整体存储的KEY
		String redisPermissionKey = SpringUtil.getEnvProperty(ConCommon.CFGKEY__REDIS_KEY_PERMISSION, "");

		AbstractRedisClientManager redisClientManager = CacheManager.getRedisClientManager();
		IRedisClient client = redisClientManager.getClient(SpringUtil.getEnvProperty("app.system.resource.redisName", "default"));

		// cache: <ConCommon.REDIS_KEY_PERMISSION, <subjectId, <redisUserPermissionKey, <ResourceInfoSet>>>>
		client.hDel(redisPermissionKey, userOpAccount);
	}

	public String getUserOpAccount(String userId) {
		if (StrUtil.isBlank(userId)) {
			return "";
		}

		if (!userOpAccounts.containsKey(userId)) {
			List<Object> paramList = new ArrayList<Object>();
			StringBuffer sql = new StringBuffer(" SELECT U.OPACCOUNT, U.OPTYPE FROM SYS_N_USERS U WHERE U.ISDEL = 0 AND U.ENABLED = 1 AND U.OPNO = ? ");
			paramList.add(userId);

			Map<String, Object> userAccount = this.getSelectMap(null, sql.toString(), paramList);
			if (userAccount == null) {
				return null;
			}

			String userOpAccount = userAccount.get("OPACCOUNT") != null ? (String) userAccount.get("OPACCOUNT") : "";

			userOpAccounts.put(userId, userOpAccount);
		}

		return userOpAccounts.get(userId);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "common/json";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

}
