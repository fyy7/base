package com.kind.base.core.login;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.baomidou.kisso.common.encrypt.MD5;
import com.kind.common.utils.CodeSwitching;
import com.kind.framework.auth.AuthManager;
import com.kind.framework.auth.SubjectAuthInfo.SubjectType;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.GenID;
import com.kind.framework.utils.SpringUtil;

/**
 * @author ZHENGJIAYUN
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = false, action = "#login", description = "登录页面", powerCode = "", requireTransaction = false)
public class GotoLoginPageService extends BaseActionService {
	private static com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(GotoLoginPageService.class);

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		Object obj = dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));

		if (obj == null || ((UserPO) obj).getOpAccount() == null) {
			dc.setClearSsoToken(true);
			AuthManager.onLogout(dc, SubjectType.USER);

		}

		// 设置初始Cookie值，避免action解密失败
		if (dc.getCookie().get(SpringUtil.getEnvProperty("kisso.config.cookieName", "") + "_user") == null) {
			dc.setCookie(SpringUtil.getEnvProperty("kisso.config.cookieName", "") + "_user", MD5.toMD5(GenID.getUuid32()), Integer.MAX_VALUE);
		}
		// 放所有参数至业务参数中。
		dc.setRequestToBusiness();
		dc.setBusiness("VERIFY_FLAG", CodeSwitching.getReidiosJsonDm("sys", "5"));
		return this.setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		Object obj = dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));

		// 是否加dc.getRequestString("ReturnURL")这个值的判断
		if (obj == null) {
			return "login";
		} else {
			log.debug("。。。。已登录，直接进入门户主页面。。。。。" + obj.toString());
			return "forward:/do?action=goto_subsystem";
		}

	}
}
