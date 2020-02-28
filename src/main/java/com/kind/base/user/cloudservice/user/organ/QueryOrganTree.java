package com.kind.base.user.cloudservice.user.organ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * @author
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = true, action = "#organ_tree_query", description = "获取机构树形结构", powerCode = "", requireTransaction = false)
public class QueryOrganTree extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String organId = dc.getRequestString("ORGID");

		UserPO userpo = (UserPO) dc.getSession().get(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		if (StrUtil.isEmpty(organId)) {

			organId = userpo.getOrgId();
		}

		dc.setBusiness("ORGAN_LIST", getOrganListTree(this, hmLogSql, organId).toJSONString());

		return this.setSuccessInfo(dc);
	}

	/**
	 * 根据机构id获取其子机构信息并返回机构树形结构
	 * 
	 * @param service
	 * @param hmLogSql
	 * @param orgid
	 * @return
	 */
	public static JSONArray getOrganListTree(BaseActionService service, HashMap<String, String> hmLogSql, String orgid) {
		List<Object> param = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("SELECT ORGANID,NAME,PARENTID,ALLORDIDX FROM SYS_ORGANIZATION_INFO");
		sql.append(" WHERE ISDEL=0 AND ALLORDIDX LIKE (SELECT ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" from SYS_ORGANIZATION_INFO where ISDEL=0 AND ORGANID=?) ORDER BY ALLORDIDX");
		param.add(orgid);
		List<Map<String, Object>> list = service.getSelectList(hmLogSql, sql.toString(), param);
		JSONArray arrJson = new JSONArray();
		if (list != null) {
			for (Map<String, Object> map : list) {
				map.put("id", map.get("ORGANID"));
				map.put("pId", map.get("PARENTID"));
				map.put("name", map.get("NAME"));
				arrJson.add(JSON.parseObject(JSON.toJSONString(map)));
			}
		}
		return arrJson;
	}

	@Override
	public String pageAddress(DataContext dc) {
		return null;
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
