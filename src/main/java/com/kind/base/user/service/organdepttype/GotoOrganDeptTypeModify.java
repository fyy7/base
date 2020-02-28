package com.kind.base.user.service.organdepttype;

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
 * 
 * @author chenzhiwei
 * 
 *         2018年4月23日
 */

@Service
@Action(requireLogin = true, action = "#organ_dept_type_modify", description = "机构部门类型维护", powerCode = "", requireTransaction = false)
public class GotoOrganDeptTypeModify extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String organ_id = dc.getRequestString("ORGAN_ID");
		String dm = dc.getRequestString("DM");
		String cmd = dc.getRequestString("CMD");
		String flag = dc.getRequestString("FLAG");
		if ("U".equals(cmd)) {
			String sql = "SELECT * FROM ORGAN_DMB WHERE DMLX=? AND DM=? AND ORGAN_ID=?";
			List<Object> param = new ArrayList<Object>();
			param.add(flag);
			param.add(dm);
			param.add(organ_id);
			dc.setBusiness("SYS_DMB_INFO", this.getSelectMap(hmLogSql, sql, param));
		}
		/* 查询机构数据 */
		List<Object> paramList = new ArrayList<Object>();
		UserPO userpo = (UserPO) dc.getSession().get(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		String organId = userpo.getOrgId() == null ? "00000000-0000-0000-1000-000000000000" : userpo.getOrgId();
		StringBuffer orgOptions = new StringBuffer("SELECT ORGANID,ORGANDM,(").append(ConvertSqlDefault.getStrJoinSQL(ConvertSqlDefault.getBlankString("ALLORDIDX", "."), "NAME")).append(") NAME FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND ORGANDM!='OrganizationTop'");
		orgOptions.append(" AND ALLORDIDX LIKE (SELECT ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" FROM SYS_ORGANIZATION_INFO where ISDEL=0 AND ORGANID=?)");
		orgOptions.append(" ORDER BY ALLORDIDX");
		paramList.add(organId);
		List<Map<String, Object>> superList = this.getSelectList(hmLogSql, orgOptions.toString(), paramList);
		dc.setBusiness("SUPER_LIST", superList);

		dc.setBusiness("CMD", cmd);
		dc.setBusiness("FLAG", flag);

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "user/sysdmb/organdeptoffice/organ_dmb_dep_modify";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

}
