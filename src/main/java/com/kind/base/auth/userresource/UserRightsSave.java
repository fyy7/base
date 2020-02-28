/**
 * @Title: GotoResourceMain.java 
 * @Package com.kind.portal.service.resource 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月5日 下午4:39:00 
 * @version V1.0   
 */
package com.kind.base.auth.userresource;

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
import com.kind.framework.utils.GenID;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * @author HUANGLEI
 *
 */
@Service
@Action(requireLogin = true, action = "#sys_user_resource_save", description = "用户授权保存", powerCode = "resource.user", requireTransaction = true)

public class UserRightsSave extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String rolerightsaddlist = dc.getRequestString("RoleRights_Add");
		String rolerightsdellist = dc.getRequestString("RoleRights_Del");
		String opno = dc.getRequestString("OPNO");

		String deptid = dc.getRequestString("DEPTID");
		UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));

		// 保存角色权限
		String arrRoleRightsAdd[], arrRoleRightsDel[];
		DataBean bean1 = new DataBean("SYS_N_USERRIGHTS", "OPNO,RESOURCEID,DEPT_ID,CREATOR,CREATOR_DEPT_ID");
		bean1.set("OPEMAN", userpo.getOpName());
		bean1.set("OPETIME", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())); // 赋建立时间
		bean1.set("CREATOR", userpo.getOpNo());
		bean1.set("OPNO", opno);
		bean1.set("DEPT_ID", deptid);

		if (userpo.getOpType() < 2) {
			bean1.set("CREATOR_DEPT_ID", "DEPT_ID");
		} else {
			bean1.set("CREATOR_DEPT_ID", userpo.getDeptId());
		}

		if (!StrUtil.isEmpty(rolerightsdellist)) {
			arrRoleRightsDel = rolerightsdellist.split(";");

			for (String s_resourceId : arrRoleRightsDel) {
				bean1.set("RESOURCEID", s_resourceId);
				int i = executeSql(hmLogSql, "delete FROM SYS_N_USERRIGHTS where OPNO=? ", Arrays.asList(opno));
				if (i != 1) {
					return this.setFailInfo(dc, "取消权限信息失败！");
				}
			}

		}

		if (!StrUtil.isEmpty(rolerightsaddlist)) {
			arrRoleRightsAdd = rolerightsaddlist.split(";");
			for (int i = 0; i < arrRoleRightsAdd.length; i++) {
				bean1.set("RESOURCEID", arrRoleRightsAdd[i]); // 资源ID
				bean1.set("PID", GenID.gen(32)); //
				executeSql(hmLogSql, "delete FROM SYS_N_USERRIGHTS where OPNO=? and RESOURCEID=? and DEPT_ID=? and CREATOR=? and CREATOR_DEPT_ID=?", Arrays.asList(bean1.get("OPNO"), arrRoleRightsAdd[i], bean1.get("DEPT_ID"), bean1.get("CREATOR"), bean1.get("CREATOR_DEPT_ID")));
				if (insert(hmLogSql, bean1) != 1) {
					return this.setFailInfo(dc, "保存角色权限信息失败！");
				}
			}
		}
		// 权限回收
		ResourceService rs = SpringUtil.getBean(ResourceService.class);
		rs.recoveryRights();
		return this.setSuccessInfo(dc, "保存成功！");
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
