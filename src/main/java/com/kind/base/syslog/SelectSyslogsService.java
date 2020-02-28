package com.kind.base.syslog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.Pagination;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * @author zhengjiayun
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = true, action = "#select_syslogs", description = "系统日志列表查询", powerCode = "", requireTransaction = false)
public class SelectSyslogsService extends BaseActionService {

	// private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(getClass());

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String opResult = dc.getRequestString("OP_RESULT");
		String app_id = dc.getRequestString("APP_ID");
		String optime1 = dc.getRequestString("OPTIME1");
		String optime2 = dc.getRequestString("OPTIME2");
		String opMillisecond = dc.getRequestString("OP_MILLISECOND");
		String loglevel = dc.getRequestString("LOGLEVEL");
		String order = dc.getRequestString("ORDER");
		String page = dc.getRequestString("page");
		String rows = dc.getRequestString("rows");
		if (StrUtil.isBlank(page)) {
			page = "1";
		}
		if (StrUtil.isBlank(rows)) {
			rows = "10";
		}

		List<Object> param = new ArrayList<Object>();
		StringBuffer sbSql = new StringBuffer("select * from sys_logs where srctype in(0,1) ");

		if (StrUtil.isNotBlank(app_id)) {
			param.add(app_id);
			sbSql.append(" and APP_ID = ? ");
		}

		if (StrUtil.isNotBlank(opResult)) {
			// param.add("%" + opResult + "%");
			sbSql.append(" and OP_RESULT like ? ");
			ConvertSqlDefault.addLikeEscapeStr(sbSql, opResult, "%%", param);
		}
		if (StrUtil.isNotBlank(loglevel)) {
			param.add(loglevel);
			sbSql.append(" AND LOGLEVEL = ? ");
		}
		if (StrUtil.isNotBlank(optime1)) {
			param.add(this.convert(Date.class, optime1));
			sbSql.append(" and optime >= ? ");
		}
		if (StrUtil.isNotBlank(optime2)) {
			param.add(this.convert(Date.class, optime2));
			sbSql.append(" and optime <= ? ");
		}
		if (StrUtil.isNotBlank(opMillisecond)) {
			param.add(this.convert(int.class, opMillisecond));
			sbSql.append(" and op_millisecond >= ? ");
		}
		if ("1".equals(order)) {
			sbSql.append("order by optime asc");
		} else {
			sbSql.append("order by optime desc");
		}
		Pagination pdata = getPagination(hmLogSql, sbSql.toString(), this.convert(int.class, page), this.convert(int.class, rows), param);

		dc.setBusiness("rows", pdata.getResultList());
		dc.setBusiness("total", pdata.getTotalRows());

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return null;
	}
}
