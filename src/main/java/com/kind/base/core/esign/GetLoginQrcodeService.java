package com.kind.base.core.esign;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;

/**
 * 获取登录二维码
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "#esign_get_login_qrcode", description = "获取登录二维码", powerCode = "", requireTransaction = false)
public class GetLoginQrcodeService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(GetLoginQrcodeService.class);

	private String serviceName = "获取登录二维码";

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		try {
			String esignLoginId = dc.getSessionString(IEsignConstant.SESSIONKEY_ESIGN_LOGIN_ID); // 获取esign登录id
			/* if (StrUtil.isBlank(esignLoginId) || !this.getRedisClient().exists(esignLoginId)) { */ // 不重复生成
			int timeout = Integer.parseInt(SpringUtil.getEnvProperty(IEsignConstant.CFGKEY_ESIGN_VERIFY_TIMEOUT, "0")); // 扫码超时时间，秒

			esignLoginId = String.format("ESIGN_LOGIN_ID:%s_%s", UUID.randomUUID().toString().replaceAll("-", "").toUpperCase(), new Date().getTime()); // 本次扫码登录标识
			this.getRedisClient().set(esignLoginId, false, timeout * 1000); // 存储到redis。key：id，value：登录状态（true登录成功，false未登录）
			dc.setSession(IEsignConstant.SESSIONKEY_ESIGN_LOGIN_ID, esignLoginId); // 存储到session
			/* } */
			log.debug(String.format("=====> ESIGN_LOGIN_ID: %s, isLogin: %s", esignLoginId, this.getRedisClient().get(esignLoginId)));
			QrConfig config = new QrConfig(300, 300);
			config.setMargin(0);
			String content = SpringUtil.getEnvProperty(IEsignConstant.CFGKEY_ESIGN_VERIFY_URL, "") + "&esignLoginId=" + esignLoginId + "," + esignLoginId; // 把esignLoginId带回到url里，方便验证
			byte[] bytes = QrCodeUtil.generatePng(content, config);
			dc.setResponseViaOutputStream(true);
			dc.getResponseOutputStream().write(bytes);
			dc.getResponseOutputStream().flush();
		} catch (Exception e) {
			String msg = String.format("[%s]异常！", serviceName);
			log.info(msg + System.getProperty("line.separator") + getStackTrace(e));

			return setFailInfo(dc, msg);
		}

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
