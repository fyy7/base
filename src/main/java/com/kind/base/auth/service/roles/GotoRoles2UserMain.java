/**
 * @Title: GotoResourceMain.java 
 * @Package com.kind.portal.service.resource 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月5日 下午4:39:00 
 * @version V1.0   
 */
package com.kind.base.auth.service.roles;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author HUANGLEI
 *
 */
@Service
@Action(requireLogin = true, action = "#goto_role_to_user", description = "角色授权界面", powerCode = "resource.role", requireTransaction = false)
public class GotoRoles2UserMain extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String roleid = dc.getRequestString("ROLEID");
		dc.setBusiness("ROLEID", roleid);
		Map<String, Object> arr_bean = getSelectMap(hmLogSql, "select * from SYS_N_ROLES where ROLEID=?", Arrays.asList(roleid));
		if (arr_bean == null) {
			return this.setFailInfo(dc, "数据错误！");
		}
		dc.setBusiness("ROLE_BEAN", arr_bean);
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext arg0) {
		return "auth/roles/goto_role_to_user";
	}

	@Override
	public String verifyParameter(DataContext arg0) {
		return null;
	}

}
