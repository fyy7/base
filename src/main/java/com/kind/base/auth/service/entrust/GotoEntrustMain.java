/**
 * @Title: GotoResourceMain.java 
 * @Package com.kind.portal.service.resource 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月5日 下午4:39:00 
 * @version V1.0   
 */
package com.kind.base.auth.service.entrust;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * 权限委托管理
 * 
 * @author WangXiaoyi
 *
 */
@Service
@Action(requireLogin = true, action = "#sys_entrust_main", description = "权限委托管理", powerCode = "resource.entrust", requireTransaction = false)
public class GotoEntrustMain extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {

		return "auth/entrust/show_sys_n_roles_entrust";
	}

	@Override
	public String verifyParameter(DataContext dc) {

		return null;
	}

}
