package com.kind.base.auth.service.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年4月19日
 */
@Service
@Action(requireLogin = true, action = "#workflow_select", description = "进入工作流择页面", powerCode = "", requireTransaction = false)
public class GotoWorkFlowSelect extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

	
		dc.setRequestToBusiness();

		
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "auth/resource/workflow_select";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
