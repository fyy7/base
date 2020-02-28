package com.kind.base.user.webservice;

import java.util.HashMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kind.common.webservice.client.ServiceBean;
import com.kind.common.webservice.client.ServiceClient;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * 与.net即时通讯的相关调用类
 *
 */
public class IMWebServiceUtil {
	private static final com.kind.framework.log.Logger logger = com.kind.framework.log.LogService.getLogger(IMWebServiceUtil.class);

	public static void main(String[] args) {
		System.out.println(System.currentTimeMillis());
	}

	/**
	 * 登录票据
	 */
	private static String ticket = null;

	/**
	 * 发送与WebService的调用
	 * 
	 * @param method
	 *            调用类型
	 * @param data
	 *            数据map
	 * @return 1为成功，其它为失败
	 * @throws Exception
	 */
	public static JSONObject send(String method, HashMap<String, Object> data) {
		if (!"1".equals(SpringUtil.getEnvProperty("IM_webservice_switch"))) {
			// 不需要调用的直接返回
			return getJson(0, "", "");
		}

		if (StrUtil.isEmpty(ticket)) {
			// 为空重新获取
			JSONObject json = getTicket();
			if (json.getIntValue("ReturnCode") == 0) {
				ticket = json.getString("Description");
			} else {
				return json;
			}
			logger.debug("------第一次获取票据值为：" + ticket);
		}
		logger.debug(SpringUtil.getEnvProperty("IM_webservice_url_address"));
		logger.debug(SpringUtil.getEnvProperty("IM_webservice_method_" + method));
		logger.debug(SpringUtil.getEnvProperty("IM_webservice_url_namespace"));
		logger.debug(method);

		ServiceBean bean = new ServiceBean(SpringUtil.getEnvProperty("IM_webservice_url_address"), "Operate", "ticket/operateType/paraType/paraInfo", SpringUtil.getEnvProperty("IM_webservice_url_namespace"));
		bean.addParameter(1, ticket);
		bean.addParameter(2, "KindWs4Java.Operate.KindTalk." + method);

		JSONObject json_data = new JSONObject();
		String arr_key[] = SpringUtil.getEnvProperty("IM_webservice_method_" + method).split("/");
		if (arr_key != null && arr_key.length > 1) {
			for (String key : arr_key) {
				if (data.get(key) == null) {
					return getJson(1, "错误", "字段" + key + "为空！");
				}
				json_data.put(key, data.get(key));
				logger.debug(data.get(key));
			}
			bean.addParameter(3, "Json");
			bean.addParameter(4, json_data.toJSONString());
		} else if (arr_key != null && arr_key.length == 1) {
			bean.addParameter(3, "Normal");
			// 如果是单个参数的，直接用字符串，（靠！统一用json串不行，非得要再搞一个解析类型出来）
			bean.addParameter(4, data.get(arr_key[0]));
		}
		try {
			// 返回类似这种格式：{"ReturnCode":0,"Description":"","DetailInfo":null}
			String result = String.valueOf(ServiceClient.getWebServiceResult(bean));
			JSONObject json = JSON.parseObject(result);
			if (json.getIntValue("ReturnCode") == 2) {
				// 登录失败
				JSONObject json2 = getTicket();
				if (json2.getIntValue("ReturnCode") == 0) {
					ticket = json2.getString("Description");
					logger.error("------重新获取票据值为：" + ticket);
				} else {
					return json;
				}
				// 重新请求一次
				bean.addParameter(1, ticket);
				result = String.valueOf(ServiceClient.getWebServiceResult(bean));
				json = JSON.parseObject(result);
			}
			return json;
		} catch (Exception e) {
			return getJson(1, "错误", "操作失败：" + e.getMessage());
		}
	}

	/**
	 * 获取验证票据
	 * 
	 * @return
	 */
	private static JSONObject getTicket() {
		ServiceBean bean = new ServiceBean(SpringUtil.getEnvProperty("IM_webservice_url_address"), "Login", "signon/password", SpringUtil.getEnvProperty("IM_webservice_url_namespace"));
		bean.addParameter(1, SpringUtil.getEnvProperty("IM_webservice_signon"));
		bean.addParameter(2, SpringUtil.getEnvProperty("IM_webservice_password"));
		try {
			// 返回类似这种格式：{"ReturnCode":0,"Description":"","DetailInfo":null}
			String result = String.valueOf(ServiceClient.getWebServiceResult(bean));
			return JSON.parseObject(result);
		} catch (Exception e) {
			logger.error("请求即时通讯失败", e);
			return getJson(1, "错误", "登录失败：" + e.getMessage());
		}
	}

	private static JSONObject getJson(int ReturnCode, String Description, String DetailInfo) {
		JSONObject json = new JSONObject();
		json.put("ReturnCode", ReturnCode);
		json.put("Description", Description);
		json.put("DetailInfo", DetailInfo);
		return json;
	}

}
