/**
 * @Title: GotoResourceMain.java 
 * @Package com.kind.portal.service.resource 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月5日 下午4:39:00 
 * @version V1.0   
 */
package com.kind.base.auth.service.entrust;

import java.util.Arrays;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * 权限委托管理
 * 
 * @author WangXiaoyis
 *
 */
@Service
@Action(requireLogin = true, action = "#sys_entrust_del", description = "权限委托管理之删除", powerCode = "resource.entrust", requireTransaction = true)

public class EntrustDel extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String privalue = dc.getRequestString("ROLEID");
		if (StrUtil.isEmpty(privalue)) {
			return setFailInfo(dc, "非法删除！");
		}

		// 删除委托信息
		String rolesEntrustSql = "delete from SYS_N_ROLES_ENTRUST where ROLEID=?";
		if (executeSql(hmLogSql, rolesEntrustSql, Arrays.asList(privalue)) != 1) {
			return setFailInfo(dc, "删除委托信息失败！");
		}

		// 删除委托用户
		String roleuserEntrustSql = "delete from SYS_N_ROLEUSER_ENTRUST where ROLEID =?";
		if (executeSql(hmLogSql, roleuserEntrustSql, Arrays.asList(privalue)) != 1) {
			return setFailInfo(dc, "删除委托用户失败！");
		}

		// 删除委托权限
		String rolerightsEntrustSql = "delete from SYS_N_ROLERIGHTS_ENTRUST where ROLEID =?";
		if (executeSql(hmLogSql, rolerightsEntrustSql, Arrays.asList(privalue)) != 1) {
			return setFailInfo(dc, "删除委托权限模块数据失败！");
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
