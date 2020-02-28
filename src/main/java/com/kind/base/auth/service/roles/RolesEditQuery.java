/**
 * @Title: GotoResourceMain.java 
 * @Package com.kind.portal.service.resource 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月5日 下午4:39:00 
 * @version V1.0   
 */
package com.kind.base.auth.service.roles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.kind.base.core.auth.ResourceService;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.auth.ResourceInfo;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * @author huanglei 根据类型获取自己的权限
 * 
 *
 */
@Service
@Action(requireLogin = true, action = "#get_resources_ourself", description = "获取自身资源权限(公用)", powerCode = "", requireTransaction = false)
public class RolesEditQuery extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// ROLEID
		String appid = dc.getRequestString("APP_ID");
		String channelRType = dc.getRequestString("CHANNEL_RTYPE");
		if (channelRType.indexOf("default01") > -1) {
			channelRType = channelRType.replace("default01", "00000,00001,00002");
		}
		List paramList=new ArrayList();
		
		UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		StringBuffer querySql=new StringBuffer("select A.RID,A.TITLE,A.PARENTID,A.ALLORDIDX,A.CHANNEL_RTYPE,A.APP_ID from v_SYS_RESOURCES_VISIBLE A where   APP_ID=? ");
		paramList.add(appid);
		if(StrUtil.isNotEmpty(channelRType)) {
			paramList.add(channelRType);
			querySql.append("and "+ConvertSqlDefault.charIndexSQL("?", "CHANNEL_RTYPE") + ">0");
		}
	
		querySql.append(" order by ALLORDIDX");
		List<Map<String, Object>> list = getSelectList(hmLogSql, querySql.toString(),paramList);
		if (!getSubjectAuthInfo(dc).isSupperUser()) {
			ResourceService rs = SpringUtil.getBean(ResourceService.class);

			Set<ResourceInfo> resources = rs.getUserResources(userpo, appid, channelRType);
			list = RolesUtil.listFilterForSet(list, resources, "RID");
		}
		// 该模拟 的授权会被权限回收，改为app插入资源表
		// if (list.size() == 0) {
		// Map<String, Object> defaultMap = new HashMap();
		// defaultMap.put("RID", "appid" + appid);
		// defaultMap.put("TITLE", CodeSwitching.getReidiosJsonDm("sys_appname", appid, "COMMON") + "(入口权限)");
		// defaultMap.put("PARENTID", "ResourceTop");
		// defaultMap.put("CHANNEL_RTYPE", "00000");
		// defaultMap.put("APP_ID", appid);
		// list.add(defaultMap);
		// }
		dc.setBusiness("value", list);
		return setSuccessInfo(dc);
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
