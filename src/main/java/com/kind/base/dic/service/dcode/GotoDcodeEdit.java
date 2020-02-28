package com.kind.base.dic.service.dcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.utils.CodeSwitching;
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
@Action(requireLogin = true, action = "#dcode_edit", description = "参数管理修改页", powerCode = "dictionary.code_2", requireTransaction = false)
public class GotoDcodeEdit extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		List<Object> paramList = new ArrayList<Object>();
		String uuid = dc.getRequestString("UUID");
		String appId = dc.getRequestString("APP_ID");

		if (StrUtil.isEmpty(appId)) {
			appId = CodeSwitching.getSystemAppId();
		}

		if (StrUtil.isEmpty(uuid)) {
			// 新增
			dc.setBusiness("CMD", ConCommon.CMD_A);
		} else {
			// 修改
			String sql = "SELECT * FROM D_CODE WHERE UUID=?";
			paramList.add(uuid);
			Map<String, Object> bean = this.getSelectMap(hmLogSql, sql, paramList);
			dc.setBusiness("BEAN", bean);
			dc.setBusiness("CMD", "U");

		}

		paramList.clear();
		StringBuffer dcgSql = new StringBuffer("SELECT CNO,CNAME FROM D_CODECATEGORY WHERE APP_ID=?");
		paramList.add(appId);
		List<Map<String, Object>> dcgList = this.getSelectList(hmLogSql, dcgSql.toString(), paramList);
		dc.setBusiness("DCODECATEGORY", dcgList);

		dc.setBusiness("APP_ID", appId);

		return this.setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "dic/dcode/dcode_edit";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
