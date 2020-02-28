package com.kind.base.core.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.base.core.IConstant;
import com.kind.base.core.auth.PowerResourceUtil;
import com.kind.base.core.auth.ResourcesResetRedis;
import com.kind.common.utils.CodeSwitching;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.ActionEncryptUtil;
import com.kind.framework.utils.KryoUtils;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * @author zhengjiayun
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = true, action = "#goto_subsystem", description = "进入子系统菜单页", powerCode = "", requireTransaction = false)
public class GotoSubSystemAction extends BaseActionService {

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String appId = dc.getRequestString("APP_ID");
		if (StrUtil.isEmpty(appId)) {
			appId = CodeSwitching.getSystemAppId();
		}
		// session取值
		UserPO userpo = (UserPO) dc.getSession().get(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		dc.setReqeust("USERPO", KryoUtils.serializationObject(userpo));

		List<Map<String, Object>> reoust = new ResourcesResetRedis().getRedis(hmLogSql, appId);
		// 超级管理员默认拥有所有权限
		if (userpo.getOpType() > 0) {
			reoust = PowerResourceUtil.getPowerList(reoust, userpo, appId, "00001", "RID");
		}

		dc.setBusiness("APP_ID", appId);
		dc.setSession("SESSION_POWER_GN_" + appId, reoust);
		String csSkin = dc.getBusinessString("CS_SKIN");
		if (StrUtil.isEmpty(csSkin)) {
			csSkin = "default";
		}
		dc.setBusiness("cs_skin", csSkin);

		List<Object> param = new ArrayList<Object>();
		String sql = "SELECT APP_LOGIN_URL,APP_NAME,APP_PIC_ADDRESS,WELCOME_URL FROM PORTAL_APP_INFO WHERE APP_ID=?";
		param.add(appId);
		Map<String, Object> map = this.getSelectMap(hmLogSql, sql, param);
		if (map != null) {
			dc.setBusiness("APP_NAME", map.get("APP_NAME"));
			dc.setBusiness("APP_LOGIN_URL", map.get("APP_LOGIN_URL"));

			if (map.get("WELCOME_URL") != null) {
				String _wel_url = (String) map.get("WELCOME_URL");
				dc.setBusiness("WELCOME_URL", ActionEncryptUtil.encodeDoAction(_wel_url, dc.getCookie().get(SpringUtil.getEnvProperty("kisso.config.cookieName", "") + "_user")));
			}

			if (map.get("APP_PIC_ADDRESS") != null) {
				dc.setBusiness("APP_PIC_ADDRESS", map.get("APP_PIC_ADDRESS"));
			}
		}
		String title = dc.getRequestString("TITLE") == null ? "首页" : dc.getRequestString("TITLE");

	

		dc.setBusiness("TITLE", title);
		dc.setBusiness("KISSO_COOKIE_NAME", SpringUtil.getEnvProperty("kisso.config.cookieName", ""));

		return setSuccessInfo(dc);

	}

	@Override
	public String pageAddress(DataContext dc) {

		return "main_subsystem";

	}
}
