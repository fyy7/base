package com.kind.base.blackip;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.Pagination;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * @author zhengjiayun
 *
 *         2019年8月14日
 */
@Service
@Action(requireLogin = true, action = "#blackip_query_list", description = "IP黑名单列表查询", powerCode = "", requireTransaction = false)
public class QueryBlackIpService extends BaseActionService {

	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(getClass());

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String black_ip = dc.getRequestString("BLACK_IP");
		String start_date = dc.getRequestString("START_DATE");
		String end_date = dc.getRequestString("END_DATE");

		List<Object> param = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("SELECT * FROM SYS_BLACK_IP WHERE  1=1 ");

		if (StrUtil.isNotEmpty(black_ip)) {
			sql.append(" AND BLACK_IP like ?");
			param.add("%" + black_ip + "%");
		}

		if (StrUtil.isNotBlank(start_date)) {
			param.add(this.convert(Date.class, start_date));
			sql.append(" and create_date >= ? ");
		}
		if (StrUtil.isNotBlank(end_date)) {
			param.add(this.convert(Date.class, end_date));
			sql.append(" and create_date <= ? ");
		}

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

		return setSuccessInfo(dc);

	}

	@Override
	public String pageAddress(DataContext dc) {
		return null;
	}
}
