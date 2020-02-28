package com.kind.base.user.service.organ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年2月23日
 */
@Service
@Action(requireLogin = true, action = "#organ_edits", description = "组织结构编辑", powerCode = "userpower.organ", requireTransaction = false)
public class EditOrgan extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String organId = dc.getRequestString("ORGANID");
		String pId = dc.getRequestString("PID");
		UserPO userpo = (UserPO) dc.getSession().get(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));

		String nlevel = null;
		List<Object> paramList = new ArrayList<Object>();

		String nullStr = "null";
		if (!StrUtil.isEmpty(organId) && !nullStr.equalsIgnoreCase(organId)) {
			String sql = "SELECT * FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND ORGANID =?";
			paramList.add(organId);
			Map<String, Object> organBean = this.getSelectMap(hmLogSql, sql, paramList);
			nlevel = String.valueOf(organBean.get("NLEVEL"));
			dc.setBusiness("BEAN", organBean);
			dc.setBusiness("CMD", "U");
		} else {
			dc.setBusiness("CMD", "A");
		}

		StringBuffer orgOptions = new StringBuffer("SELECT NAME ORGANNAME, ORGANID,ORGANDM,(").append(ConvertSqlDefault.getStrJoinSQL(ConvertSqlDefault.getBlankString("ALLORDIDX", "."), "NAME")).append(") NAME,NLEVEL,ALLORDIDX FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0");
		if (!ConCommon.NUM_2.equals(nlevel)) {
			// 级别为2，就是根目录，当为2时，则需要有根选择，避免判断验证不过
			orgOptions.append(" and organdm!='OrganizationTop'");
		}
		if (!StrUtil.isEmpty(organId)) {
			orgOptions.append(" and ALLORDIDX not like (select ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" from SYS_ORGANIZATION_INFO where ISDEL=0 AND ORGANID='").append(organId).append("')");
		}
		if (userpo.getOpType() != 0) {
			orgOptions.append(" and ALLORDIDX like (select ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" from SYS_ORGANIZATION_INFO where ISDEL=0 AND ORGANID='").append(userpo.getOrgId()).append("')");
		}
		orgOptions.append(" order by ALLORDIDX");

		// 获取上级组织列表
		List<Map<String, Object>> superList = this.getSelectList(hmLogSql, orgOptions.toString(), new ArrayList<Object>());
		List<Map<String, Object>> grouptype_list = this.getSelectList(hmLogSql, "SELECT DM,DMNR FROM SYS_DMB WHERE DMLX='GBTYPE'", new ArrayList<Object>());
		dc.setBusiness("SUPER_LIST", superList);
		dc.setBusiness("ORGANID", organId);
		dc.setBusiness("PARENTID", pId);
		dc.setBusiness("GROUPTYPE_LIST", grouptype_list);
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "user/organ/organ_edit";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
