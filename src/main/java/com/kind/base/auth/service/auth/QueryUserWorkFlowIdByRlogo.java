package com.kind.base.auth.service.auth;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.base.auth.service.auth.WorkFlawResourceService;
import com.kind.common.utils.CodeSwitching;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * @author
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = true, action = "#get_user_workflow_Id", description = "查询当前用户特定模块使用的工作流", powerCode = "", requireTransaction = false)
public class QueryUserWorkFlowIdByRlogo extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String rlogo=dc.getRequestString("ROLOG");
		if(StrUtil.isEmpty(rlogo)) {
			return setFailInfo(dc,"资源标识(ROLOG)不能为空！");
		}
		UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		String workflowID = WorkFlawResourceService.getWorkflowIdForRlogo(this,userpo, CodeSwitching.getSystemAppId(), rlogo);
		
		if(StrUtil.isEmpty(workflowID)) {
			return setFailInfo(dc,"未获取到工作流");
		}else {
			dc.setBusiness("FLOW_ID", workflowID);
			return setSuccessInfo(dc);
		}
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
