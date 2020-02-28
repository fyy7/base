package com.kind.base.user.service.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * @author zhengjiayun
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = true, action = "#get_my_power_menu_by_tree", description = "获取登录系统菜单", powerCode = "", requireTransaction = false)
public class GetMyPowerMenuService2 extends BaseActionService {

	// private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(getClass());

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// session取值
		// UserPO userpo = (UserPO) dc.getSession().get(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		String appId = dc.getRequestString("APP_ID");
		if (StrUtil.isBlank(appId)) {
			return this.setFailInfo(dc, "未获取应用ID号！");
		}
		List<Map<String, Object>> modules = (List<Map<String, Object>>) dc.getSessionObject("SESSION_POWER_GN_" + appId);

		JSONArray backJson = new JSONArray();
		JSONObject childlib = new JSONObject();
		for (Map<String, Object> tmp : modules) {
			JSONObject tmp_json = new JSONObject();
			tmp_json.put("id", tmp.get("RID"));
			tmp_json.put("name", tmp.get("TITLE"));
			tmp_json.put("index", tmp.get("ALLORDIDX"));
			if (childlib.getJSONArray(tmp.get("PARENTID").toString()) != null) {
				childlib.getJSONArray(tmp.get("PARENTID").toString()).add(tmp_json);
			} else {
				JSONArray tmp_arr = new JSONArray();
				tmp_arr.add(tmp_json);
				childlib.put(tmp.get("PARENTID").toString(), tmp_arr);
			}
			if (tmp.get("RLEVEL").toString().equals("2")) {
				backJson.add(tmp_json);
			}

		}
		for (int i = 0; i < backJson.size(); i++) {

			addChild(backJson.getJSONObject(i), childlib);
		}
		dc.setBusiness("menusData", backJson);
		return setSuccessInfo(dc);

	}

	private void addChild(JSONObject data, JSONObject childlib) {
		if (childlib.getJSONArray(data.getString("id")) != null) {
			JSONArray child_arr = childlib.getJSONArray(data.getString("id"));
			for (int i = 0; i < child_arr.size(); i++) {
				addChild(child_arr.getJSONObject(i), childlib);
			}
			data.put("childs", child_arr);
		}
	}

	@Override
	public String pageAddress(DataContext dc) {

		return "main_portal";

	}

}
