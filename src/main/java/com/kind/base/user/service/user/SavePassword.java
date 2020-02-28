package com.kind.base.user.service.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.kind.common.constant.ConCommon;
import com.kind.common.constant.ConProperties;
import com.kind.common.stream.MqBean;
import com.kind.common.utils.CodeSwitching;
import com.kind.common.utils.Encrypt;
import com.kind.common.utils.RsaUtils;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataBean;
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
@Action(requireLogin = true, action = "#save_password", description = "修改密码", powerCode = "", requireTransaction = true)
public class SavePassword extends BaseActionService {

	private com.kind.framework.log.Logger logger = com.kind.framework.log.LogService.getLogger(getClass());

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		List<MqBean> mqList = new ArrayList<>();
		MqBean mqBean = new MqBean();

		List<Object> paramList = new ArrayList<Object>();

		UserPO userpo = (UserPO) dc.getSession().get(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		String opno = userpo.getOpNo();
		// 密码密文解密
		String depwdmi = RsaUtils.decryptBase64String(dc.getRequestString("PWDMI"));
		String depwdmiOld = RsaUtils.decryptBase64String(dc.getRequestString("OLD_PWDMI"));

		// 采用原文处理
		// String depwdmi = dc.getRequestString("PWDMI");
		// String depwdmiOld = dc.getRequestString("OLD_PWDMI");

		logger.debug("depwdmi=" + depwdmi + ",     depwdmiOld=" + depwdmiOld);

		JSONObject appParamJsonObj = CodeSwitching.getReidiosJsonObj("sys");
		String pwd = null;

		// 加密方式 (OPNO的MD5 + PWD的MD5)的MD5
		StringBuffer opnoMd5 = new StringBuffer(Encrypt.getAspMD5(opno, ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE));
		String oldPwdMd5 = Encrypt.getAspMD5(depwdmiOld, ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE);
		pwd = Encrypt.getAspMD5(opnoMd5.append(oldPwdMd5).toString(), ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE);

		String checkOldPwdSql = "SELECT COUNT(1) NUM FROM SYS_N_USERS WHERE OPNO=? AND PWD=?";
		paramList.add(opno);
		paramList.add(pwd);
		String checkOldPwdResutl = String.valueOf(this.getOneFiledValue(hmLogSql, checkOldPwdSql, paramList));
		if (!ConCommon.NUM_1.equals(checkOldPwdResutl)) {
			return this.setFailInfo(dc, "用户认证失败(原密码不对？)，请检查！");
		}

		// 秘密强度或者格式验证
		boolean needCheckPwdStrong = ConCommon.NUM_1.equals(StrUtil.nullToDefault(appParamJsonObj.getString("7"), "1"));
		if (needCheckPwdStrong && StrUtil.isNotEmpty(appParamJsonObj.getString("21"))) {
			String pwdRegex = appParamJsonObj.getString("21");
			if (!depwdmi.matches(pwdRegex)) {
				return this.setFailInfo(dc, appParamJsonObj.getString("22"));
			}
		} else {
			if (StrUtil.isEmpty(depwdmi)) {
				return this.setFailInfo(dc, "密码不能为空！");
			}
		}

		// 改为与.net那边的即时通讯的加密算法一致
		opnoMd5 = new StringBuffer(Encrypt.getAspMD5(opno, ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE));
		String newPwdMd5 = Encrypt.getAspMD5(depwdmi, ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE);
		pwd = Encrypt.getAspMD5(opnoMd5.append(newPwdMd5).toString(), ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE);
		DataBean bean = new DataBean("SYS_N_USERS", "OPNO");
		bean.set("OPNO", opno);
		bean.set("PWD", pwd);

		mqBean = new MqBean(bean, MqBean.CMD_U, null);
		mqList.add(mqBean);

		if (this.update(hmLogSql, bean) != 1) {
			return this.setFailInfo(dc, "修改失败！");
		}

		return this.setSuccessInfo(dc, "修改成功！");
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
