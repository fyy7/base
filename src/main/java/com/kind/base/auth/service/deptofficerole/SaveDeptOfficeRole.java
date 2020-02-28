package com.kind.base.auth.service.deptofficerole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年3月26日
 */
@Service
@Action(requireLogin = true, action = "#dept_office_role_save", description = "部门、职务权限配置保存", powerCode = "resource.dept_person.power", requireTransaction = true)
public class SaveDeptOfficeRole extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String rolerightsaddlist = dc.getRequestString("RoleRights_Add");
		String rolerightsdellist = dc.getRequestString("RoleRights_Del");

		UserPO userpo = (UserPO) dc.getSession().get(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));

		DataBean bean = this.getBean(dc, "SYS_N_ROLES_DEPT_OFFICE", "ROLEID");
		String roleid = bean.getString("ROLEID");

		Object dataType = bean.get("DEPT_TYPE");
		if (dataType == null) {
			bean.set("DEPT_TYPE", "");
		}
		Object office = bean.get("OFFICE_TYPE");
		if (office == null) {
			bean.set("OFFICE_TYPE", "");
		}
		bean.set("UPDATE_AT", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

		boolean isAdd = false;

		// 保存角色信息
		int result;
		List<Object> param = new ArrayList<Object>();
		String selectCountSql = "Select count(1) from SYS_N_ROLES_DEPT_OFFICE where ROLEID=?";
		param.add(roleid);
		String count = String.valueOf(this.getOneFiledValue(hmLogSql, selectCountSql, param));

		if (ConCommon.NUM_0.equals(count)) {
			isAdd = true;
			bean.set("CREATE_OPNO", userpo.getOpNo());
			bean.set("CREATE_OPNAME", userpo.getOpName());
			bean.set("CREATE_AT", bean.get("UPDATE_AT"));
			result = this.insert(hmLogSql, bean);
		} else { // 修改
			result = this.update(hmLogSql, bean);
		}

		if (result != 1) {
			return this.setFailInfo(dc, "保存角色信息失败！");
		}

		// 保存角色权限
		DataBean beanRolerights = new DataBean("SYS_N_ROLES_DEPT_OFFICE_RIGHTS", "ROLEID,RESOURCEID");
		beanRolerights.set("ROLEID", roleid);

		// 处理角色中权限资源的删除
		if (StrUtil.isNotEmpty(rolerightsdellist)) {
			String[] arrRoleRightsDel = rolerightsdellist.split(";");
			for (String tempResourceId : arrRoleRightsDel) {
				beanRolerights.set("ROLEID", roleid);
				beanRolerights.set("RESOURCEID", tempResourceId);

				// 删除角色中的权限资源ID
				if (this.delete(hmLogSql, beanRolerights) != 1) {
					return this.setFailInfo(dc, "取消角色权限信息失败！");
				}
			}
		}

		// 增加的权限资源
		if (StrUtil.isNotEmpty(rolerightsaddlist)) {
			String[] arrRoleRightsAdd = rolerightsaddlist.split(";");
			for (int i = 0; i < arrRoleRightsAdd.length; i++) {
				beanRolerights.set("ROLEID", roleid);
				beanRolerights.set("RESOURCEID", arrRoleRightsAdd[i]);
				executeSql(hmLogSql, "delete FROM SYS_N_ROLES_DEPT_OFFICE_RIGHTS where ROLEID=? and RESOURCEID=?", Arrays.asList(roleid, arrRoleRightsAdd[i]));
				if (this.insert(hmLogSql, beanRolerights) != 1) {
					return this.setFailInfo(dc, "保存角色权限信息失败！");
				}
			}
		}

		if (!isAdd) {
			// 调用全局权限回收

		}

		// 保存结果提示
		if (result == 1) {
			return this.setSuccessInfo(dc, "保存成功！");
		} else {
			return this.setFailInfo(dc, "保存失败！");
		}
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
