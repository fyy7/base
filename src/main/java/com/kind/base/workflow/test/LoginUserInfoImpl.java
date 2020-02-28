package com.kind.base.workflow.test;

import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.utils.SpringUtil;
import com.kind.workflow.bo.WFlowUserBean;
import com.kind.workflow.interfaces.IWFlowLoginUserInfo;

/**
 * 获取当前用户实现类
 *
 */
public class LoginUserInfoImpl implements IWFlowLoginUserInfo {
	private static com.kind.framework.log.Logger logger = com.kind.framework.log.LogService.getLogger(LoginUserInfoImpl.class);

	@Override
	public WFlowUserBean getUserBean(DataContext dc) {

		logger.error("-------------------业务上实现获取当前用户信息处理----------------------------");

		WFlowUserBean sub = new WFlowUserBean();
		UserPO userpo = ((UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM)));

		sub.setUserId(userpo.getOpNo());
		sub.setUserName(userpo.getOpName());
		sub.setDeptId(userpo.getDeptId());
		sub.setDeptName(userpo.getDeptName());
		sub.setOrgId(userpo.getOrgId());
		sub.setOrgName(userpo.getOrgName());
		sub.setRemarks(" ");
		return sub;
	}

}
