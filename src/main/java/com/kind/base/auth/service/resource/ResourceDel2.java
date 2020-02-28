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
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.base.auth.cloudservice.auth.ResourcesResetRedis;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author HUANGLEI
 *
 */
@Service
@Action(requireLogin = true, action = "#sys_resoure_del2", description = "资源删除", powerCode = "resource.module", requireTransaction = true)

public class ResourceDel2 extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String rid = dc.getRequestString("RID");
		Map<String, Object> bean = getSelectMap(hmLogSql, "SELECT * FROM  SYS_RESOURCES where RID =?", Arrays.asList(rid));
		if (bean == null) {
			return this.setFailInfo(dc, "删除资源失败，无效资源ID！");
		}
		if (executeSql(hmLogSql, "delete from SYS_RESOURCES where RID = ?", Arrays.asList(rid)) != 1) {
			return this.setFailInfo(dc, "删除资源失败！");
		}
		if (delResource(hmLogSql, dc) != 1) {
			// 递归执行删除
			return this.setFailInfo(dc, "删除下级资源失败！");
		}

		ResourcesResetRedis rrr = new ResourcesResetRedis();
		rrr.resetRedis(hmLogSql, bean.get("APP_ID").toString());
		return setSuccessInfo(dc, "删除成功！");
	}

	/**
	 * 清楚无源的资源
	 * 
	 * @param dc
	 * @return
	 * @throws Exception
	 */
	private int delResource(HashMap<String, String> hmLogSql, DataContext dc) {
		if (getSelectList(hmLogSql, "select RID from SYS_RESOURCES where PARENTID not in (select RID from SYS_RESOURCES) and RLEVEL>1", new ArrayList<Object>()).size() > 0) {
			if (executeSql(hmLogSql, "delete from SYS_RESOURCES where PARENTID not in (select RID from SYS_RESOURCES) and RLEVEL>1", new ArrayList<Object>()) != 1) {
				return 0;
			}
			return delResource(hmLogSql, dc);
		}
		return 1;
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
