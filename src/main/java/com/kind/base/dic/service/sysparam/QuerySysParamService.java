package com.kind.base.dic.service.sysparam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
 * @author majiantao
 *
 *         2018年2月27日
 */
@Service
@Action(requireLogin = true, action = "#query_sysparam", description = "参数管理列表查询", powerCode = "dictionary.parameter", requireTransaction = false)
public class QuerySysParamService extends BaseActionService {

	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(QuerySysParamService.class);

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		// HandleSpringCloudService hscs = SpringUtil.getBean("handleSpringCloudService", HandleSpringCloudService.class);
		// JSONObject json = hscs.handleService("portal/portal", "sysparam_main", dc);
		// if (json.getShort(Constant.FRAMEWORK_G_RESULT) == 0) {
		// log.debug("-----获取微服务失败：" + json.getString(Constant.FRAMEWORK_G_MESSAGE));
		// } else {
		// log.debug("-----获取微服务成功：" + json.toJSONString());
		// }

		String appId = dc.getRequestString("APP_ID");
		String paramid = dc.getRequestString("PARAID");
		String paramname = dc.getRequestString("PARANAME");

		List<Object> param = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("SELECT P.PARAID,P.PARANAME,P.PARAVALUE,P.NOTES,P.APP_ID,P.APP_ID as APP_NAME,P.UUID FROM SYS_SYSPARM P WHERE  1=1 ");

		if (StrUtil.isNotEmpty(appId)) {
			sql.append(" AND P.APP_ID = ?");
			param.add(appId);
		}
		if (StrUtil.isNotEmpty(paramid)) {
			// 类型转换
			param.add(this.convert(int.class, paramid));
			sql.append(" AND PARAID = ?");
		}
		if (StrUtil.isNotEmpty(paramname)) {
			// param.add("%" + paramname + "%");
			sql.append(" AND PARANAME LIKE ?");
			ConvertSqlDefault.addLikeEscapeStr(sql, paramname, "%%", param);
		}
		sql.append(" ORDER BY PARAID");

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
		Pagination pdata = this.getPagination(hmLogSql, sql.toString(), Integer.parseInt(page), Integer.parseInt(pagesize), param);
		dc.setBusiness(ConCommon.PAGINATION_ROWS, pdata.getResultList());
		dc.setBusiness(ConCommon.PAGINATION_TOTAL, pdata.getTotalRows());
		// 执行转码
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
