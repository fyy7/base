package com.kind.base.core.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.utils.CodeSwitching;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.ActionEncryptUtil;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * @author zhengjiayun
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = true, action = "#get_my_power_menu", description = "获取登录系统菜单", powerCode = "", requireTransaction = false)
public class GetMyPowerMenuService extends BaseActionService {

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// session取值
		UserPO userpo = (UserPO) dc.getSession().get(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		String appId = dc.getRequestString("APP_ID");
		if (StrUtil.isBlank(appId)) {
			return this.setFailInfo(dc, "未获取应用ID号！");
		}
		List<Map<String, Object>> modules = (List<Map<String, Object>>) dc.getSessionObject("SESSION_POWER_GN_" + appId);
		String baseAppId = CodeSwitching.getSystemAppId();
		// 系统id为空时取默认id
		if (StrUtil.isEmpty(appId)) {
			appId = baseAppId;
		}

		boolean sameApp = false;

		// 获取系统配置
		Map<String, Object> appConfig = getAppConfig(hmLogSql, dc, appId);
		String appUrl = String.valueOf(appConfig.get("APP_LOGIN_URL"));

		if (StrUtil.equals(appId, baseAppId)) {
			sameApp = true;
		} else {
			Map<String, Object> baseAppConfig = getAppConfig(hmLogSql, dc, baseAppId);
			boolean isAppNameSame = baseAppConfig != null && baseAppConfig.get("APP_LOGIN_URL") != null && StrUtil.equals(String.valueOf(appConfig.get("APP_LOGIN_URL")), String.valueOf(baseAppConfig.get("APP_LOGIN_URL")));
			if (isAppNameSame) {
				sameApp = true;
			}
		}

		StringBuffer sbHtml = new StringBuffer("");
		int iFirstTreePath = 0;

		if (userpo == null) {
			return setFailInfo(dc, "获取用户信息失败！");
		}

		for (int i = 0; i < modules.size(); i++) {
			if (modules.get(i).get("RLEVEL") != null && "2".equals(modules.get(i).get("RLEVEL").toString().trim())) {
				if (modules.get(i).get("ISGROUP") != null && "1".equals(modules.get(i).get("ISGROUP").toString())) {
					sbHtml.append("<li id='").append(iFirstTreePath == 0 ? "id_li_active" : "").append("'><a allordidx='").append(modules.get(i).get("ALLORDIDX")).append("' href='javascript:void(0);' class='parent_tree'>").append(modules.get(i).get("TITLE").toString().trim()).append("</a>");
					try {
						sbHtml.append(getMenuHtml2(dc, 2, modules.get(i).get("ALLORDIDX").toString(), modules, userpo, sameApp, appUrl));
					} catch (Exception e) {

						e.printStackTrace();
					}
					sbHtml.append("</li>");
				} else {
					String srcValue = sameApp ? String.valueOf(modules.get(i).get("EXTITEM1")) : (((String.valueOf(modules.get(i).get("EXTITEM1")).toLowerCase().startsWith("http://")) ? "" : appUrl) + String.valueOf(modules.get(i).get("EXTITEM1")));
					srcValue = encodeUrlHandle(dc, srcValue);
					sbHtml.append("<li><a allordidx='").append(modules.get(i).get("ALLORDIDX")).append("' href='javascript:void(0);' class='cs-navi-tab' style='margin-left: 2px;' src='").append(srcValue).append("'>").append(modules.get(i).get("TITLE")).append("</a></li>");
				}
				iFirstTreePath = 1;
			}
		}
		dc.setBusiness("back_html", sbHtml.toString());

		return setSuccessInfo(dc);

	}

	@Override
	public String pageAddress(DataContext dc) {
		return "main_portal";
	}

