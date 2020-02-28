package com.kind.base.attachment.cloudservice;

import java.util.ArrayList;
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
 * 附件列表查询
 * 
 * @author WANGXIAOYI
 *
 */
@Service
@Action(requireLogin = true, action = "#get_attachment_list", description = "附件列表查询", powerCode = "", requireTransaction = false)
public class QueryAttachmentListService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String token = dc.getRequestString("TOKEN");
		String appId = dc.getRequestString("APP_ID");
		String type = dc.getRequestString("TYPE");
		String name = dc.getRequestString("NAME");
		String beginTime = dc.getRequestString("BEGIN_TIME");
		String endTime = dc.getRequestString("END_TIME");

		/* 查询语句与条件 */
		List<Object> param = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("select AR.PK_UID, AR.APP_ID, AI.ATTACHMENT_TYPE, AR.ATTACHMENT_NAME, AI.ATTACHMENT_SUFFIX, AI.ATTACHMENT_SIZE, AI.SAVE_PATH, AI.CHECK_TYPE, AI.CHECK_VALUE, AR.CREATE_TIME, AR.CREATOR_ID, AR.COMMENTS from ATTACHMENT_RECORDS AR, ATTACHMENT_INFO AI where AR.ATTACHMENT_UID=AI.PK_UID ");
		sql.append(" and AI.STATUS <> 'TEMP' ");

		if (StrUtil.isNotEmpty(appId)) {
			sql.append(" and AR.APP_ID = ?");
			param.add(appId);
		}

		if (StrUtil.isNotEmpty(type) && !"99".equals(type)) {
			sql.append(" and AI.ATTACHMENT_TYPE = ?");
			param.add(type);
		}

		if (StrUtil.isNotEmpty(name)) {
			sql.append(" and AR.ATTACHMENT_NAME LIKE ? ");
			// param.add("%" + name + "%");
			ConvertSqlDefault.addLikeEscapeStr(sql, name, "%%", param);
		}

		if (StrUtil.isNotEmpty(beginTime)) {
			sql.append(ConvertSqlDefault.minDateSQL("AR.CREATE_TIME", beginTime));
		}

		if (StrUtil.isNotEmpty(endTime)) {
			sql.append(ConvertSqlDefault.maxDateSQL("AR.CREATE_TIME", endTime));
		}

		sql.append(" ORDER BY AR.CREATE_TIME ASC ");

		/* 执行查询 */
		String pageNo = dc.getRequestString("PAGE_NO");
		String pageSize = dc.getRequestString("PAGE_SIZE");
		if (StrUtil.isBlank(pageNo)) {
			pageNo = "1";
		}
		if (StrUtil.isBlank(pageSize)) {
			pageSize = "10";
		}
		Pagination pdata = this.getPagination(hmLogSql, sql.toString(), Integer.parseInt(pageNo), Integer.parseInt(pageSize), param);
		dc.setBusiness("rows", pdata.getResultList());
		dc.setBusiness("total", pdata.getTotalRows());

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
