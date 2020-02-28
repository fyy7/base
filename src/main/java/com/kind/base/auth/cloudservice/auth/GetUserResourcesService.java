package com.kind.base.auth.cloudservice.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.kind.base.core.auth.ResourceService;
import com.kind.framework.auth.ResourceInfo;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

/**
 * 获取用户权限资源
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "#get_resources_service", description = "获取用户权限资源", powerCode = "", requireTransaction = false)
public class GetUserResourcesService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(GetUserResourcesService.class);

	private CommonService commonService = new CommonService();

	// TODO 需考虑下缓存问题
	/**
	 * http://my.kind.com/portal/restful?action=get_resources_service&USER_ID=05370572-4bc6-4720-b17e-5aa9b7172f98&APP_ID=APP0001&RESOURCE_TYPE=00001&DEPARTMENT_ID=000001001200000000000000000000000000
	 */
	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		ResourceService resourceService = SpringUtil.getBean(ResourceService.class);

		String thirdPart = "0";

		Set<ResourceInfo> resources = new HashSet<ResourceInfo>();
		Map<String, Object> rm = this.getSuccessBaseResultMap();
		try {
			String token = dc.getRequestObject("TOKEN", "");
			String userId = dc.getRequestObject("USER_ID", "");
			String channelId = dc.getRequestObject("APP_ID", "");
			String resourceType = dc.getRequestObject("RESOURCE_TYPE", "");
			String departmentId = dc.getRequestObject("DEPARTMENT_ID", "");
			String organId = dc.getRequestObject("ORGAN_ID", "");
			thirdPart = dc.getRequestObject("THIRD_PART", "");

			// String ret = commonService.doAccessValid(dc, token, channelId);
			// if (StrUtil.isNotBlank(ret)) {
			// return setFailInfo(dc, ret + "，获取用户权限资源失败！");
			// }

			resources = resourceService.getUserResources(userId, channelId, resourceType, departmentId, organId);
		} catch (Exception e) {
			String msg = "获取用户资源数据异常";
			rm = this.getBaseResultMap(0, msg);
			resources = new HashSet<ResourceInfo>();
			log.info(msg + System.getProperty("line.separator") + getStackTrace(e));

			dc.getBusiness().putAll(rm);
			// throw new RuntimeException(msg + e.getMessage());
			return setFailInfo(dc);
		}

		if ("1".equals(thirdPart)) {
			rm.put("value", getThirdPartFormat(hmLogSql, resources));
		} else {
			rm.put("value", resources);
		}

		dc.getBusiness().putAll(rm);

		return setSuccessInfo(dc);
	}

	// 供第三方调用的资源格式
	private List<Map<String, Object>> getThirdPartFormat(HashMap<String, String> hmLogSql, Set<ResourceInfo> resources) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		try {
			StringBuffer rids = new StringBuffer("");
			for (ResourceInfo info : resources) {
				rids.append("'").append(info.getResourceId()).append("',");
			}
			rids.append("''");

			String sql = String.format(" SELECT R.RID, R.TITLE, R.PARENTID PID, R.CHANNEL_RTYPE CRTP, R.VISIBLE VIS, R.ISGROUP GRUP, R.ORDIDX ORD, R.ALLORDIDX, R.APP_ID, R.RLOGO, R.EXTITEM1 EXT1, R.EXTITEM2 EXT2 FROM SYS_RESOURCES R WHERE R.RID IN (%s) ", rids.toString());
			result = this.getSelectList(hmLogSql, sql, new ArrayList<Object>());
		} catch (Exception e) {
			result = new ArrayList<Map<String, Object>>();
		}

		return result;
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "common/json";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

}
