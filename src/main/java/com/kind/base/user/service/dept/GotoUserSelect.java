package com.kind.base.user.service.dept;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
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
 *         2018年4月19日
 */
@Service
@Action(requireLogin = true, action = "#user_select", description = "进入人员选择页面", powerCode = "", requireTransaction = false)
public class GotoUserSelect extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String organid = dc.getRequestString("ORGANID");
		// 多选标识
		String multi = dc.getRequestString("MULTI");
		// 组织机构可修改标识
		String orgModify = dc.getRequestString("ORG_MODIFY");
		// 用户id的页面元素id
		String idUserid = dc.getRequestString("ID_USERID");
		// 用户名的页面元素id
		String idUsername = dc.getRequestString("ID_USERNAME");
		// 部门id的页面元素id
		String idDeptid = dc.getRequestString("ID_DEPTID");
		// 部门名的页面元素id
		String idDeptname = dc.getRequestString("ID_DEPTNAME");
		String deptId = dc.getRequestString("DEPTID");
		String deptName="";
		
		// 查询机构下拉
		UserPO userpo = (UserPO) dc.getSession().get(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));

		List<Object> param = new ArrayList<Object>();
		StringBuffer orgOptions = new StringBuffer("SELECT ORGANID,ORGANDM,(").append(ConvertSqlDefault.getStrJoinSQL(ConvertSqlDefault.getBlankString("ALLORDIDX", "."), "NAME")).append(") NAME FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND ORGANDM!='OrganizationTop'");
		orgOptions.append(" AND ALLORDIDX LIKE (SELECT ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" FROM SYS_ORGANIZATION_INFO where ISDEL=0 AND ORGANID=?)");
		orgOptions.append(" ORDER BY ALLORDIDX");
		param.add(userpo.getOrgId());
		List<Map<String, Object>> organList = this.getSelectList(hmLogSql, orgOptions.toString(), param);
		dc.setBusiness("ORGAN_LIST", organList);

		if (StrUtil.isNotEmpty(deptId) && StrUtil.isEmpty(deptName)) {
			Map<String, Object> selectMap = this.getSelectMap(hmLogSql, "SELECT DEPTNAME FROM SYS_DEPARTMENT_INFO where DEPTID=? ", Arrays.asList(deptId));
			if (selectMap != null) {
				deptName = selectMap.get("DEPTNAME").toString();
			}

		}
		dc.setBusiness("CURRENT_ORGANID", userpo.getOrgId());
		Integer usertype = userpo.getOpType();
		dc.setBusiness("USERTYPE", usertype);
		if (usertype == 2) {
			dc.setBusiness("ORGAN_TREE_DATAS", JSONArray.toJSONString(organList));
		}

		dc.setBusiness("ORGANID", organid);
		dc.setBusiness("DEPTID", deptId);
		dc.setBusiness("DEPTIDNAME", deptName);
		dc.setBusiness("MULTI", multi);
		dc.setBusiness("ORG_MODIFY", orgModify);
		dc.setBusiness("ID_USERID", idUserid);
		dc.setBusiness("ID_USERNAME", idUsername);
		dc.setBusiness("ID_DEPTID", idDeptid);
		dc.setBusiness("ID_DEPTNAME", idDeptname);

		
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "user/dept/dept_leader_select";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
