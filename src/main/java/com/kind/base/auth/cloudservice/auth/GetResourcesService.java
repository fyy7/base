package com.kind.base.auth.cloudservice.auth;

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
import com.kind.framework.tree.TreeNode;
import com.kind.framework.utils.SpringUtil;

/**
 * 根据资源ID列表获取资源列表
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "#get_resources_by_rids_service", description = "根据资源ID列表获取资源列表", powerCode = "", requireTransaction = false)
public class GetResourcesService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(GetResourcesService.class);

	/**
	 * http://my.kind.com/portal/restful?action=get_resources_by_rids_service &TOKEN= &APP_ID=APP0001 &RTYPE= &RIDS=R1311220919234240001|R1401221125013960001... &CASCADE=1 &TREEMODE=1
	 */
	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		ResourceService resourceService = SpringUtil.getBean(ResourceService.class);

		Set<ResourceInfo> resources = new HashSet<ResourceInfo>();
		Map<String, Object> rm = this.getSuccessBaseResultMap();
		String msg = "";
		try {
			// String token = dc.getRequestObject("TOKEN", "");
			String channelId = dc.getRequestObject("APP_ID", "");
			String rtype = dc.getRequestObject("RTYPE", ""); // 资源类型
			String rids = dc.getRequestObject("RIDS", ""); // 资源ID列表，格式：ID1|ID2...
			String cascade = dc.getRequestObject("CASCADE", ""); // 0：仅获取ids对应的一级节点；1：获取ids一级节点以及一级子节点；2：获取ids一级节点以及级联获取子节点。不能为空
			String treeMode = dc.getRequestObject("TREEMODE", ""); // 是否以rids各个节点为根节点分别生成树结构，0：否，1：是

			if ("1".equals(treeMode)) {
				List<TreeNode> treeRoots = resourceService.getResourceTreeByIds(cascade, channelId, rtype, rids);
				rm.put("value", treeRoots);
			} else {
				resources = resourceService.getResourcesByIds(cascade, channelId, rtype, rids);
				rm.put("value", resources);
			}
		} catch (Exception e) {
			msg = "获取资源列表异常！";
			rm = this.getBaseResultMap(0, msg);
			log.info(msg + System.getProperty("line.separator") + getStackTrace(e));

			dc.getBusiness().putAll(rm);
			// throw new RuntimeException(msg + e.getMessage());
			return setFailInfo(dc);
		}

		dc.getBusiness().putAll(rm);

		return setSuccessInfo(dc);
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
