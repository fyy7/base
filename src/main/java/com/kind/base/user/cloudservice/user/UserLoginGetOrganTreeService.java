package com.kind.base.user.cloudservice.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.auth.SubjectAuthInfo;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

/**
 * @author chenzhiwei
 *
 *         2018年4月8日
 */
@Service
@Action(requireLogin = false, action = "#user_login_get_organ_list", description = "获取组织结构列表", powerCode = "", requireTransaction = false)
public class UserLoginGetOrganTreeService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		String opAccount = dc.getRequestString("OPACCOUNT");
		if (userpo != null) {
			if (userpo.getConsignorType() == 1) {
				opAccount = userpo.getConsignorSysUserPO().getOpAccount();
			} else {
				opAccount = userpo.getOpAccount();
			}
		}
		StringBuffer sb_sql = new StringBuffer("SELECT '").append(opAccount).append("' OPACCOUNT,A.NAME,A.ORGANID,A.NLEVEL FROM SYS_ORGANIZATION_INFO A WHERE A.ISDEL=0 AND A.PARENTID!='OrganizationTop000000000000000000000'");
		SubjectAuthInfo sa = getSubjectAuthInfo(dc);
		List<Object> paramData = new ArrayList<Object>();
		if (!sa.isSupperUser()) {
			sb_sql.append(" AND (EXISTS(SELECT 1 FROM SYS_N_USER_DEPT_INFO b,SYS_N_USERS c WHERE A.ORGANID=b.ORGAN_ID");
			sb_sql.append(" AND c.OPNO=b.OPNO AND c.OPACCOUNT=? AND c.ISDEL=0) ");
			paramData.add(opAccount);
			sb_sql.append(" or (").append(ConvertSqlDefault.getStrJoinSQL("A.ACCOUNT_PERFIX", "'admin'")).append("=?)"); // 某机构的管理员
			paramData.add(opAccount);
			sb_sql.append(")");

			StringBuffer sb_sql1 = new StringBuffer("SELECT distinct b.ORGAN_ID FROM SYS_N_USER_DEPT_INFO b,SYS_N_USERS c WHERE ");
			sb_sql1.append(" c.OPNO=b.OPNO AND b.MAIN_DEPT_FLAG=1 and c.OPACCOUNT=? AND c.ISDEL=0");
			List<Map<String, Object>> main_org_id = this.getSelectList(hmLogSql, sb_sql1.toString(), Arrays.asList(opAccount));

			// Object main_org_id = DBHelper.selectOneFieldValueBySQL(this.operateLog, ac.getConnection(), sb_sql1.toString(), new Object[] { opAccount });
			if (main_org_id == null) {

			}
			dc.setBusiness("MAIN_ORG_ID", main_org_id);
			// this.getJSONObject().put("MAIN_ORG_ID", main_org_id);
		}
		sb_sql.append(" order by A.ALLORDIDX");
		List<Map<String, Object>> arr_resourceId = getSelectList(hmLogSql, sb_sql.toString(), paramData);
		dc.setBusiness("ORGAN_TREE", JSON.toJSON(arr_resourceId));
		return this.setSuccessInfo(dc, "获取成功！");
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
