package com.kind.base.user.cloudservice.user;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.kisso.common.encrypt.MD5;
import com.baomidou.kisso.security.token.SSOToken;
import com.kind.HandleSpringCloudService;
import com.kind.common.constant.ConCommon;
import com.kind.common.service.MicroServerUrlManager;
import com.kind.common.utils.ConvertSqlDefault;
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

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author chenzhiwei
 *
 *         2018年4月9日
 */
@Service
@Action(requireLogin = true, action = "#user_change_dept", description = "切换用户保存", powerCode = "", requireTransaction = false)
public class ChangeDeptService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));

		if (userpo.getConsignorType() == 1) {
			userpo = userpo.getConsignorSysUserPO();
		}
		UserPO userpoNew = new UserPO();
		JSONObject json = new JSONObject();
		String flag = dc.getRequestString("flag");

		if (userpo.getOpType() >= 2) {
			// 调用微服务验证前台参数是否合法
			HandleSpringCloudService hscs = SpringUtil.getBean("handleSpringCloudService", HandleSpringCloudService.class);
			String service = MicroServerUrlManager.getServerUrl("kind.framework.ms.urls.user");
			json = hscs.handleService(service, "user_change_dept_check", dc);

			if (json.getShort(Constant.FRAMEWORK_G_RESULT) == 0) {
				return setFailInfo(dc, json.getString(Constant.FRAMEWORK_G_MESSAGE));
			}
			for (Map.Entry<String, Object> entry : json.entrySet()) {
				dc.setBusiness(entry.getKey(), json.get(entry.getKey()));
			}
			// 機構管理員或者admin切換判斷
		} else if ((userpo.getOpType() == 1 && !userpo.getOrgId().equals(dc.getRequestString("ORGANID"))) || "2".equals(flag)) {
			// 机构管理员，只能切换自己机构的的部门/admin和机构管理员不能切委托
			return setFailInfo(dc, "切换失败！。");
		}

		// 直接切换部门
		if (!"2".equals(flag)) {
			userpoNew = userpo;
		} else {
			// 委托状态,
			userpoNew.setConsignorType(1);
			userpoNew.setConsignorSysUserPO(userpo);
			userpoNew.setOpName(dc.getRequestString("OPNONAME") + "(" + userpo.getOpName() + "-代)");
			userpoNew.setOpNo(dc.getRequestString("OPNO"));
			userpoNew.setOpAccount(json.getString("OPACCOUNT"));

		}

		userpoNew.setOrgId(dc.getRequestString("ORGANID"));
		userpoNew.setOrgName(dc.getRequestString("ORGAN_NAME"));
		userpoNew.setDeptId(dc.getRequestString("DEPT_ID"));
		userpoNew.setDeptName(dc.getRequestString("DEPT_NAME"));
		dc.setSession(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM), userpoNew);

		// 清除旧登录的用户身份
		SSOToken _ssoToken = dc.getSsoToken();
		if (_ssoToken != null) {
			JSONObject encJson = SSoTakenUtils.getJson(_ssoToken);
			IRedisClient client = this.getRedisClient();
			// 清除redis中的登录信息
			client.remove(Constant.FRAMEWORK_ONLINE_USER_REDIS_KEY + ":" + encJson.getString(Constant.KISSO_JWT_ENC_JSON_ACCOUNT) + ":" + encJson.getString(Constant.KISSO_JWT_ENC_JSON_DEPTID) + ":" + _ssoToken.getTime());
		}

		// 设置单点登录信息
		SSOToken ssoToken = SSOToken.create();

		// 自定义的json对象，存放业务相关数据
		JSONObject data = new JSONObject();
		data.put(Constant.KISSO_JWT_ENC_JSON_ACCOUNT, userpoNew.getOpAccount());
		data.put(Constant.KISSO_JWT_ENC_JSON_DEPTID, userpoNew.getDeptId());

		// 处理自定义的key值
		SSoTakenUtils.setData(ssoToken, data);

		dc.setSsoToken(ssoToken);

		// 设置加载sso信息
		dc.setSsoTokenFlag(true);

		// 用户账号与部门id做md5唯一
		dc.setCookie(SpringUtil.getEnvProperty("kisso.config.cookieName", "") + "_md5", MD5.toMD5(userpoNew.getOpAccount() + userpoNew.getDeptId()), Integer.MAX_VALUE, "/", SpringUtil.getEnvProperty("kisso.config.cookieDomain", ""));

		long loginTime = System.currentTimeMillis();
		dc.setSession(Constant.LOGIN_TIME, loginTime);

		// 权限加载
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
		AuthManager.onLogout(dc, SubjectType.USER);
		AuthManager.onLogin(dc, SubjectType.USER, userpoNew);

		return this.setSuccessInfo(dc, "修改成功！");
	}

	@Override
	public String pageAddress(DataContext arg0) {
		return null;
	}

	@Override
	public String verifyParameter(DataContext arg0) {
		return null;
	}
}
