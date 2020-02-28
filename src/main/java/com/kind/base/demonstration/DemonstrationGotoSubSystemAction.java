package com.kind.base.demonstration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kind.base.core.auth.PowerResourceUtil;
import com.kind.base.core.auth.ResourcesResetRedis;
import com.kind.common.utils.CodeSwitching;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.KryoUtils;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 演示页
 * 
 * @author yanhang0610
 */
@Service
@Action(requireLogin = true, action = "#demonstration_goto_subsystem", description = "[演示]进入子系统菜单页", powerCode = "", requireTransaction = false)
public class DemonstrationGotoSubSystemAction extends BaseActionService {

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

		String globalCfgJsonStr = FileUtil.readUtf8String("/quick_demonstration_config/GLOBAL.txt");
		JSONObject globalCfgJson = JSONObject.parseObject(globalCfgJsonStr);
		dc.setBusiness("APP_NAME", globalCfgJson.getString("homePageTitle"));
		dc.setBusiness("TITLE", dc.getRequestString("TITLE"));
		dc.setBusiness("APP_PIC_ADDRESS", globalCfgJson.getString("systemLogoUrl"));
		dc.setBusiness("COPY_RIGHTS", globalCfgJson.getString("copyRights"));

		dc.setBusiness("WELCOME_URL", dc.getRequestString("WELCOMEURL"));
		dc.setBusiness("KISSO_COOKIE_NAME", SpringUtil.getEnvProperty("kisso.config.cookieName", ""));

		// 页面需配置：var i_model="1"; 设置为测试模式，以免系统加载库表里菜单数据
		// <ul class='topnav' id='id_resource_menu'>
		// <li id='id_li_active'>
		// <a allordidx='0.0005.0001' href='javascript:void(0);' class='parent_tree'>基础信息管理</a>
		// <ul style='list-style:none;'>
		// <li><a allordidx='0.0005.0001.0001' href='javascript:void(0);' class='cs-navi-tab' style='margin-left:18px;' src='do?action=organization_main&RLOGO=jb.basic.manage.social.org'>社保机构信息管理</a></li>
		// <li><a allordidx='0.0005.0001.0002' href='javascript:void(0);' class='cs-navi-tab' style='margin-left:18px;' src='do?action=bank_main&RLOGO=jb.basic.manage.bank'>银行信息管理</a></li>
		// </ul>
		// </li>
		// </ul>
		JSONArray menus = globalCfgJson.getJSONArray("menus");
		StringBuffer menuHtml = new StringBuffer();
		menuHtml.append("<ul class='topnav' id='id_resource_menu'>");
		for (int i = 0; i < menus.size(); i++) {
			if (i == 0) {
				menuHtml.append("<li id='id_li_active'>");
			} else {
				menuHtml.append("<li>");
			}

			JSONObject menu = menus.getJSONObject(i);
			menuHtml.append("<a allordidx='0.0005.0001' href='javascript:void(0);' class='parent_tree'>" + menu.getString("name") + "</a>");
			menuHtml.append("<ul style='list-style:none;'>");

			JSONArray subMenus = menu.getJSONArray("subMenus");
			if (subMenus != null) {
				for (int j = 0; j < subMenus.size(); j++) {
					JSONObject subMenu = subMenus.getJSONObject(j);
					menuHtml.append("<li><a allordidx='' href='javascript:void(0);' class='cs-navi-tab' style='margin-left:18px;' src='do?action=demonstration_goto_main_page&pageId=" + subMenu.getString("targetPageId") + "&RLOGO='>" + subMenu.getString("name") + "</a></li>");
				}
			}

			menuHtml.append("</ul>");
			menuHtml.append("</li>");
		}
		menuHtml.append("</ul>");

		dc.setBusiness("MENU_HTML", menuHtml.toString());

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "demonstration/demonstration_main_subsystem";
	}
}