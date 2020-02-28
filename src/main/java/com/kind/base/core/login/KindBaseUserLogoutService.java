package com.kind.base.core.login;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.kisso.security.token.SSOToken;
import com.kind.common.constant.ConCommon;
import com.kind.common.utils.CodeSwitching;
import com.kind.framework.auth.AuthManager;
import com.kind.framework.auth.SubjectAuthInfo.SubjectType;
import com.kind.framework.cache.redis.IRedisClient;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SSoTakenUtils;
import com.kind.framework.utils.SpringUtil;

/**
 * @author zhengjiayun
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = false, action = "#kind_base_user_logout", description = "用户退出登录", powerCode = "", requireTransaction = false)
public class KindBaseUserLogoutService extends BaseActionService {

	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(getClass());

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// 退出单点登录
		dc.setClearSsoToken(true);
		dc.setBusiness("VERIFY_FLAG", CodeSwitching.getReidiosJsonDm("sys", "5"));
		/** added by yanhang begin **/
		// 清除redis里登录用户对应的权限数据
		UserPO user = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		IRedisClient client = this.getRedisClient();
		String cfgKey = SpringUtil.getEnvProperty(ConCommon.CFGKEY__REDIS_KEY_PERMISSION, "");
		if (user != null && client.exists(cfgKey)) {
			if (user.getConsignorType() == 1) {
				client.hDel(cfgKey, user.getConsignorSysUserPO().getOpAccount());
			} else {
				client.hDel(cfgKey, user.getOpAccount());
			}
		}

		SSOToken _ssoToken = dc.getSsoToken();
		JSONObject encJson = SSoTakenUtils.getJson(_ssoToken);

		if (_ssoToken != null) {
			// 清除redis中的登录信息
			client.remove(Constant.FRAMEWORK_ONLINE_USER_REDIS_KEY + ":" + encJson.getString(Constant.KISSO_JWT_ENC_JSON_ACCOUNT) + ":" + encJson.getString(Constant.KISSO_JWT_ENC_JSON_DEPTID) + ":" + _ssoToken.getTime());
		}

		AuthManager.onLogout(dc, SubjectType.USER);
		/** added by yanhang end **/

		log.debug("用户退出");
		dc.setRemoveSession(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		dc.setRemoveCookie(SpringUtil.getEnvProperty("kisso.config.cookieName", "") + "_md5");
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "login";
	}
}
