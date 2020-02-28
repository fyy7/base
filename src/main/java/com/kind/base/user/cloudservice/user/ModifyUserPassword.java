package com.kind.base.user.cloudservice.user;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.kind.common.utils.RsaUtils;
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
 *         2018年3月16日
 */
@Service
@Action(requireLogin = true, action = "#modify_password", description = "密码修改保存密码", powerCode = "", requireTransaction = true)
public class ModifyUserPassword extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		UserPO userpo = (UserPO) dc.getSession().get(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		dc.setBusiness("ACCOUNT", userpo.getOpAccount());
		dc.setBusiness("LOGIN_NAME", userpo.getOpName());

		// 公匙
		try {
			dc.setBusiness("PUBLIC_KEY", RsaUtils.getPublicKeyString(RsaUtils.getPublicKey()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		JSONObject appParamJsonObj = new JSONObject();// CodeSwitching.getReidiosJsonObj("sys","APP0001");
		// 是否开启密码强度验证 1验证，0不验证
		dc.setBusiness("STRONG_PWD", StrUtil.nullToDefault(appParamJsonObj.getString("7"), "1"));

		// 秘密强度验证正则表达式
		dc.setBusiness("PWD_REGEXP", StrUtil.nullToDefault(appParamJsonObj.getString("21"), "^(?=.*[a-zA-Z])(?=.*\\d).{6,20}$"));

		// 密码强度验证提示语
		dc.setBusiness("PWD_MSG", StrUtil.nullToDefault(appParamJsonObj.getString("22"), "密码必须包含字母、数字，且长度为6-20"));

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "user/user/modify_password";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
