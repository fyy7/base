/**
 * @Title: GotoResourceMain.java 
 * @Package com.kind.portal.service.resource 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月5日 下午4:39:00 
 * @version V1.0   
 */
package com.kind.base.auth.service.entrust;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kind.HandleSpringCloudService;
import com.kind.common.constant.ConCommon;
import com.kind.common.service.MicroServerUrlManager;
import com.kind.common.utils.CodeSwitching;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.DbHelper;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.GenID;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 权限委托管理
 * 
 * @author WangXiaoyi
 *
 */
@Service
@Action(requireLogin = true, action = "#goto_sys_entrust_edit", description = "权限委托管理之获取回填数据", powerCode = "resource.entrust", requireTransaction = false)

public class GotoEntrustEdit extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		// ROLEID
		String roleId = dc.getRequestString("ROLEID");
		dc.setBusiness("FALG", roleId);
		DataBean bean = new DataBean("SYS_N_ROLES_ENTRUST", "ROLEID");
		Map<String, Object> rolesEntrustList = new HashMap<>(16);

		if (StrUtil.isEmpty(roleId)) {
			// 新增
			roleId = "R" + GenID.gen(19);
			rolesEntrustList.put("ROLEID", roleId);

		} else {
			bean.set("ROLEID", roleId);
			rolesEntrustList = getSelectMap(hmLogSql, "select * from SYS_N_ROLES_ENTRUST where ROLEID=? ", Arrays.asList(roleId));
		}
		rolesEntrustList.put("BEGINDATE", DateUtil.format((Date) rolesEntrustList.get("BEGINDATE"), "yyyy-MM-dd HH:mm:ss"));
		rolesEntrustList.put("ENDDATE", DateUtil.format((Date) rolesEntrustList.get("ENDDATE"), "yyyy-MM-dd HH:mm:ss"));

		Map<String, Object> rolesUserEntrustList = getSelectMap(hmLogSql, "select * from SYS_N_ROLEUSER_ENTRUST where ROLEID=?", Arrays.asList(roleId));

		dc.setBusiness("SYS_N_ROLES_ENTRUST_LIST", rolesEntrustList);
		dc.setBusiness("SYS_N_ROLESUSER_ENTRUST_LIST", rolesUserEntrustList);
		dc.setBusiness("ROLEID", roleId);

		// 获取勾选资源
		List<Map<String, Object>> rolerightsEntrustList = getSelectList(hmLogSql, "SELECT * FROM SYS_N_ROLERIGHTS_ENTRUST WHERE ROLEID=? ", Arrays.asList(roleId));

		dc.setBusiness("ROLERIGHTS_ENTRUST_BEAN", JSON.toJSON(rolerightsEntrustList));
		// 勾选资源转为json对象
		JSONObject jsonData = new JSONObject();
		for (int i = 0; i < rolerightsEntrustList.size(); i++) {
			jsonData.put(rolerightsEntrustList.get(i).get("RESOURCEID").toString(), true);

		}
		dc.setBusiness("ROLERIGHTS_ENTRUST_JSON", jsonData);
		// 获取已保存数据

		dc.setBusiness("ROLERIGHTS_ENTRUST_TYPE", getResourcesAppinfo(hmLogSql, Arrays.asList(roleId), "SYS_N_ROLERIGHTS_ENTRUST", dc));

		return setSuccessInfo(dc);
	}

	/**
	 * 获取已保存的系统、类型数据
	 * 
	 * @param hmLogSql
	 * @param addparamList
	 * @param type
	 * @return
	 */
	public static JSONArray getResourcesAppinfo(HashMap<String, String> hmLogSql, List<Object> addparamList, String type, DataContext dc) {
		List<Object> sqlList = new ArrayList<>();
		StringBuffer appSql = new StringBuffer("SELECT APP_ID,CHANNEL_RTYPE FROM SYS_RESOURCES A ");
		// StringBuffer appSql = new StringBuffer("SELECT APP_ID,CHANNEL_RTYPE FROM SYS_RESOURCES A WHERE exists(select 1 from portal_app_info where a.APP_ID=APP_ID and is_del=0) ");
		if (ConCommon.NUM_1.equals(ConCommon.NUM_2)) {
			switch (type) {
			case "SYS_N_ROLERIGHTS_ENTRUST":
				appSql.append(" and EXISTS(SELECT 1 FROM SYS_N_ROLERIGHTS_ENTRUST b where A.RID=B.RESOURCEID and B.ROLEID=? )  ");
				break;
			case "SYS_N_USERRIGHTS":
				appSql.append(" and EXISTS(SELECT 1 FROM SYS_N_USERRIGHTS b where A.RID=B.RESOURCEID and OPNO=? and DEPT_ID=? and CREATOR_DEPT_ID=? and CREATOR=? )  ");
				break;
			default:
				break;
			}

			sqlList = new ArrayList<>(addparamList);
		}
		appSql.append(" GROUP BY  APP_ID,CHANNEL_RTYPE");
		DbHelper dbHelper = new DbHelper();
		List<Map<String, Object>> appinfo = dbHelper.getSelectList(hmLogSql, appSql.toString(), sqlList);

		List<Map<String, Object>> newAppinfo = new ArrayList<Map<String, Object>>();


		HashMap<String, String> tmpMap = new HashMap<>(16);

		for (int i = 0; i < appinfo.size(); i++) {
			if (appinfo.get(i).get("APP_ID") == null || appinfo.get(i).get("CHANNEL_RTYPE") == null) {
				break;
			}
			if (tmpMap.get(appinfo.get(i).get("APP_ID")) == null) {
				tmpMap.put(appinfo.get(i).get("APP_ID").toString(), appinfo.get(i).get("CHANNEL_RTYPE").toString());
			} else {
				tmpMap.put(appinfo.get(i).get("APP_ID").toString(), tmpMap.get(appinfo.get(i).get("APP_ID").toString()) + "," + appinfo.get(i).get("CHANNEL_RTYPE"));
			}
		}

		JSONArray backJson = new JSONArray();
		Iterator<Map.Entry<String, String>> iter = tmpMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
			String key = entry.getKey();
			String val = entry.getValue();
			JSONObject tmpJson = new JSONObject();
			tmpJson.put("APPID", key.toString());
			tmpJson.put("APPNAME", CodeSwitching.getReidiosJsonDm("sys_appname", key.toString(), "COMMON"));
			tmpJson.put("CHANNEL_RTYPE",  val.toString());
			backJson.add(tmpJson);
		}
		if (backJson.size() == 0) {
			JSONObject tmpJson = new JSONObject();
			String key = "APP0001";
			tmpJson.put("APPID", key);
			tmpJson.put("APPNAME", CodeSwitching.getReidiosJsonDm("sys_appname", key, "COMMON"));
			tmpJson.put("CHANNEL_RTYPE", "");
			backJson.add(tmpJson);
		}
		return backJson;

	}

	@Override
	public String pageAddress(DataContext dc) {

		return "auth/entrust/modify_sys_n_roles_entrust";
	}

	@Override
	public String verifyParameter(DataContext dc) {

		return null;
	}

}
