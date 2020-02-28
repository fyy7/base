package com.kind.base.user.cloudservice.user.organ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = false, action = "#organ_tree_query_public", description = "获取全部机构树形结构（公共）", powerCode = "", requireTransaction = false)
public class QueryOrganTreePublic extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		List<Object> param = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer(" SELECT ORGANID, NAME, PARENTID, ALLORDIDX FROM SYS_ORGANIZATION_INFO WHERE ISDEL = 0 AND NLEVEL > 1 ");
		List<Map<String, Object>> list = this.getSelectList(hmLogSql, sql.toString(), param);
		JSONArray arrJson = new JSONArray();
		if (list != null) {
			for (Map<String, Object> map : list) {
				map.put("id", map.get("ORGANID"));
				map.put("pId", map.get("PARENTID"));
				map.put("name", map.get("NAME"));
				arrJson.add(JSON.parseObject(JSON.toJSONString(map)));
			}
		}

		dc.setBusiness("ORGAN_LIST", arrJson.toJSONString());

		return this.setSuccessInfo(dc);
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
