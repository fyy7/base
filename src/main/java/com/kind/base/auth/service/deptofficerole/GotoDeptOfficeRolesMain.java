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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

/**
 * @author HUANGLEI
 *
 */
@Service
@Action(requireLogin = true, action = "#dept_office_role_main", description = "进入部门、职务主页", powerCode = "resource.dept_person.power", requireTransaction = false)

public class GotoDeptOfficeRolesMain extends BaseActionService {
	private static com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(GotoDeptOfficeRolesMain.class);

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		/* 查询机构数据 */
		UserPO userpo = (UserPO) dc.getSession().get(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));

		List<Object> paramList = new ArrayList<Object>();
		String OrgId = userpo.getOrgId() == null ? "00000000-0000-0000-1000-000000000000" : userpo.getOrgId();
		paramList.add(OrgId);

		/* 查询机构数据 */
		StringBuffer orgOptions = new StringBuffer("SELECT ORGANID,ORGANDM,(").append(ConvertSqlDefault.getStrJoinSQL(ConvertSqlDefault.getBlankString("ALLORDIDX", "."), "NAME")).append(") NAME2,NAME FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND ORGANDM!='OrganizationTop'");
		orgOptions.append(" AND ALLORDIDX LIKE (SELECT ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" FROM SYS_ORGANIZATION_INFO where ISDEL=0 AND ORGANID=?)");
		orgOptions.append(" ORDER BY ALLORDIDX");
		List<Map<String, Object>> orgList = this.getSelectList(hmLogSql, orgOptions.toString(), paramList);

		dc.setBusiness("SUPER_LIST", orgList);
		dc.setBusiness("ORGAN_ID", OrgId);
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext arg0) {

		return "auth/deptofficerole/dept_office_role_main";
	}

	@Override
	public String verifyParameter(DataContext arg0) {

		return null;
	}

}
