package com.kind.base.auth.service.auth;

import java.util.Arrays;

import com.kind.framework.core.DbHelper;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.utils.GenID;

import cn.hutool.core.date.DateTime;

public class PrivilegeRecovery {
	private static boolean isrun = false;
	private static DbHelper help;
	private static org.springframework.jdbc.core.JdbcTemplate JdbcTemplate;

	public static boolean recoveryRights() {
		if (isrun) {
			return isrun;
		}
		if (help == null) {
			help = new DbHelper();
			JdbcTemplate = help.getJdbcTemplate(0);
		}
		isrun = true;

		DataBean bean = new DataBean("SYS_RECOVERY_RIGHTS_TIME", "TID");
		bean.set("TID", GenID.gen(20));
		bean.set("BEGINTIME", DateTime.now());
		help.insert(null, bean);

		// 来源于角色权限的非法权限验证语句与删除语句(需要改善增加上级部门授权合法的判断，当前是B.DEPT_ID=C.DEPT_ID)
		StringBuffer selRoleRightsSql = new StringBuffer("SELECT COUNT(1) AS NUM FROM SYS_N_ROLERIGHTS A,SYS_N_ROLES B WHERE A.ROLEID=B.ROLEID AND NOT EXISTS(SELECT 1 FROM V_USER_RESOURCES C WHERE  B.CREATOR=C.OPNO AND A.RESOURCEID=C.RID AND (B.DEPT_ID=C.DEPT_ID OR C.DEPT_ID='DEPT_ID') )");
		StringBuffer delRoleRightsSql = new StringBuffer("DELETE FROM SYS_N_ROLERIGHTS from SYS_N_ROLERIGHTS,SYS_N_ROLES B  WHERE SYS_N_ROLERIGHTS.ROLEID=B.ROLEID  AND    NOT EXISTS(SELECT 1 FROM V_USER_RESOURCES C WHERE B.CREATOR=C.OPNO AND SYS_N_ROLERIGHTS.ROLEID=B.ROLEID AND SYS_N_ROLERIGHTS.RESOURCEID=C.RID AND (B.DEPT_ID=C.DEPT_ID OR C.DEPT_ID='DEPT_ID') )");

		// 来源于用户权限管理的非法权限验证语句与删除语句
		StringBuffer selUserRightSql = new StringBuffer("SELECT COUNT(1) AS NUM FROM SYS_N_USERRIGHTS A WHERE NOT EXISTS(SELECT 1 FROM V_USER_RESOURCES B WHERE A.CREATOR=B.OPNO AND A.RESOURCEID=B.RID AND (A.CREATOR_DEPT_ID=B.DEPT_ID OR B.DEPT_ID='DEPT_ID') )");
		StringBuffer delUserRightSql = new StringBuffer("DELETE FROM SYS_N_USERRIGHTS WHERE NOT EXISTS(SELECT 1 FROM V_USER_RESOURCES B WHERE SYS_N_USERRIGHTS.CREATOR=B.OPNO AND SYS_N_USERRIGHTS.RESOURCEID=B.RID AND (SYS_N_USERRIGHTS.CREATOR_DEPT_ID=B.DEPT_ID OR B.DEPT_ID='DEPT_ID') )");

		// 来源于部门职位权限管理的非法权限验证语句与删除语句
		StringBuffer selDeptOfficeRightSql = new StringBuffer("SELECT COUNT(1) AS NUM FROM SYS_N_ROLES_DEPT_OFFICE_RIGHTS A WHERE   NOT EXISTS(SELECT 1 FROM V_USER_RESOURCES C,SYS_N_ROLES_DEPT_OFFICE B WHERE B.CREATE_OPNO=C.OPNO AND A.ROLEID=B.ROLEID AND A.RESOURCEID=RID)");
		StringBuffer delDeptOfficeRightSql = new StringBuffer("DELETE FROM SYS_N_ROLES_DEPT_OFFICE_RIGHTS WHERE NOT EXISTS(SELECT 1 FROM V_USER_RESOURCES C,SYS_N_ROLES_DEPT_OFFICE B WHERE B.CREATE_OPNO=C.OPNO AND SYS_N_ROLES_DEPT_OFFICE_RIGHTS.ROLEID=B.ROLEID AND SYS_N_ROLES_DEPT_OFFICE_RIGHTS.RESOURCEID=RID)");

		try {
			Boolean check = true;// 是否需要验证
			while (check) {
				check = false;

				String roleRightsNum = String.valueOf(help.getOneFiledValue(null, selRoleRightsSql.toString(), Arrays.asList()));

				if (!"0".equals(roleRightsNum)) {
					help.executeSql(null, delRoleRightsSql.toString(), Arrays.asList());

					check = true;
				}

				String userRightNum = String.valueOf(help.getOneFiledValue(null, selUserRightSql.toString(), Arrays.asList()));
				if (!"0".equals(userRightNum)) {
					help.executeSql(null, delUserRightSql.toString(), Arrays.asList());
					check = true;
				}

				String deptOfficeRight = String.valueOf(help.getOneFiledValue(null, selDeptOfficeRightSql.toString(), Arrays.asList()));
				if (!"0".equals(deptOfficeRight)) {
					help.executeSql(null, delDeptOfficeRightSql.toString(), Arrays.asList());
					check = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		bean.set("ENDTIME", DateTime.now());
		help.update(null, bean);
		isrun = false;
		return isrun;
	}
}