	public static String getMenuHtml2(DataContext dc, int rlevel, String allordidx, List<Map<String, Object>> modules, UserPO sysuerpo, boolean sameApp, String appUrl) throws Exception {
		StringBuffer sbHtml = new StringBuffer();
		sbHtml.append("<ul style='list-style:none;'>");
		rlevel++;
		for (int i = 0; i < modules.size(); i++) {
			if (modules.get(i).get("RLEVEL") != null && String.valueOf(rlevel).equals(modules.get(i).get("RLEVEL").toString())) {
				if (String.valueOf(modules.get(i).get("ALLORDIDX")).startsWith(allordidx)) {
					if (Integer.parseInt(String.valueOf(modules.get(i).get("ISGROUP"))) == 1) {
						sbHtml.append("<li><a allordidx='").append(modules.get(i).get("ALLORDIDX")).append("' href='javascript:void(0);' class='parent_tree' style='margin-left:");
						sbHtml.append(((rlevel - 1) * 4)).append("px;'>").append(modules.get(i).get("TITLE").toString().trim()).append("</a>");
						sbHtml.append(getMenuHtml2(dc, Integer.parseInt(modules.get(i).get("RLEVEL").toString()), modules.get(i).get("ALLORDIDX").toString(), modules, sysuerpo, sameApp, appUrl));
						sbHtml.append("</li>");
					} else {
						// modules.get(i).printAllKeyValue();
						sbHtml.append("<li><a allordidx='");
						sbHtml.append(modules.get(i).get("ALLORDIDX")).append("' href='javascript:void(0);' class='cs-navi-tab' style='margin-left:");
						sbHtml.append(((rlevel - 1) * 9) + "px;' src='");
						if (modules.get(i).get("EXTITEM1") != null && StrUtil.isNotBlank(String.valueOf(modules.get(i).get("EXTITEM1")))) {
							// String srcValue = sameApp ? String.valueOf(modules.get(i).get("EXTITEM1")) : (appUrl + String.valueOf(modules.get(i).get("EXTITEM1")));

							String srcValue = sameApp ? String.valueOf(modules.get(i).get("EXTITEM1")) : (((String.valueOf(modules.get(i).get("EXTITEM1")).toLowerCase().startsWith("http://")) ? "" : appUrl) + String.valueOf(modules.get(i).get("EXTITEM1")));

							sbHtml.append(encodeUrlHandle(dc, srcValue));

							// 以http://开头的不加rlogo
							if (!srcValue.toLowerCase().startsWith("http://")) {
								sbHtml.append(modules.get(i).get("EXTITEM1").toString().indexOf("?") > -1 ? "&" : "?");
								sbHtml.append("RLOGO=").append(modules.get(i).get("RLOGO"));
							}
						}
						sbHtml.append("'>");
						// if (modules.get(i).get("EXTITEM3") != null) {
						// 显示图标
						// sbHtml.append("<img height='12' src='").append(modules.get(i).get("EXTITEM3")).append("'/>&nbsp;");
						// }
						sbHtml.append(modules.get(i).get("TITLE").toString().trim()).append("</a></li>");
					}
				}
			}
		}
		sbHtml.append("</ul>");
		// System.out.println(sbHtml);
		return sbHtml.toString();
	}

	private static String encodeUrlHandle(DataContext dc, String srcValue) {
		if ("1".equals(SpringUtil.getEnvProperty(Constant.APP_ACTION_ENCYPT_SWITCH))) {

			// 有开启action加密的
			if (!srcValue.toLowerCase().startsWith("http://") && srcValue.indexOf("do?action=") > -1) {
				// 对Action的do地址进行加密
				srcValue = ActionEncryptUtil.encodeDoAction(srcValue, dc.getCookie().get(SpringUtil.getEnvProperty("kisso.config.cookieName", "") + "_user"));
			}
		}
		return srcValue;
	}

	private Map<String, Object> getAppConfig(HashMap<String, String> hmLogSql, DataContext dc, String appId) {
		List<Object> param = new ArrayList<Object>();
		String sql = "SELECT APP_LOGIN_URL,APP_NAME,APP_PIC_ADDRESS FROM PORTAL_APP_INFO WHERE APP_ID=?";
		param.add(appId);
		Map<String, Object> map = this.getSelectMap(hmLogSql, sql, param);
		if (map != null) {
			return map;
		} else {
			return null;
		}
	}
}
