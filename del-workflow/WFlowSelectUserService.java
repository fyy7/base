package com.kind.base.user.cloudservice.user.workflow;

import java.util.ArrayList;
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

@Service
@Action(requireLogin = true, action = "#workflow_select_user_main", description = "工作流-实例用户选择", powerCode = "", requireTransaction = false)
public class WFlowSelectUserService extends BaseActionService {

	private void getOrganList(HashMap<String, String> hmLogSql, DataContext dc, UserPO supo) {
		StringBuffer sb_organ = new StringBuffer("select a.* from SYS_ORGANIZATION_INFO a where a.PARENTID!='OrganizationTop'");
		// 用户标识，1为本部门，2为本机关所有部门，3为上下级所有，4为上级所有部门，
		// 5为上级加本局部门，6本局加下级部门,7下级部门,8下一级,9下级不包含工商所,10为省局，11为市局,12上一级
		String dept_type = dc.getRequestString("WF_ACTIVITY_USER_TYPE");

		if ("1".equals(dept_type) || "2".equals(dept_type)) {
			// 1为本部门，2为本机关所有部门
			sb_organ.append(" and a.ORGANID='").append(supo.getOrgId()).append("'");
		}

		if ("3".equals(dept_type)) {
			// 3为上下级所有
		}

		if ("4".equals(dept_type)) {
			// 4为上级所有部门
			sb_organ.append(" and exists(select 1 from  SYS_ORGANIZATION_INFO b where b.ORGANID='").append(supo.getOrgId()).append("'");
			sb_organ.append(" and ").append(ConvertSqlDefault.charIndexSQL("b.ALLORDIDX", "a.ALLORDIDX")).append(">0 and b.ORGANID!=a.ORGANID)");
		}
		if ("5".equals(dept_type)) {
			// 5为上级加本局部门
			sb_organ.append(" and exists(select 1 from  SYS_ORGANIZATION_INFO b where b.ORGANID='").append(supo.getOrgId()).append("'");
			sb_organ.append(" and ").append(ConvertSqlDefault.charIndexSQL("b.ALLORDIDX", "a.ALLORDIDX")).append(">0)");
		}
		if ("6".equals(dept_type)) {
			// 6本局加下级部门
			sb_organ.append(" and exists(select 1 from  SYS_ORGANIZATION_INFO b where b.ORGANID='").append(supo.getOrgId()).append("'");
			sb_organ.append(" and ").append(ConvertSqlDefault.charIndexSQL("a.ALLORDIDX", "b.ALLORDIDX")).append(">0)");
		}
		if ("7".equals(dept_type)) {
			// 7下级部门
			sb_organ.append(" and exists(select 1 from  SYS_ORGANIZATION_INFO b where b.ORGANID='").append(supo.getOrgId()).append("'");
			sb_organ.append(" and ").append(ConvertSqlDefault.charIndexSQL("a.ALLORDIDX", "b.ALLORDIDX")).append(">0 and b.ORGANID!=a.ORGANID)");
		}

		if ("8".equals(dept_type)) {
			// 8下一级
			sb_organ.append(" and exists(select 1 from  SYS_ORGANIZATION_INFO b where b.ORGANID='").append(supo.getOrgId()).append("'");
			sb_organ.append(" and ").append(ConvertSqlDefault.charIndexSQL("a.ALLORDIDX", "b.ALLORDIDX")).append(">0 and (b.NLEVEL+1=a.NLEVEL))");
		}
		// 9下级不包含工商所,10为省局，11为市局,12上一级
		if ("9".equals(dept_type)) {
			// 9下级不包含工商所
			sb_organ.append(" and exists(select 1 from  SYS_ORGANIZATION_INFO b where b.ORGANID='").append(supo.getOrgId()).append("'");
			sb_organ.append(" and ").append(ConvertSqlDefault.charIndexSQL("a.ALLORDIDX", "b.ALLORDIDX")).append(">0 and (b.NLEVEL+1=a.NLEVEL)) and a.NLEVEL!=5");
		}
		// 9下级不包含工商所,10为省局，11为市局,12上一级
		if ("10".equals(dept_type)) {
			// 10为省局
			sb_organ.append(" and a.NLEVEL=2");
		}

		if ("11".equals(dept_type)) {
			// 9下级不包含工商所,10为省局，11为市局,12上一级
			sb_organ.append(" and a.NLEVEL=3");
		}
		if ("12".equals(dept_type)) {
			// 9下级不包含工商所,10为省局，11为市局,12上一级
			sb_organ.append(" and exists(select 1 from  SYS_ORGANIZATION_INFO b where b.ORGANID='").append(supo.getOrgId()).append("'");
			sb_organ.append(" and ").append(ConvertSqlDefault.charIndexSQL("b.ALLORDIDX", "a.ALLORDIDX")).append(">0 and (b.NLEVEL-1=a.NLEVEL or (b.NLEVEL=2 and a.NLEVEL=2)))");
		}
		sb_organ.append(" order by a.ALLORDIDX");
		dc.setBusiness("ORGAN_LIST", this.getSelectList(hmLogSql, sb_organ.toString(), new ArrayList<Object>()));
		dc.setBusiness("CURRENT_ORGANID", supo.getOrgId());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		UserPO supo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		// 把Request信息放置business中
		dc.setRequestToBusiness();

		/*
		 * 用户标识
		 * 
		 * 本部门[1] 本机关所有部门[2] 上下级所有部门[3] 上级所有部门[4] 下级所有部门[5] 本局加上级部门[6] 本局加下级部门[7] 下一级部门[8] 上一级部门[9] 下级不包含工商所[10] 省局[11] 市局[12]
		 */
		if (dc.getRequestString("WF_ACTIVITY_USER_TYPE") == null) {
			dc.setBusiness("WF_ACTIVITY_USER_TYPE", "1");
		}
		// 用户类型 0 个人 1部门 2机关
		getOrganList(hmLogSql, dc, supo);

		if ("2".equals(dc.getRequestString("USER_TYPE"))) {
			// 机关
			List list = (List) dc.getBusinessObject("ORGAN_LIST");
			JSONArray arr_json = new JSONArray();
			if (list != null && list.size() > 0) {
				for (Object o : list) {
					Map<String, Object> bean = (Map<String, Object>) o;
					bean.put("id", bean.get("ORGANID"));
					bean.put("pId", bean.get("PARENTID"));
					bean.put("name", bean.get("NAME"));
					arr_json.add(bean);
				}
			}
			dc.setBusiness("ORGAN_TREE_DATAS", arr_json.toJSONString());
		}

		return this.setSuccessInfo(dc);
	}

	@Override
	public String verifyParameter(DataContext arg0) {
		return null;
	}

	@Override
	public String pageAddress(DataContext arg0) {
		return "workflow/manage/selectuser/wflow_select_user_main";
	}
}
