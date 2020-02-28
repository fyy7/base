package com.kind.base.auth.service.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kind.base.core.auth.ResourceService;
import com.kind.framework.core.DbHelper;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;

public class WorkFlawResourceService{
	
	public static String getWorkflowIdForRlogo(BaseActionService server,UserPO userPo, String channelId,String rlogo) {
		HashMap<String, String> hmLogSql =new HashMap<String, String>();
		StringBuffer getIdSql=new StringBuffer("select * from sys_resources where rlogo=?");
		Map<String, Object> resourMap = server.getSelectMap( hmLogSql, getIdSql.toString(), Arrays.asList(rlogo));
		if(resourMap==null) {
			return "";
		}
		String rid=resourMap.get("RID").toString();
		List paramList=new ArrayList();
		StringBuffer querySql=new StringBuffer("select * from sys_resources_workflow where rid=?  ");
		paramList.add(rid);
		querySql.append("and flow_id in (");
		
		querySql.append("select right(rid,8) from v_user_resources WHERE opno =? and DEPT_ID=? and CHANNEL_RTYPE='00004'");
		paramList.add(userPo.getOpNo());
		if(userPo.getOpType()<=1) {
			paramList.add("DEPT_ID");
		}else {
			paramList.add(userPo.getDeptId());
		}
		querySql.append(") order by FLOW_INDEX");
		DataBean[] selectArrayDataBean = server.getSelectArrayDataBean(hmLogSql, querySql.toString(), paramList);
		if (selectArrayDataBean!=null&&selectArrayDataBean.length>0) {
			return selectArrayDataBean[0].getString("FLOW_ID");
		}
		return "";
	}

}
