package com.kind.base.user.service.user;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConProperties;
import com.kind.common.utils.CodeSwitching;
import com.kind.common.utils.Encrypt;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年2月22日
 */
@Service
@Action(requireLogin = true, action = "#user_reset_password", description = "修改用户密码", powerCode = "register.user", requireTransaction = true)
public class ResetPassword extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String opno = dc.getRequestString("OPNO");
		if (StrUtil.isEmpty(opno)) {
			return this.setFailInfo(dc, "非法重置！");
		}

		// md5加密
		boolean useSimplePwd = false;
		String defaultPwdStr = CodeSwitching.getReidiosJsonObj("sys").getString("500");
		String pwd = null;
		if (useSimplePwd) {
			pwd = Encrypt.getAspMD5(defaultPwdStr, ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE);
		} else {
			StringBuffer opnoMD5 = new StringBuffer(Encrypt.getAspMD5(opno, ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE));
			String defaultPwdMD5 = Encrypt.getAspMD5(defaultPwdStr, ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE);
			pwd = Encrypt.getAspMD5(opnoMD5.append(defaultPwdMD5).toString(), ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE);
		}

		DataBean bean = new DataBean("SYS_N_USERS", "OPNO");

		bean.set("OPNO", opno);
		bean.set("PWD", pwd);

		if (this.update(hmLogSql, bean) == 0) {
			return this.setFailInfo(dc, "重置失败！");
		}

		return this.setSuccessInfo(dc, "重置成功！");
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
