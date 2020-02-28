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

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.DbHelper;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author HUANGLEI
 *
 */
@Service
@Action(requireLogin = true, action = "#sys_resoure_reindex2", description = "资源重排", powerCode = "", requireTransaction = true)
public class ReIndexResource2 extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		StringBuffer select_sql = new StringBuffer("select * from SYS_RESOURCES where PARENTID='ResourceTop' order by ORDIDX");
		List<Map<String, Object>> resources = getSelectList(hmLogSql, select_sql.toString(), new ArrayList<Object>()); // 顶级
		String parentallordidx = "0";
		for (int i = 0; i < resources.size(); i++) {
			StringBuffer allordidx = new StringBuffer(parentallordidx).append(".").append(new java.text.DecimalFormat("0000").format(i + 1));// 补0
			StringBuffer update_Sql = new StringBuffer("update SYS_RESOURCES set RLEVEL=1,ORDIDX=").append(i + 1).append(",ALLORDIDX = '").append(allordidx).append("' where RID='").append(resources.get(i).get("RID")).append("'");
			if (executeSql(hmLogSql, update_Sql.toString(), new ArrayList<Object>()) != 1) {
				return this.setFailInfo(dc, "更新失败！");
			}
			if (updateAllOrdIdx(hmLogSql, this.getJdbcTemplate(0), String.valueOf(resources.get(i).get("RID")), allordidx.toString(), 1) != 1) {
				return this.setFailInfo(dc, "更新失败！");
			}
		}
		return setSuccessInfo(dc, "更新成功！");
	}

	private int updateAllOrdIdx(HashMap<String, String> hmLogSql, JdbcTemplate jdbcTemplate, String rid, String parentallordidx, int rlevel) {
		List<Map<String, Object>> resources = getSelectList(hmLogSql, jdbcTemplate, "select * from SYS_RESOURCES where PARENTID=? order by ORDIDX", Arrays.asList(rid)); // 顶级

		for (int i = 0; i < resources.size(); i++) {
			StringBuffer allordidx = new StringBuffer(parentallordidx).append(".").append(new java.text.DecimalFormat("0000").format(i + 1));// 补0
			StringBuffer update_Sql = new StringBuffer("update SYS_RESOURCES set RLEVEL=").append(rlevel + 1).append(",ORDIDX=").append(i + 1).append(",ALLORDIDX = '").append(allordidx).append("' where RID='").append(resources.get(i).get("RID")).append("'");
			if (DbHelper.executeSql(hmLogSql, jdbcTemplate, update_Sql.toString(), new ArrayList<Object>()) != 1) {
				return 0;
			}
			// 递归执行
			if (updateAllOrdIdx(hmLogSql, jdbcTemplate, String.valueOf(resources.get(i).get("RID")), allordidx.toString(), rlevel + 1) != 1) {
				return 0;
			}
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
