/**
 * @Title: GotoResourceMain.java 
 * @Package com.kind.portal.service.resource 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月5日 下午4:39:00 
 * @version V1.0   
 */
package com.kind.base.auth.service.resource;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

/**
 * @author HUANGLEI
 *
 */
@Service
@Action(requireLogin = true, action = "#sys_resoure_main", description = "资源模块管理", powerCode = "resource.module", requireTransaction = false)

public class GotoResourceMain2 extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> arg0, DataContext dc) {
		dc.setBusiness("RID", dc.getRequestObject("RID"));
		dc.setBusiness("APPID", SpringUtil.getEnvProperty("app.system.appid"));
		dc.setBusiness("APPNAME", SpringUtil.getEnvProperty("kind.framework.common.params.system.title"));
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext arg0) {
		return "auth/resource/showresourcemain";
	}

	@Override
	public String verifyParameter(DataContext arg0) {
		return null;
	}

}
