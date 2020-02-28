package com.kind.base.dic.service.dcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.kind.common.utils.CodeSwitching;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年3月13日
 */
@Service
@Action(requireLogin = true, action = "#dcode_query", description = "数据字典管理树结构数据查询", powerCode = "dictionary.code", requireTransaction = false)
public class QueryDcodeTree extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String appId = dc.getRequestString("APP_ID");
		if (StrUtil.isEmptyOrUndefined(appId)) {
			appId = CodeSwitching.getSystemAppId();
		}
		List<Object> paramList = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("SELECT '0' AS CATEGORYNO,CNO,'ResourceTop' AS PARENTNO,CNO AS ALLORDIDX,CNAME,UUID FROM D_CODECATEGORY WHERE APP_ID=? UNION ALL SELECT CATEGORYNO,CNO,CASE WHEN (").append(ConvertSqlDefault.isnullSQL("PARENTNO", "' '")).append("= ' ' OR ").append(ConvertSqlDefault.isnullSQL("PARENTNO", "''")).append("='') THEN CATEGORYNO ELSE PARENTNO END AS PARENTNO,ALLORDIDX,CVALUE,UUID  FROM D_CODE WHERE APP_ID=? ORDER BY ALLORDIDX ");
		paramList.add(appId);
		paramList.add(appId);
		List<Map<String, Object>> list = this.getSelectList(hmLogSql, sql.toString(), paramList);
		if (list != null) {
			for (Map<String, Object> map : list) {
				map.put("id", map.get("ALLORDIDX"));
				String allordidx = map.get("ALLORDIDX").toString();
				if (allordidx.length() > 4) {
					map.put("pId", allordidx.substring(0, allordidx.length() - 5));
				} else {
					map.put("pId", map.get("PARENTNO"));
				}
				map.put("name", map.get("CNAME"));
			}
		}
		dc.setBusiness("D_CODE_LIST", JSON.toJSON(list));

		return setSuccessInfo(dc);
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
