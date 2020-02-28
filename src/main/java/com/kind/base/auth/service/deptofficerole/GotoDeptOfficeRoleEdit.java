/**
 * @Title: GotoResourceMain.java 
 * @Package com.kind.portal.service.resource 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月5日 下午4:39:00 
 * @version V1.0   
 */
package com.kind.base.auth.service.deptofficerole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kind.base.auth.service.roles.GotoRolesEdit;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
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
@Action(requireLogin = true, action = "#dept_office_role_edit", description = "进入部门、职务权限分配页面", powerCode = "resource.dept_person.power", requireTransaction = false)

public class GotoDeptOfficeRoleEdit extends BaseActionService {
	private static com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(GotoDeptOfficeRoleEdit.class);

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// ROLEID
		String roleid = dc.getRequestString("ROLEID");
		UserPO userpo = (UserPO) dc.getSession().get(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		String organId = "";

		Map<String, Object> beanMap = null;
		List<Object> param = new ArrayList<Object>();

		if (StrUtil.isEmpty(roleid)) {
			// 新增
			beanMap = new HashMap<String, Object>(1);
			roleid = "R" + GenID.gen(19);
			beanMap.put("ROLEID", roleid);

			organId = userpo.getOrgId();
			dc.setBusiness("ORGAN_ID", organId);
			dc.setBusiness("CMD", "A");
		} else {
			param.add(roleid);
			String selectBeanSql = "SELECT ROLEID,REMARK,DEPT_TYPE,OFFICE_TYPE,CREATE_OPNO,UPDATE_AT,CREATE_AT,CREATE_OPNAME,ROLENAME FROM SYS_N_ROLES_DEPT_OFFICE WHERE ROLEID = ?";
			beanMap = this.getSelectMap(hmLogSql, selectBeanSql, param);

			organId = dc.getRequestString("ORGAN_ID");
			dc.setBusiness("ORGAN_ID", organId);
			dc.setBusiness("CMD", "U");
		}

		dc.setBusiness("BEAN", beanMap);
		dc.setBusiness("ROLEID", roleid);

		String roleRightsBeanSql = "SELECT * FROM SYS_N_ROLES_DEPT_OFFICE_RIGHTS WHERE ROLEID=?";
		param.clear();
		param.add(roleid);

		// 获取勾选资源
		List<Map<String, Object>> rolerightsList = this.getSelectList(hmLogSql, roleRightsBeanSql, param);

		dc.setBusiness("ROLERIGHTS_BEAN", JSON.toJSON(rolerightsList));

		String deptTypeBeanSql = "SELECT DM,DMNR FROM SYS_DMB WHERE DMLX ='DEPT.TYPE'";

		dc.setBusiness("DEPT_TYPE_BEAN", this.getSelectList(hmLogSql, deptTypeBeanSql, Arrays.asList()));

		String officeTypeBeanSql = "SELECT DM,DMNR FROM SYS_DMB WHERE DMLX='OFFICE.TYPE'";
		dc.setBusiness("OFFICE_TYPE_BEAN", this.getSelectList(hmLogSql, officeTypeBeanSql, Arrays.asList()));
		// 勾选资源转为json对象
		JSONObject jsonData = new JSONObject();
		for (int i = 0; i < rolerightsList.size(); i++) {
			jsonData.put(rolerightsList.get(i).get("RESOURCEID").toString(), true);

		}
		dc.setBusiness("ROLERIGHTS_JSON", jsonData);
		// 获取已保存的系统、类型数据
		JSONArray backJson = GotoRolesEdit.getResourcesAppinfo(hmLogSql, param, "SYS_N_USERRIGHTS", dc, getSubjectAuthInfo(dc));
		dc.setBusiness("USERRIGHTS_TYPE", backJson);

		/* 查询机构数据 */
		List<Object> paramList = new ArrayList<Object>();
		String OrgId = userpo.getOrgId() == null ? "00000000-0000-0000-1000-000000000000" : userpo.getOrgId();
		paramList.add(OrgId);

		/* 查询机构数据 */
		StringBuffer orgOptions = new StringBuffer("SELECT ORGANID,ORGANDM,(").append(ConvertSqlDefault.getStrJoinSQL(ConvertSqlDefault.getBlankString("ALLORDIDX", "."), "NAME")).append(") NAME2,NAME FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND ORGANDM!='OrganizationTop'");
		orgOptions.append(" AND ALLORDIDX LIKE (SELECT ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" FROM SYS_ORGANIZATION_INFO where ISDEL=0 AND ORGANID=?)");
		orgOptions.append(" ORDER BY ALLORDIDX");
		List<Map<String, Object>> orgList = this.getSelectList(hmLogSql, orgOptions.toString(), paramList);

		dc.setBusiness("SUPER_LIST", orgList);

		return this.setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext arg0) {
		return "auth/deptofficerole/dept_office_role_modify";
	}

	@Override
	public String verifyParameter(DataContext arg0) {
		return null;
	}

}
