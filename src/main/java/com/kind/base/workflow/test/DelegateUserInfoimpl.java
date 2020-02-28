package com.kind.base.workflow.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.workflow.bo.WFlowUserBean;
import com.kind.workflow.interfaces.IWFlowDelegateUserInfo;

import cn.hutool.core.date.DateUtil;

public class DelegateUserInfoimpl implements IWFlowDelegateUserInfo {
	private static com.kind.framework.log.Logger logger = com.kind.framework.log.LogService.getLogger(DelegateUserInfoimpl.class);

	@Override
	public WFlowUserBean getUserBean(BaseActionService service, DataContext dc, WFlowUserBean userBean, String flow_id, String inst_uuid) {
		logger.error("-------------------未实现委托用户逻辑代码-----------------------------flow_id=" + flow_id);
		//获取工作流业务标识
		StringBuffer BusinessSql=new StringBuffer("SELECT UUID,BUSINESS_KEY FROM WF_INSTANCE WHERE UUID=?");
		List<Object> businessParam=new ArrayList<Object>();
		businessParam.add(inst_uuid);
		Map<String, Object> businessMap = service.getSelectMap(null, BusinessSql.toString(), businessParam);
		Object businessKey=businessMap.get("BUSINESS_KEY");
	
		//获取用户有效的委托权限记录
		List queryParam=new ArrayList();
		String userId = userBean.getUserId();
		StringBuffer nextUserSql=new StringBuffer();
		 
		nextUserSql.append("select C.RLOGO,D.OPNO,DEPT_ID  from sys_n_roleuser_entrust D,sys_n_rolerights_entrust B,sys_resources C  where C.CHANNEL_RTYPE='00004' AND D.ROLEID=B.ROLEID  AND  B.RESOURCEID=C.RID  AND C.RLOGO  like ? and exists(select * from SYS_N_ROLES_ENTRUST A where  ? between BEGINDATE and ENDDATE and  A.ROLEID=D.ROLEID AND CREATOR=? )");
		//兼容写法，如果未配置业务类型，则根据只判断工作流来委托
		if(businessKey==null) {
			queryParam.add("%_"+flow_id);
		}else {
			queryParam.add(businessKey+"_"+flow_id);
		}
		queryParam.add(DateUtil.now());
		queryParam.add(userBean.getUserId());
	
		 HashMap<String, String> hmLogSql = new HashMap(10);
		 Map nextUserMap= service.getSelectMap( hmLogSql, nextUserSql.toString(), queryParam);
	
		//根据委托记录获取被委托人员
		
		if(nextUserMap!=null) {
			//封装人人员信息并返回
			String nextUserId=nextUserMap.get("OPNO").toString();
			String nextDeptId=nextUserMap.get("DEPT_ID").toString();
			StringBuffer userInfoSql=new StringBuffer("	SELECT (SELECT OPNAME FROM SYS_N_USERS WHERE OPNO=?) AS OPNAME,A.DEPTNAME,B.NAME AS ORGNAME,B.ORGANID FROM SYS_DEPARTMENT_INFO A,SYS_ORGANIZATION_INFO B WHERE A.ORGANID=B.ORGANID AND DEPTID=?");
			Map<String, Object> userInfoMap = service.getSelectMap(null, userInfoSql.toString(), Arrays.asList(nextUserId,nextDeptId));
			WFlowUserBean nextBean=new WFlowUserBean(userInfoMap.get("OPNAME").toString(), nextUserId, null, nextDeptId, userInfoMap.get("DEPTNAME").toString(), userInfoMap.get("ORGANID").toString(), userInfoMap.get("ORGNAME").toString());
			return nextBean;
		}
		return null;
	}

}
