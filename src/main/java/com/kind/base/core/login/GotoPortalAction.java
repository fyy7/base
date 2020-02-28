package com.kind.base.core.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.kind.framework.auth.SubjectAuthInfo;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.ActionEncryptUtil;
import com.kind.framework.utils.SpringUtil;

/**
 * @author ZHENGJIAYUN
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = true, action = "#goto_portal", description = "用户登录验证", powerCode = "", requireTransaction = false)
public class GotoPortalAction extends BaseActionService {

	// private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(getClass());

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// session取值
		UserPO userpo = (UserPO) dc.getSession().get(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		List<Object> paramList = new ArrayList<Object>();

		// 假删除的，停用的状态不查询出来
		String querySql = "SELECT app_request_url,APP_STATUS,APP_LOGIN_URL,APP_ID,APP_PIC_ADDRESS,APP_NAME,APP_LOGO,APP_TYPE FROM PORTAL_APP_INFO WHERE IS_DEL=0 and APP_STATUS=1 ORDER BY APP_ORDER";
		List<Map<String, Object>> reoust = getSelectList(hmLogSql, querySql, paramList);

		List<Map<String, Object>> rigthReouse = new ArrayList<>();
		// 超级管理员默认拥有所有权限
		if (userpo.getOpType() > 0) {
			SubjectAuthInfo subjectAuthInfo = getSubjectAuthInfo(dc);
			Set<String> appResources = subjectAuthInfo.getCurrentAppIds();
			JSONObject tmp = new JSONObject();
			// 数据转换，便于比较
			for (Object appid : appResources) {
				tmp.put(appid.toString(), true);
			}

			// rigthReouse = PowerResourceUtil.comparePowerList(reoust, tmp, "APP_ID");
			for (int i = 0; i < reoust.size(); i++) {
				if (tmp.getBooleanValue(reoust.get(i).get("APP_ID").toString())) {
					Object url = reoust.get(i).get("APP_LOGIN_URL");

					if (url != null && String.valueOf(url).indexOf("do?action=") > -1) {
						// 对相关有action地址的进行加密
						reoust.get(i).put("APP_LOGIN_URL", ActionEncryptUtil.encodeDoAction(String.valueOf(url), dc.getCookie().get(SpringUtil.getEnvProperty("kisso.config.cookieName", "") + "_user")));
					}

					rigthReouse.add(reoust.get(i));
				}
			}
		} else {
			for (int i = 0; i < reoust.size(); i++) {
				Object url = reoust.get(i).get("APP_LOGIN_URL");
				if (url != null && String.valueOf(url).indexOf("do?action=") > -1) {
					// 对相关有action地址的进行加密
					reoust.get(i).put("APP_LOGIN_URL", ActionEncryptUtil.encodeDoAction(String.valueOf(url), dc.getCookie().get(SpringUtil.getEnvProperty("kisso.config.cookieName", "") + "_user")));
				}
			}
			rigthReouse = reoust;
		}

		// for (Map<String, Object> map : rigthReouse) {
		// // 判断是否是内部系统且系统标识（APP_LOGIN_URL）合法
		// boolean isInternal = StrUtil.equals("1INTERNAL", String.valueOf(map.get("APP_TYPE"))) && map.get("APP_LOGIN_URL") != null;
		// if (isInternal) {
		// StringBuffer appLoginUrl = new StringBuffer("/nportal/do?action=goto_subsystem&APP_ID=");
		// map.put("APP_LOGIN_URL", appLoginUrl.append(map.get("APP_ID")));
		// }
		// }

		dc.setBusiness("rows", rigthReouse);
		dc.setBusiness("KISSO_COOKIE_NAME", SpringUtil.getEnvProperty("kisso.config.cookieName", ""));

		return setSuccessInfo(dc);

	}

	@Override
	public String pageAddress(DataContext dc) {

		return "main_portal";

	}
}
