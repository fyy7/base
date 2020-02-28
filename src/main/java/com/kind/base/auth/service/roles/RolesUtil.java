/**
 * @Title: RolesUtil.java 
 * @Package com.kind.portal.service.roles 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月21日 下午2:55:44 
 * @version V1.0   
 */
package com.kind.base.auth.service.roles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kind.framework.auth.ResourceInfo;

/**
 * @author huanglei
 *
 */
public class RolesUtil {
	/**
	 * set转换为HashMap
	 * 
	 * @param resources
	 * @return
	 */
	public static HashMap<String, ResourceInfo> set2Map(Set<ResourceInfo> resources) {
		HashMap<String, ResourceInfo> backMap = new HashMap<>();
		Iterator<ResourceInfo> it = resources.iterator();
		while (it.hasNext()) {
			ResourceInfo ri = it.next();
			backMap.put(ri.getResourceId(), ri);
		}
		return backMap;
	}

	/**
	 * 根据set获取列表权限项目
	 * 
	 * @param list
	 * @param resources
	 * @return
	 */
	public static List<Map<String, Object>> listFilterForSet(List<Map<String, Object>> list, Set<ResourceInfo> resources, String keyName) {
		List<Map<String, Object>> backList = new ArrayList<>();
		HashMap<String, ResourceInfo> setMap = set2Map(resources);
		for (int i = 0; i < list.size(); i++) {
			if (setMap.get(list.get(i).get(keyName)) != null) {
				backList.add(list.get(i));
			}

		}
		return backList;
	}

}
