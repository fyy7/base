package com.kind.base.core.esign;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;
import com.kinsec.PKIInterface;

import cn.hutool.core.util.StrUtil;

/**
 * 校验e手签签名数据，把验证后得到的身份证存储到Redis上，key：esignLoginId+"_"+IDCARD
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "#esign_verify_data", description = "校验e手签签名数据", powerCode = "", requireTransaction = false)
public class VerifyEsignDataService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(VerifyEsignDataService.class);

	private String serviceName = "校验e手签签名数据";

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		try {
			String data = dc.getRequestString("Data");
			if (StrUtil.isBlank(data)) {
				dc.setBusiness("json_content", "未收到Data数据");
				return setSuccessInfo(dc);
			}

			String esignLoginId = dc.getRequestString("esignLoginId");
			if (StrUtil.isBlank(esignLoginId)) {
				dc.setBusiness("json_content", "未收到esignLoginId");
				return setSuccessInfo(dc);
			}

			if (!this.getRedisClient().exists(esignLoginId)) {
				dc.setBusiness("json_content", "二维码已失效，请刷新重试");
				return setSuccessInfo(dc);
			}

			log.debug("e手签签名校验相关数据：" + data);

			String[] dataArray = data.split("#"); // 根据#分割得到数组arr，将arr[0], arr[1], arr[2]传到pki方法校验(arr[0]是签名原文，arr[1]是签名后的值，arr[2]是证书)
			String strSrc = dataArray[0];
			String strB64Sign = dataArray[1];
			String strB64Cert = dataArray[2];

			String host = SpringUtil.getEnvProperty(IEsignConstant.CFGKEY_ESIGN_PKI_HOST, "");
			int port = Integer.parseInt(SpringUtil.getEnvProperty(IEsignConstant.CFGKEY_ESIGN_PKI_PORT, "0"));

			// 接口实例
			PKIInterface myobj = new PKIInterface(host, port);
			byte[] by = strSrc.getBytes("GBK");

			String ss = myobj.signRawData(by);
			log.debug("PKI计算签名值：" + ss);
			String cert = myobj.getLastSignCert();
			log.debug("签名证书：" + cert);

			// 验证数字签名
			boolean bRet = myobj.verifySign(by, strB64Cert, strB64Sign);
			if (bRet) {
				log.debug("验证成功");
				log.debug("姓名：" + myobj.getCertCN(strB64Cert));
				log.debug("身份证：" + myobj.getCertIdentificationNumber(strB64Cert));

				String idCardNo = myobj.getCertIdentificationNumber(strB64Cert);

				String esignIdcardRdsKey = esignLoginId + "_IDCARD";
				this.getRedisClient().set(esignIdcardRdsKey, idCardNo, Integer.parseInt(SpringUtil.getEnvProperty(IEsignConstant.CFGKEY_ESIGN_VERIFY_TIMEOUT, "0")) * 1000);
			} else {
				String lastError = myobj.getLastError();
				log.debug("验证失败:" + lastError);

				dc.setBusiness("json_content", lastError);

				return setSuccessInfo(dc);
			}
		} catch (Exception e) {
			String msg = String.format("[%s]异常！", serviceName);
			log.info(msg + System.getProperty("line.separator") + getStackTrace(e));

			dc.setBusiness("json_content", msg);

			return setSuccessInfo(dc, msg);
		}

		dc.setBusiness("json_content", "1");

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "common/json";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

}
