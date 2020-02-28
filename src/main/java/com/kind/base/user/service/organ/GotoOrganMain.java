package com.kind.base.user.service.organ;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.base.user.cloudservice.user.organ.QueryOrganTree;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

/**
 * @author
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = true, action = "#organ_main", description = "组织结构", powerCode = "userpower.organ", requireTransaction = false)
public class GotoOrganMain extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(GotoOrganMain.class);

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		UserPO userpo = (UserPO) dc.getSession().get(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));

		dc.setBusiness("ORGAN_LIST", QueryOrganTree.getOrganListTree(this, hmLogSql, userpo.getOrgId() == null ? "00000000-0000-0000-1000-000000000000" : userpo.getOrgId()).toJSONString());
		dc.setBusiness("ORGANID", dc.getRequestString("ORGANID"));
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "user/organ/organ_main";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
