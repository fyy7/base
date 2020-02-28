/**
 * @Title: GotoResourceMain.java 
 * @Package com.kind.portal.service.resource 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月5日 下午4:39:00 
 * @version V1.0   
 */
package com.kind.base.auth.service.entrust;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.kind.base.auth.service.roles.RolesUtil;
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
 * 权限委托管理
 * 
 * @author WangXiaoyi 根据类型获取自己的权限
 * 
 */
@Service
@Action(requireLogin = true, action = "#get_resources_entrust_ourself", description = "获取自身资源权限(公用)", powerCode = "resource.entrust", requireTransaction = false)

public class EntrustEditQuery extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// ROLEID

		String appId = dc.getRequestString("APP_ID");
		String channelRType = dc.getRequestString("CHANNEL_RTYPE");
		int result = channelRType.indexOf("default01");
		if (result > -1) {
			channelRType = channelRType.replace("default01", "00001");
		}

		UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		List params=new ArrayList();
		StringBuffer sql = new StringBuffer("select a.RID, a.TITLE, a.PARENTID, a.ALLORDIDX, a.CHANNEL_RTYPE, a.APP_ID from SYS_RESOURCES a where exists(select 1 from v_SYS_RESOURCES_VISIBLE where a.rid=rid)  ");
		if(StrUtil.isNotEmpty(channelRType)) {
			params.add(channelRType);
			sql.append( " and "+ConvertSqlDefault.charIndexSQL("?", "a.CHANNEL_RTYPE") + ">0 ");
		}

		sql.append( " and  (a.ISENTRUST<>0 or a.ISENTRUST IS NULL) and (exists(select 1 from SYS_RESOURCES b where b.RID=a.PARENTID and (b.ISENTRUST<>0 or b.ISENTRUST IS NULL) and exists(select 1 from SYS_RESOURCES c where c.RID=b.PARENTID and (c.ISENTRUST<>0 or c.ISENTRUST IS NULL))) or ISGROUP=1) and APP_ID=?");
		params.add(appId);
		List<Map<String, Object>> list = getSelectList(hmLogSql, sql.toString(), params);

		if (!getSubjectAuthInfo(dc).isSupperUser()) {
			ResourceService rs = SpringUtil.getBean(ResourceService.class);
			Set<ResourceInfo> resources = rs.getUserResources(userpo, appId, channelRType);
			list = RolesUtil.listFilterForSet(list, resources, "RID");
		}

		dc.setBusiness("value", list);

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {

		return null;
	}

	@Override
	public String verifyParameter(DataContext dc) {

		return null;
	}

}
