package com.kind.base.dic.service.codecategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kind.common.automation.ActPostposition;
import com.kind.common.constant.ConCommon;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.Pagination;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年2月27日
 */
@Service
@Action(requireLogin = true, action = "#codecategory_query", description = "数据字典分类查询", powerCode = "dictionary.codecategory", requireTransaction = false)
public class QueryCodecategory extends BaseActionService {

	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(QueryCodecategory.class);

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String appId = dc.getRequestString("APP_ID");
		String cno = dc.getRequestString("CNO");
		String cname = dc.getRequestString("CNAME");

		List<Object> paramList = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("SELECT A.CNO,A.CNAME,A.NOTES,A.UUID,A.APP_ID, A.APP_ID AS APP_NAME FROM D_CODECATEGORY A WHERE 1=1 ");

		if (StrUtil.isNotEmpty(appId)) {
			sql.append(" AND A.APP_ID = ?");
			paramList.add(appId);
		}
		if (StrUtil.isNotEmpty(cno)) {
			// paramList.add("%" + cno + "%");
			sql.append(" AND CNO LIKE ?");
			ConvertSqlDefault.addLikeEscapeStr(sql, cno, "%%", paramList);
		}
		if (StrUtil.isNotEmpty(cname)) {
			// paramList.add("%" + cname + "%");
			sql.append(" AND CNAME LIKE ?");
			ConvertSqlDefault.addLikeEscapeStr(sql, cname, "%%", paramList);
		}
		sql.append(" ORDER BY CNO");

		/* 执行查询 */
		String page = dc.getRequestString("page");
		String pagesize = dc.getRequestString("rows");
		if (StrUtil.isBlank(page)) {
			page = "1";
		}
		if (StrUtil.isBlank(pagesize)) {
			pagesize = "10";
		}
		log.debug("page:" + page + ",pagesize:" + pagesize);
		Pagination pdata = this.getPagination(hmLogSql, sql.toString(), Integer.parseInt(page), Integer.parseInt(pagesize), paramList);

		List<Map<String, Object>> resultList = pdata.getResultList();

		dc.setBusiness(ConCommon.PAGINATION_ROWS, resultList);
		dc.setBusiness(ConCommon.PAGINATION_TOTAL, pdata.getTotalRows());
		// 代码转换
		JSONObject config = new JSONObject();
		config.put("type", "LISTTURNCODE");
		config.put("target", ConCommon.PAGINATION_ROWS);
		config.put("TURNCODE", JSONArray.parse("[{'field': 'APP_NAME','dmlx': 'COMMON.sys_appname','appid':'COMMON'}]"));
		JSONArray tmp = new JSONArray();
		tmp.add(config);
		ActPostposition.postpositionAction(dc, tmp);
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return null;
	}
}
