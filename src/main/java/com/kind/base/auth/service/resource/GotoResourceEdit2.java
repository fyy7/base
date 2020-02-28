/**
 * @Title: GotoResourceMain.java 
 * @Package com.kind.portal.service.resource 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月5日 下午4:39:00 
 * @version V1.0   
 */
package com.kind.base.auth.service.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * @author HUANGLEI
 *
 */
@Service
@Action(requireLogin = true, action = "#sys_resoure_goto_edit2", description = "资源新增、修改编辑页面", powerCode = "resource.module", requireTransaction = false)

public class GotoResourceEdit2 extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		dc.setBusiness("APP_ID", dc.getRequestString("APP_ID"));
		List<Map<String, Object>> resourece_button_list = new ArrayList<>();

		String rid = dc.getRequestString("RID");
		List<Map<String, Object>> resourece_button_lists = getSelectList(hmLogSql, "select DM as BVALUE,DMNR as  BTITLE, '0' as BSELECT from SYS_DMB where dmlx='AUTHORITY.BUTTON' order by DM ", Arrays.asList());
		if (!StrUtil.isEmpty(rid)) {
			List<Object> param = new ArrayList<>();
			param.add(rid);
			Map<String, Object> resourece_bean = getSelectMap(hmLogSql, "select * from SYS_RESOURCES where rid=?", param);
			// 组织结构的处理
			if (!"1".equals(resourece_bean.get("SCOPE"))) {
				// 不是所有的，取出包含或排除的值
				StringBuffer sb_sql = new StringBuffer("SELECT B.NAME,A.ORGANID FROM SYS_RESOURCES_ORGS A,SYS_ORGANIZATION_INFO B WHERE A.ORGANID=B.ORGANID AND A.RID=?");

				List<Map<String, Object>> orgs = getSelectList(hmLogSql, sb_sql.toString(), param);
				if (orgs != null) {
					StringBuffer org_names = new StringBuffer("");
					StringBuffer org_ids = new StringBuffer("");
					for (int i = 0; i < orgs.size(); i++) {
						if (i != 0) {
							org_names.append(",");
							org_ids.append(",");
						}
						org_names.append(orgs.get(i).get("NAME"));
						org_ids.append(orgs.get(i).get("ORGANID"));
					}
					resourece_bean.put("SCOPE_ORGIDS", org_ids.toString());
					resourece_bean.put("SCOPE_ORGNAMES", org_names.toString());
				}
			}
			List<Map<String, Object>> flowList = getSelectList(hmLogSql, "SELECT FLOW_ID,FLOW_NAME FROM sys_resources_workflow WHERE  RID=? order by FLOW_ID ", param);
			StringBuffer flowIds = new StringBuffer(), flowNames = new StringBuffer();
			for (int i = 0; i < flowList.size(); i++) {
				if (flowIds.length() > 0) {
					flowIds.append(",");
					flowNames.append(",");
				}
				flowIds.append(flowList.get(i).get("FLOW_ID"));
				flowNames.append(flowList.get(i).get("FLOW_NAME"));
			}
			resourece_bean.put("WORKFLOWS", flowIds);
			resourece_bean.put("WORKFLOWS_NAME", flowNames);
			dc.setBusiness("RESOURCE_BEAN", resourece_bean);

			resourece_button_list = getSelectList(hmLogSql, "SELECT BVALUE,BTITLE,'1' BSELECT FROM SYS_RESOURCES_BUTTON WHERE BVALUE!=0  and RID=? order by BVALUE ", param);

		}
		// jsonobj2list

		for (int i = 0; i < resourece_button_lists.size(); i++) {

			for (int j = 0; j < resourece_button_list.size(); j++) {
				String button = resourece_button_lists.get(i).get("BVALUE").toString();
				String buttons = resourece_button_list.get(j).get("BVALUE").toString();

				if (button.equals(buttons)) {
					resourece_button_lists.get(i).put("BTITLE", resourece_button_list.get(j).get("BTITLE"));
					resourece_button_lists.get(i).put("BSELECT", "1");

				}

			}

		}

		dc.setBusiness("RESOURECE_BUTTON_LIST", resourece_button_lists);

		return setSuccessInfo(dc);

	}

	@Override
	public String pageAddress(DataContext arg0) {

		return "auth/resource/resourceform";
	}

	@Override
	public String verifyParameter(DataContext arg0) {

		return null;
	}

}
