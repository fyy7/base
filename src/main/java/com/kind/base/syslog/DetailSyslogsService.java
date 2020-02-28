package com.kind.base.syslog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

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
@Action(requireLogin = true, action = "#detail_syslogs", description = "系统日志详情", powerCode = "", requireTransaction = false)
public class DetailSyslogsService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		/*
		 * String logId = dc.getRequestString("LOGID"); String linkId = dc.getRequestString("LINKID"); String traceId = dc.getRequestString("TRACEID");
		 * 
		 * StringBuffer sbSql = new StringBuffer("select * from sys_logs a where "); List<Object> param = new ArrayList<Object>();
		 * 
		 * if (StrUtil.isNotBlank(linkId)) { sbSql.append(" a.LINKID = ?"); param.add(linkId); } else if (StrUtil.isNotBlank(logId)) { sbSql.append(" a.LOGID=?"); param.add(logId); } else if (StrUtil.isNotBlank(traceId)) { sbSql.append(" a.traceid = ?  or a.traceid2=?"); param.add(traceId); param.add(traceId); } else { return this.setFailInfo(dc, "非法日志查询！"); }
		 * 
		 * sbSql.append(" order by a.optime DESC"); // 详情的，从第一页，取50条 Pagination pdata = getPagination(hmLogSql, sbSql.toString(), 1, 50, param); dc.setBusiness("rows", pdata.getResultList()); dc.setBusiness("total", pdata.getTotalRows());
		 */

		StringBuffer sql = new StringBuffer();
		List<Object> param = new ArrayList<>();
		String pk_id = dc.getRequestString("LOGID");
		if (StrUtil.isNotEmpty(pk_id)) {
			sql.append("SELECT * FROM SYS_LOGS WHERE LOGID=?");
			param.add(pk_id);
		}

		Map<String, Object> beanMap = this.getSelectMap(hmLogSql, sql.toString(), param);
		dc.setBusiness("BEAN", beanMap);
		return setSuccessInfo(dc);

	}

	@Override
	public String pageAddress(DataContext dc) {
		return "syslog/sys_logs_show";
	}

	@Override
	public String verifyParameter(DataContext arg0) {
		return null;
	}
}
