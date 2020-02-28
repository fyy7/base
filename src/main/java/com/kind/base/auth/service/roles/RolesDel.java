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
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * @author HUANGLEI
 *
 */
@Service
@Action(requireLogin = true, action = "#sys_role_del", description = "角色删除", powerCode = "resource.role", requireTransaction = true)
public class RolesDel extends BaseActionService {
	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String privalue = dc.getRequestString("ROLEID");
		if (StrUtil.isEmpty(privalue)) {
			return this.setFailInfo(dc, "非法删除！");
		}

		// StringBuffer sb_sql = new StringBuffer("select RESOURCEID from SYS_N_ROLERIGHTS where ROLEID=?");
		// DBDYPO[] arr_resourceId = DBHelper.selectBySQL(this.hmLogSql, sb_sql.toString(), Arrays.asList( privalue });
		// if (arr_resourceId != null && arr_resourceId.length > 0) {
		// for (DBDYPO bean : arr_resourceId) {
		// if (0 == HandleRole.deleteUserRoleId(this.operateLog, dc, bean.get("RESOURCEID").toString(), privalue, (!(0 == PowerDAO.checkIsAdmin(ac) || 1 == PowerDAO.checkIsAdmin(ac)) ? ((UserBean) dc.getObjValue("SESSION_USER_BEAN")).getSysUserPO().getDeptId() : "DEPT_ID"), ((UserBean) dc.getObjValue("SESSION_USER_BEAN")).getSysUserPO().getOpNo())) {
		// this.setFailInfo(dc, "收回角色权限信息失败!");
		// return 0;
		// }
		// }
		// }

		// 删除角色
		int result = executeSql(hmLogSql, "delete from SYS_N_ROLES where ROLEID=?", Arrays.asList(privalue));
		if (result != 1) {
			return this.setFailInfo(dc, "删除角色失败！");
		}

		// 删除角色用户
		if (executeSql(hmLogSql, "delete from SYS_N_ROLEUSER where ROLEID =?", Arrays.asList(privalue)) != 1) {
			return this.setFailInfo(dc, "删除角色用户失败！");
		}

		// 删除角色部门
		if (executeSql(hmLogSql, "delete from SYS_N_ROLE_DEPARTMENT where ROLEID =?", Arrays.asList(privalue)) != 1) {
			return this.setFailInfo(dc, "删除角色部门失败！");
		}

		// 删除角色权限
		if (executeSql(hmLogSql, "delete from SYS_N_ROLERIGHTS where ROLEID =?", Arrays.asList(privalue)) != 1) {
			return this.setFailInfo(dc, "删除角色权限模块数据失败！");
		}
		// 权限回收
		ResourceService rs = SpringUtil.getBean(ResourceService.class);
		rs.recoveryRights();
		// 删除成功提示
		// fjkind.common.utils.SaveSysLog.success(dc, new fjkind.common.bean.LogPO(operateLog, "I", "C01", "SYS_N_ROLERIGHTS", privalue, "", "delete", "删除角色成功!"));

		return setSuccessInfo(dc, "删除成功");
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
