package com.kind.base.auth.cloudservice.auth;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;

/**
 * 删除资源
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "#delete_resources_service", description = "删除资源", powerCode = "", requireTransaction = true)
public class DeleteResourcesService extends DeleteResourcesInternalService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(DeleteResourcesService.class);

	private CommonService commonService = new CommonService();

	/**
	 * http://my.kind.com/auth/restful?action=delete_resources_service&TOKEN=&APP_ID=APP0002&RTYPE=99999&RIDS=TSTRID001|TSTRID002|TSTRID003
	 */
	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String token = dc.getRequestObject("TOKEN", "");
		String channelId = dc.getRequestObject("APP_ID", "");

		// String ret = commonService.doAccessValid(dc, token, channelId);
		// if (StrUtil.isNotBlank(ret)) {
		// return setFailInfo(dc, ret + "，资源删除失败！");
		// }

		dc.setReqeust("RTYPE", "00003");

		return super.handleData(hmLogSql, dc);
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
