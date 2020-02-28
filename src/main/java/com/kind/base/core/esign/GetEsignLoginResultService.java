package com.kind.base.core.esign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 获取e手签登录结果
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "#esign_get_login_result", description = "获取e手签登录结果", powerCode = "", requireTransaction = false)
public class GetEsignLoginResultService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(GetEsignLoginResultService.class);

	private String serviceName = "获取e手签登录结果";

	// 约定g_result=1时返回code字段，含义如下：
	// 0：登录成功。1：已登录。2：二维码失效。3：E手签验证通过，根据身份证获取用户信息失败。4：E手签验证通过，自动登录失败。5：等待E手签验证。6：异常
	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		try {
			String esignLoginId = dc.getSessionString(IEsignConstant.SESSIONKEY_ESIGN_LOGIN_ID);
			if (StrUtil.isNotBlank(esignLoginId) && dc.getSsoToken() != null) {
				setResultCode(dc, "1");
				return setSuccessInfo(dc, "已登录");
			}

			try {
				if (!this.getRedisClient().exists(esignLoginId)) {
					setResultCode(dc, "2");
					return setSuccessInfo(dc, "二维码已失效，请刷新重试");
				}
			} catch (Exception e) {
				setResultCode(dc, "5");
				return setSuccessInfo(dc, "未登录，等待E手签验证");
			}

			// 判断redis上是否存在esignLoginId对应的身份证号码，存在则表示该身份证经过E手签验证通过，可使用该身份证进行模拟登录
			String esignIdcardRdsKey = esignLoginId + "_IDCARD";
			if (this.getRedisClient().exists(esignIdcardRdsKey)) {
				String esignLoginIdIdCard = (String) this.getRedisClient().get(esignIdcardRdsKey);

				StringBuffer sql = new StringBuffer();
				List<Object> param = new ArrayList<>();
				sql.append("SELECT * FROM SYS_N_USERS  WHERE ISDEL = 0 AND PKI = ? ");
				param.add(esignLoginIdIdCard);

				Map<String, Object> beanMap = this.getSelectMap(hmLogSql, sql.toString(), param);
				if (beanMap == null) {
					setResultCode(dc, "3");
					return this.setSuccessInfo(dc, "E手签验证通过，根据身份证获取用户信息失败！");
				}

				String opaccount = beanMap.get("OPACCOUNT").toString();

				// 模拟登录
				dc.setReqeust("username", opaccount);
				String userLoginClass = SpringUtil.getEnvProperty(Constant.KISSO_LOGIN_SERVICE_CLASS);
				BaseActionService fs = (BaseActionService) ReflectUtil.newInstance(userLoginClass);
				if (fs == null || fs.handleMain(dc) == 0) {
					String msg = dc.getBusinessString(Constant.FRAMEWORK_G_MESSAGE);
					setResultCode(dc, "4");
					return setSuccessInfo(dc, "E手签验证通过，自动登录失败！" + msg);
				}

				this.getRedisClient().remove(esignIdcardRdsKey);
			} else {
				setResultCode(dc, "5");
				return setSuccessInfo(dc, "未登录，等待E手签验证");
			}
		} catch (Exception e) {
			String msg = String.format("[%s]异常！", serviceName);
			log.info(msg + System.getProperty("line.separator") + getStackTrace(e));

			return setFailInfo(dc, msg);
		}

		setResultCode(dc, "0");

		return setSuccessInfo(dc, "登录成功！");
	}

	private void setResultCode(DataContext dc, String code) {
		dc.setBusiness("code", code);
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
