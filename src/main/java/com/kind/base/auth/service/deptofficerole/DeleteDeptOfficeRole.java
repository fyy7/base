package com.kind.base.auth.service.deptofficerole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年3月26日
 */
@Service
@Action(requireLogin = true, action = "#dept_office_role_del", description = "部门、职务权限配置删除", powerCode = "resource.dept_person.power", requireTransaction = true)
public class DeleteDeptOfficeRole extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		List<Object> paramList = new ArrayList<Object>();
		String privalue = dc.getRequestString("ROLEID");
		if (StrUtil.isEmpty(privalue)) {
			return this.setFailInfo(dc, "非法删除！");
		}

		// 删除角色
		String deleteDeptOfficeSql = "DELETE FROM SYS_N_ROLES_DEPT_OFFICE where ROLEID=?";
		paramList.add(privalue);
		if (this.executeSql(hmLogSql, deleteDeptOfficeSql, paramList) != 1) {
			return this.setFailInfo(dc, "删除角色失败！");
		}

		// 删除角色权限
		String deleteRoleRightsSql = "DELETE FROM SYS_N_ROLES_DEPT_OFFICE_RIGHTS where ROLEID =?";
		if (this.executeSql(hmLogSql, deleteRoleRightsSql, paramList) != 1) {
			return this.setFailInfo(dc, "删除角色权限模块数据失败！");
		}

		// 删除成功提示
		return this.setSuccessInfo(dc, "删除成功！");
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
