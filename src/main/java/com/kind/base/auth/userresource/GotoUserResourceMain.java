/**
 * @Title: GotoResourceMain.java 
 * @Package com.kind.portal.service.resource 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月5日 下午4:39:00 
 * @version V1.0   
 */
package com.kind.base.auth.userresource;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author HUANGLEI
 *
 */
@Service
@Action(requireLogin = true, action = "#sys_user_resource_main", description = "用户授权管理", powerCode = "resource.user", requireTransaction = false)

public class GotoUserResourceMain extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> arg0, DataContext dc) {
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext arg0) {
		return "auth/userresource/sys_user_resource_main";
	}

	@Override
	public String verifyParameter(DataContext arg0) {
		return null;
	}

}
