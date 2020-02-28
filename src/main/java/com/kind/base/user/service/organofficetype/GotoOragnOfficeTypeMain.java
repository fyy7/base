package com.kind.base.user.service.organofficetype;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author chenzhiwei
 *
 *         2018年4月23日
 */
@Service
@Action(requireLogin = true, action = "#organ_office_type_main", description = "机构职务类型管理", powerCode = "", requireTransaction = false)
public class GotoOragnOfficeTypeMain extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		/*
		 * UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM)); // 获取登陆者Session信息 String unit = userpo.getOrgId(); // 组织结构 查询机构数据 StringBuffer orgOptions = new StringBuffer("SELECT ORGANID,ORGANDM,(").append(ConvertSqlDefault.getStrJoinSQL(ConvertSqlDefault.getBlankString("ALLORDIDX", "."), "NAME")).append(") NAME FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND ORGANDM!='OrganizationTop'"); orgOptions.append(" AND ALLORDIDX LIKE (SELECT ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" FROM SYS_ORGANIZATION_INFO where ISDEL=0 AND ORGANID=?)"); orgOptions.append(" ORDER BY ALLORDIDX"); List<Map<String, Object>> superList = this.getSelectList(hmLogSql, orgOptions.toString(), Arrays.asList(unit)); dc.setBusiness("SUPER_LIST", superList);
		 */
		dc.setBusiness("FLAG", "OFFICE.TYPE");
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "user/sysdmb/organdeptoffice/organ_dmb_main";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
