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

import org.springframework.stereotype.Service;

import com.kind.base.core.auth.ResourceService;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * @author HUANGLEI
 *
 */
@Service
@Action(requireLogin = true, action = "#sys_role_save", description = "角色保存", powerCode = "resource.role", requireTransaction = true)

public class RolesSave extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String rolerightsaddlist = dc.getRequestString("RoleRights_Add");
		String rolerightsdellist = dc.getRequestString("RoleRights_Del");

		DataBean bean = getBean(dc, "SYS_N_ROLES", "ROLEID");
		String roleid = String.valueOf(bean.get("ROLEID"));
		String rolename = String.valueOf(bean.get("ROLENAME"));

		UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));

		if (getSelectMap(hmLogSql, "select ROLEID from SYS_N_ROLES where ROLEID<>? and ROLENAME =? and CREATOR=?", Arrays.asList(roleid, rolename, userpo.getOpNo())) != null) {
			return this.setFailInfo(dc, "角色名称已经存在，请重新命名！");
		}

		bean.set("OPETIME", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())); // 赋建立时间

		if (userpo.getOpType() < 2) {
			// 机构管理员
			bean.set("DEPT_ID", "DEPT_ID");
		} else {
			// 当前部门的
			bean.set("DEPT_ID", userpo.getDeptId());
		}

		// 保存角色信息
		int result;
		if (getSelectMap(hmLogSql, "select 1 from SYS_N_ROLES where ROLEID=?", Arrays.asList(roleid)) == null) {
			bean.set("CREATOR", userpo.getOpNo());
			bean.set("OPEMAN", userpo.getOpName());
			result = insert(hmLogSql, bean);
		} else { // 修改
			result = update(hmLogSql, bean);
		}

		if (result != 1) {
			return this.setFailInfo(dc, "保存角色信息失败！");
		}

		// 保存角色权限
		DataBean bean_rolerights = new DataBean("SYS_N_ROLERIGHTS", "ROLEID,RESOURCEID");
		bean_rolerights.set("OPEMAN", userpo.getOpName());
		bean_rolerights.set("OPETIME", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())); // 赋建立时间

		// 处理角色中权限资源的删除
		if (!StrUtil.isEmpty(rolerightsdellist)) {
			String arrRoleRightsDel[] = rolerightsdellist.split(";");
			for (String s_resourceId : arrRoleRightsDel) {
				bean_rolerights.set("ROLEID", roleid);
				bean_rolerights.set("RESOURCEID", s_resourceId);

				// 删除角色中的权限资源ID
				if (executeSql(hmLogSql, "delete FROM SYS_N_ROLERIGHTS where ROLEID=? and RESOURCEID=?", Arrays.asList(roleid, s_resourceId)) != 1) {
					return this.setFailInfo(dc, "取消角色权限信息失败！");
				}
			}
		}

		// 增加的权限资源
		if (!StrUtil.isEmpty(rolerightsaddlist)) {
			String arrRoleRightsAdd[] = rolerightsaddlist.split(";");
			for (int i = 0; i < arrRoleRightsAdd.length; i++) {
				bean_rolerights.set("ROLEID", roleid);
				bean_rolerights.set("RESOURCEID", arrRoleRightsAdd[i]); // 资源ID
				executeSql(hmLogSql, "delete FROM SYS_N_ROLERIGHTS where ROLEID=? and RESOURCEID=?", Arrays.asList(roleid, arrRoleRightsAdd[i]));
				if (insert(hmLogSql, bean_rolerights) != 1) {
					return this.setFailInfo(dc, "保存角色权限信息失败！");
				}
			}
		}

		// 保存结果提示
		if (result == 1) {
			// 权限回收
			ResourceService rs = SpringUtil.getBean(ResourceService.class);
			rs.recoveryRights();
			return this.setSuccessInfo(dc, "保存成功！");
		} else {
			return this.setFailInfo(dc, "保存失败！");
		}
	}

	@Override
	public String pageAddress(DataContext arg0) {
		return null;
	}

	@Override
	public String verifyParameter(DataContext arg0) {
		return null;
	}

}
