package com.kind.base.auth.cloudservice.auth;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.kind.HandleSpringCloudService;
import com.kind.common.service.MicroServerUrlManager;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

public class CommonService {

	protected com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(CommonService.class);
	protected final static String fieldSeparator = "|";

	/**
	 * 获取请求的IP信息
	 * 
	 * @param dc
	 * @return
	 */
	public String getRequestIp(DataContext dc) {
		return (String) (dc.getHeader().get("ip") == null ? "" : dc.getHeader().get("ip"));
	}

	/**
	 * 校验接入IP合法性
	 * 
	 * @param dc
	 * @param validIpList
	 *            合法IP列表
	 * @param validIpSeparator
	 *            IP之间的分隔符
	 * @return
	 */
	public boolean isIpValid(DataContext dc, String validIpList, String validIpSeparator) {
		String requestIp = getRequestIp(dc);
		if (StrUtil.isNotBlank(requestIp) && !(validIpSeparator + validIpList + validIpSeparator).contains(validIpSeparator + requestIp + validIpSeparator)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 根据TOKEN获取appinfo
	 * 
	 * @param token
	 * @return
	 */
	public Map<String, Object> getAppInfoByToken(String token) {
		DataContext dc = new DataContext();
		dc.setReqeust("TOKEN", token);

		HandleSpringCloudService hscs = SpringUtil.getBean("handleSpringCloudService", HandleSpringCloudService.class);
		JSONObject json = hscs.handleService(MicroServerUrlManager.getNPortalServerUrl(), "get_appinfo_by_token", dc);
		// log.debug("调用微服务[get_appinfo_by_token][token: " + token + "]返回结果：" + json.toJSONString());
		if (json.getShort(Constant.FRAMEWORK_G_RESULT) == 1) {
			return json.getJSONObject("value");
		} else {
			return null;
		}
	}

	/**
	 * 接入信息验证。验证接入传参是否正确。 根据token获取appinfo，再比对appId和ip。
	 * 
	 * @param dc
	 * @param token
	 *            传参
	 * @param appId
	 *            传参
	 * @return 返回空:验证通过；其他值:验证不通过
	 */
	public String doAccessValid(DataContext dc, String token, String appId) {
		//
		// Map<String, Object> appInfo = this.getAppInfoByToken(token);
		// if (appInfo == null) {
		// return "未获取到APP信息[TOKEN: " + token + "]";
		// }
		//
		// String _appId = MapUtil.getStr(appInfo, "APP_ID");
		// if (!_appId.equalsIgnoreCase(appId)) {
		// return "APP_ID不正确[APP_ID: " + appId + "]";
		// }
		//
		// String _ips = MapUtil.getStr(appInfo, "IP");
		// if (StrUtil.isNotBlank(_ips) && !this.isIpValid(dc, _ips, "|")) {
		// return "IP校验失败[requestIp: " + getRequestIp(dc) + "]";
		// }

		return "";
	}

	/**
	 * 接入信息验证。验证接入传参是否正确。 根据token获取appinfo，再比对ip。
	 * 
	 * @param dc
	 * @param token
	 *            传参
	 * @return 返回空:验证通过；其他值:验证不通过
	 */
	public String doAccessValidWithoutAppIdCheck(DataContext dc, String token) {

		Map<String, Object> appInfo = this.getAppInfoByToken(token);
		if (appInfo == null) {
			return "未获取到APP信息[TOKEN: " + token + "]";
		}

		String _ips = MapUtil.getStr(appInfo, "IP");
		if (StrUtil.isNotBlank(_ips) && !this.isIpValid(dc, _ips, "|")) {
			return "IP校验失败[requestIp: " + getRequestIp(dc) + "]";
		}

		return "";
	}

}
