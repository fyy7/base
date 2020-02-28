/**
 * @Title: GotoResourceMain.java 
 * @Package com.kind.portal.service.resource 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月5日 下午4:39:00 
 * @version V1.0   
 */
package com.kind.base.auth.service.roles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kind.common.utils.CodeSwitching;
import com.kind.framework.auth.SubjectAuthInfo;
import com.kind.framework.core.Action;
import com.kind.framework.core.DbHelper;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.GenID;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * @author HUANGLEI
 *
 */
@Service
@Action(requireLogin = true, action = "#goto_sys_role_edit", description = "角色管理编辑数据准备", powerCode = "resource.role", requireTransaction = false)

public class GotoRolesEdit extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		// ROLEID
		String roleid = dc.getRequestString("ROLEID");

		DataBean bean = new DataBean("SYS_N_ROLES", "ROLEID");
		Map<String, Object> sys_n_roles_list = new HashMap<>();

		if (StrUtil.isEmpty(roleid)) {
			// 新增

			roleid = "R" + GenID.gen(19);
			sys_n_roles_list.put("ROLEID", roleid); // 生成ID号

		} else {
			bean.set("ROLEID", roleid);
			sys_n_roles_list = getSelectMap(hmLogSql, "select * from SYS_N_ROLES  where ROLEID=? ", Arrays.asList(roleid));
		}

		dc.setBusiness("SYS_N_ROLES_LIST", sys_n_roles_list);
		dc.setBusiness("ROLEID", roleid);

		// 获取勾选资源
		List<Map<String, Object>> rolerightsList = getSelectList(hmLogSql, "SELECT * FROM SYS_N_ROLERIGHTS WHERE ROLEID=? ", Arrays.asList(roleid));

		dc.setBusiness("ROLERIGHTS_BEAN", JSON.toJSON(rolerightsList));
		// 勾选资源转为json对象
		JSONObject jsonData = new JSONObject();
		for (int i = 0; i < rolerightsList.size(); i++) {
			jsonData.put(rolerightsList.get(i).get("RESOURCEID").toString(), true);

		}
		dc.setBusiness("ROLERIGHTS_JSON", jsonData);
		// 获取已保存数据

		dc.setBusiness("ROLERIGHTS_TYPE", getResourcesAppinfo(hmLogSql, Arrays.asList(roleid), "SYS_N_ROLERIGHTS", dc, getSubjectAuthInfo(dc)));
		return this.setSuccessInfo(dc);
	}

	/**
	 * 获取已保存的系统、类型数据
	 * 
	 * @param hmLogSql
	 * @param addparamList
	 * @param type
	 * @return
	 */
	public static JSONArray getResourcesAppinfo(HashMap<String, String> hmLogSql, List<Object> addparamList, String type, DataContext dc, SubjectAuthInfo subjectAuthInfo) {
		UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty("app.user.session.id.param"));

		List<Object> sqlList = new ArrayList<>();
		StringBuffer appSql = new StringBuffer("SELECT APP_ID,CHANNEL_RTYPE FROM SYS_RESOURCES A ");
		// StringBuffer appSql = new StringBuffer("SELECT APP_ID,CHANNEL_RTYPE FROM SYS_RESOURCES A WHERE exists(select 1 from portal_app_info where a.APP_ID=APP_ID and is_del=0) ");
		if ("1".equals("2")) {
			switch (type) {
			case "SYS_N_ROLERIGHTS":
				appSql.append(" and EXISTS(SELECT 1 FROM SYS_N_ROLERIGHTS b where A.RID=B.RESOURCEID and B.ROLEID=? )  ");
				break;
			case "SYS_N_USERRIGHTS":
				appSql.append(" and EXISTS(SELECT 1 FROM SYS_N_USERRIGHTS b where A.RID=B.RESOURCEID and OPNO=? and DEPT_ID=? and CREATOR_DEPT_ID=? and CREATOR=? )  ");
				break;
			case "SYS_N_ROLES_DEPT_OFFICE_RIGHTS":
				appSql.append(" and EXISTS(SELECT 1 FROM SYS_N_ROLES_DEPT_OFFICE_RIGHTS b where A.RID=B.RESOURCEID and B.ROLEID=? )  ");
				break;

			default:
				break;
			}

			sqlList = new ArrayList<>(addparamList);
		}
		appSql.append(" GROUP BY  APP_ID,CHANNEL_RTYPE");
		DbHelper dbHelper = new DbHelper();
		List<Map<String, Object>> appinfo = dbHelper.getSelectList(hmLogSql, appSql.toString(), sqlList);
		// 后面有根据用户权限判断，这里不需要重新判断
		List<Map<String, Object>> newAppinfo = new ArrayList<Map<String, Object>>();
		JSONArray jsonArray = new JSONArray();
		// 筛选出正常状态的appid【exists(select 1 from portal_app_info where a.APP_ID=APP_ID and is_del=0)】
		/*
		 * HandleSpringCloudService hscs = SpringUtil.getBean("handleSpringCloudService", HandleSpringCloudService.class); JSONObject json = hscs.handleService(MicroServerUrlManager.getNPortalServerUrl(), "appconfig_list_all", dc); if (json.getShort(Constant.FRAMEWORK_G_RESULT) != 0) { jsonArray = json.getJSONArray("APP_LIST"); }
		 */
		List<Object> param = new ArrayList<Object>();

		// 假删除的，停用的状态不查询出来
		StringBuffer sql = new StringBuffer("SELECT APP_NAME, APP_ID, APP_TYPE FROM PORTAL_APP_INFO WHERE IS_DEL=0 and APP_STATUS=1 ");
		sql.append(" ORDER BY APP_ORDER");

		List<Map<String, Object>> pdata = dbHelper.getSelectList(hmLogSql, sql.toString(), param);

		// appinfo = newAppinfo;

		HashMap<String, String> tmpMap = new HashMap<String, String>();
		// 将数值汇总到json对象中，便于下一步处理
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


		// 非超级管理员过滤权限外的系统
		if (userpo.getOpType() > 0) {
			Set<String> appResources = subjectAuthInfo.getCurrentAppIds();
			JSONObject tmp = new JSONObject();
			// 数据转换，便于比较
			for (Object appid : appResources) {
				tmp.put(appid.toString(), true);
			}
			JSONArray tmpJson1 = new JSONArray();
			for (int i = 0; i < pdata.size(); i++) {
				if (tmp.getBooleanValue(pdata.get(i).get("APP_ID").toString())) {
					tmpJson1.add(pdata.get(i));
				}
			}
			jsonArray = tmpJson1;

			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject info = jsonArray.getJSONObject(i);

				JSONObject tmpJson = new JSONObject();
				if (info.isEmpty() || info.get("APP_ID") == null) {
					break;
				}
				String key = info.getString("APP_ID");
				tmpJson.put("APPID", info.get("APP_ID"));
				tmpJson.put("APPNAME", info.getString("APP_NAME").isEmpty() ? CodeSwitching.getReidiosJsonDm("sys_appname", key, "COMMON") : info.getString("APP_NAME"));
				tmpJson.put("CHANNEL_RTYPE", tmpMap.get(key));
				backJson.add(tmpJson);
			}

		} else {
			for (int i = 0; i < pdata.size(); i++) {

				JSONObject tmpJson = new JSONObject();
				if (pdata.get(i) == null || pdata.get(i).get("APP_ID") == null) {
					break;
				}
				String key = pdata.get(i).get("APP_ID").toString();
				tmpJson.put("APPID", pdata.get(i).get("APP_ID"));
				tmpJson.put("APPNAME", pdata.get(i).get("APP_NAME") == null ? CodeSwitching.getReidiosJsonDm("sys_appname", key, "COMMON") : pdata.get(i).get("APP_NAME"));
				tmpJson.put("CHANNEL_RTYPE", tmpMap.get(key));
				backJson.add(tmpJson);
			}
		}

		// 这里的代码会被代码回收 -废弃
		// if (backJson.size() == 0) {
		// JSONObject tmpJson = new JSONObject();
		// String key = "APP0001";
		// tmpJson.put("APPID", key);
		// tmpJson.put("APPNAME", CodeSwitching.getReidiosJsonDm("sys_appname", key));
		// tmpJson.put("CHANNEL_RTYPE", "default01");
		// backJson.add(tmpJson);
		// }

		return backJson;

	}

	@Override
	public String pageAddress(DataContext arg0) {

		return "auth/roles/modify_sys_n_roles";
	}

	@Override
	public String verifyParameter(DataContext arg0) {

		return null;
	}

}
