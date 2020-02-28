/**
 * @Title: ResourceUtil.java 
 * @Package com.kind.portal.service.auth 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月16日 下午4:29:44 
 * @version V1.0   
 */
package com.kind.base.core.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.kind.framework.auth.ResourceInfo;
import com.kind.framework.core.bean.UserPO;

/**
 * @author huanglei
 *
 */
public class PowerResourceUtil {

	/**
	 * 根据权限过滤资源对象
	 * 
	 * @param resList
	 *            资源对象
	 * @param userpo
	 *            用户bean
	 * @param channelRType
	 *            资源类型
	 * @return 过滤后的资源列表
	 */
	public static List<Map<String, Object>> getPowerList(List<Map<String, Object>> resList, UserPO userPo, String appid, String channelRType, String keyColumn) {
		ResourceService rs = new ResourceService();
		Set<ResourceInfo> resources = rs.getUserResourcesByUserPO(userPo, appid, channelRType);
		// List<Map<String, Object>> backlist = new ArrayList<>();
		JSONObject tmp = new JSONObject();
		// 数据转换，便于处理
		for (ResourceInfo ri : resources) {
			tmp.put(ri.getResourceId(), true);
		}
		return comparePowerList(resList, tmp, keyColumn);
	}

	public static List<Map<String, Object>> comparePowerList(List<Map<String, Object>> resList, JSONObject compareJson, String keyColumn) {
		List<Map<String, Object>> backlist = new ArrayList<>();
		for (int i = 0; i < resList.size(); i++) {
			if (compareJson.getBooleanValue(resList.get(i).get(keyColumn).toString())) {
				backlist.add(resList.get(i));
			}
		}
		return backlist;
	}
}
