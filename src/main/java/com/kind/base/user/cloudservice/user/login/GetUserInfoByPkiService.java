package com.kind.base.user.cloudservice.user.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "#cloud_get_user_info_by_pki", description = "根据用户身份证获取用户信息", powerCode = "", requireTransaction = false)
public class GetUserInfoByPkiService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(getClass());

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	private String serviceName = "根据用户身份证获取用户信息";

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		try {
			String pki = dc.getRequestString("PKI");

			// 身份证存储于user表里的pki字段
			String sql = " SELECT OPNO, OPNAME, PKI, PWD, CREATETIME, ACTIVETIME, LASTLOGINIP, LOGINTIMES, PREVLOGINIP, PREVLOGINTIME, OPLIMIT, ENABLED, REMARK, SECRECY, OPTYPE, ISCAKEY, OPACCOUNT, OPACCOUNT1, OPACCOUNT2, REMARK_2, MOBILE, ISDEL, PSN FROM SYS_N_USERS WHERE PKI = ? ";

			List<Object> paramList = new ArrayList<Object>();
			paramList.add(pki);

			Map<String, Object> info = this.getSelectMap(hmLogSql, sql, paramList);

			dc.setBusiness("value", info);
		} catch (Exception e) {
			String msg = String.format("[%s]异常！", serviceName);
			log.info(msg + System.getProperty("line.separator") + getStackTrace(e));

			return setFailInfo(dc, msg);
		}

		return setSuccessInfo(dc, serviceName + "成功！");
	}

	@Override
	public String pageAddress(DataContext dc) {
		return null;
	}
}
