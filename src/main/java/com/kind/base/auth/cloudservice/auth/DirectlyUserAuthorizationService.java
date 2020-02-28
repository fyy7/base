package com.kind.base.auth.cloudservice.auth;

import java.util.HashMap;
import java.util.Map;

import com.kind.base.core.auth.ResourceService;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.ResultBean;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

/**
 * 用户授权
 * 
 * @author yanhang
 */
// 暂不对外提供API
// @Service
// @Action(requireLogin = false, action = "#directly_user_authorization_service", description = "", powerCode = "", requireTransaction = false) //powerCode: authorization.user.directly
public class DirectlyUserAuthorizationService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(DirectlyUserAuthorizationService.class);

	/**
	 * http://my.kind.com/portal/do?action=directly_user_authorization_service &TOKEN= &APP_ID=APP0001 &USER_ID=180319151351456900000000000000000001 &USER_DEPARTMENT_ID=aps00ld00000000000000000000000000000 &RESOURCE_ID=TESTRESOURCE0001 &CREATOR_ID=00000000-0000-0000-0000-000000000001 &CREATOR_DEPARTMENT_ID=CREATORDEPARTMENTID0001
	 */
	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		ResourceService resourceService = SpringUtil.getBean(ResourceService.class);

		Map<String, Object> rm = this.getSuccessBaseResultMap();
		try {
			// String token = dc.getRequestObject("TOKEN", "");
			// String channelId = dc.getRequestObject("APP_ID", "");
			String userId = dc.getRequestObject("USER_ID", "");
			String userDepartmentId = dc.getRequestObject("USER_DEPARTMENT_ID", "");
			String resourceId = dc.getRequestObject("RESOURCE_ID", "");
			String creatorId = dc.getRequestObject("CREATOR_ID", "");
			String creatorDepartmentId = dc.getRequestObject("CREATOR_DEPARTMENT_ID", "");

			// 接入合法性判断
			// if (!channelResourceBaseService.validateChannelSystem(channelId, token)) {
			// rm = this.getBaseResultMap(0, "非法接入！");
			// dc.setBusiness("json_content", getJsonStr(rm));
			// return setSuccessInfo(dc);
			// }

			ResultBean rb = resourceService.directlyUserAuthorization(userId, userDepartmentId, resourceId, creatorId, creatorDepartmentId);
			if (!rb.getSuccess()) {
				rm = this.getBaseResultMap(0, rb.getMsg());
			}
		} catch (Exception e) {
			String msg = "用户授权异常！";
			rm = this.getBaseResultMap(0, msg);
			log.info(msg + System.getProperty("line.separator") + getStackTrace(e));

			dc.getBusiness().putAll(rm);
			// throw new RuntimeException(msg + e.getMessage());
			return setFailInfo(dc);
		}

		// dc.setBusiness("json_content", getJsonStr(rm));
		dc.getBusiness().putAll(rm);

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
