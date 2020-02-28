package com.kind.base.dic.service.dcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
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
@Action(requireLogin = true, action = "#dcode_option", description = "数据字典管理上级代码查询", powerCode = "dictionary.code", requireTransaction = false)
public class QueryDcodeOption extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String appId = dc.getRequestString("APP_ID");
		String categoryno = dc.getRequestString("CATEGORYNO");
		String cno = dc.getRequestString("CNO");

		List<Object> paramList = new ArrayList<>();
		// TODO edit by 20181031 去掉 AND CATEGORYNO=PARENTNO 不限制在一级 ， 可在除本节点外的其它节点都可
		StringBuffer sql = new StringBuffer("SELECT CNO,CVALUE,CATEGORYNO,PARENTNO FROM D_CODE A WHERE A.APP_ID=? ");
		paramList.add(appId);

		// 根据查询字段得到SQL
		if (StrUtil.isNotEmpty(categoryno)) {
			sql.append(" AND A.CATEGORYNO = ?");
			paramList.add(categoryno);
		}
		if (StrUtil.isNotEmpty(cno)) {
			sql.append(" AND A.CNO = ?");
			paramList.add(cno);
			// 可在除本节点外的其它节点都可
			sql.append(" AND NOT EXISTS (SELECT 1 FROM D_CODE B WHERE B.CNO=? AND B.CATEGORYNO=? AND ");
			sql.append(ConvertSqlDefault.charIndexSQL("A.ALLORDIDX", "B.ALLORDIDX")).append(">0)");
			paramList.add(cno);
			paramList.add(categoryno);
		}
		sql.append(" ORDER BY ALLORDIDX");

		List<Map<String, Object>> list = this.getSelectList(hmLogSql, sql.toString(), paramList);
		dc.setBusiness("D_CODE_LIST", JSON.parseArray(JSON.toJSONString(list)));
		dc.setBusiness("CATEGORYNO", categoryno);

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
