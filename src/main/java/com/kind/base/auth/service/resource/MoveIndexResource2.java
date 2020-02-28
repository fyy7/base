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

import org.springframework.stereotype.Service;

import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author HUANGLEI
 *
 */
@Service
@Action(requireLogin = true, action = "#sys_resoure_moveIndex", description = "资源模块-菜单上、下移", powerCode = "", requireTransaction = true)

public class MoveIndexResource2 extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String rid = dc.getRequestString("RID");
		String _style = dc.getRequestString("STYLE");

		Map<String, Object> moveFromMap = getSelectMap(hmLogSql, "select * from SYS_RESOURCES where RID=?", Arrays.asList(rid));
		if (moveFromMap == null) {
			return setFailInfo(dc, "获取资源信息失败！");
		}
		Object ordidx;
		DataBean fromBean = new DataBean("SYS_RESOURCES", "RID");
		DataBean toBeans = new DataBean("SYS_RESOURCES", "RID");
		List<Map<String, Object>> move2List;
		StringBuffer sql = new StringBuffer();

		ordidx = moveFromMap.get("ORDIDX");

		sql.append("select * from SYS_RESOURCES where PARENTID='").append(String.valueOf(moveFromMap.get("PARENTID"))).append("' and RID<>'").append(rid).append("'");
		if (_style.equals("1")) {
			// 上移
			sql.append(" and ORDIDX < ").append(String.valueOf(ordidx)).append(" order by ORDIDX desc");
		} else {
			// 下移
			sql.append(" and ORDIDX > ").append(String.valueOf(ordidx)).append(" order by ORDIDX");
		}

		move2List = getSelectList(hmLogSql, sql.toString(), new ArrayList<Object>());

		if (move2List == null || move2List.size() == 0) {
			return setSuccessInfo(dc, "没有可移动的目标！");
		}
		toBeans.set("RID", move2List.get(0).get("RID"));
		toBeans.set("ORDIDX", moveFromMap.get("ORDIDX"));
		fromBean.set("RID", moveFromMap.get("RID"));
		fromBean.set("ORDIDX", move2List.get(0).get("ORDIDX"));

		if (update(hmLogSql, toBeans) != 1) {
			return setFailInfo(dc, "排序保存失败！");
		}
		if (update(hmLogSql, fromBean) != 1) {
			return setFailInfo(dc, "排序保存失败！");
		}

		// sybase:substring,char_length
		// oracle:substr,length
		// 更新上下文ORDIDX
		String allordidx = String.valueOf(moveFromMap.get("ALLORDIDX")); // 上下文排序号，格式如0.0001.0001,0.0002.0002,0.0011.0005
		String replaceallordidx = String.valueOf(move2List.get(0).get("ALLORDIDX"));

		// sybase:substring,char_length,oracle:substr,length

		// StringBuffer replacesql = new StringBuffer("update SYS_RESOURCES set ALLORDIDX='aaaa'||ALLORDIDX where ALLORDIDX like '").append(allordidx).append("%'");
		StringBuffer replacesql = new StringBuffer("update SYS_RESOURCES set ALLORDIDX=");
		replacesql.append(ConvertSqlDefault.getStrJoinSQL("'aaaa'", "ALLORDIDX")).append(" where ALLORDIDX like '").append(allordidx).append("%'");
		// 1-->a
		if (executeSql(hmLogSql, replacesql.toString(), new ArrayList<Object>()) != 1) {
			return setFailInfo(dc, "保存失败！");
		}

		// 2-->1
		replacesql = new StringBuffer("update SYS_RESOURCES set ALLORDIDX=");
		replacesql.append(ConvertSqlDefault.getStrJoinSQL("'" + allordidx + "'", ConvertSqlDefault.isnullSQL(ConvertSqlDefault.substrSQL("ALLORDIDX", (allordidx.length() + 1) + "", ConvertSqlDefault.lengthSQL("ALLORDIDX")), "''")));
		replacesql.append(" where ALLORDIDX like '").append(replaceallordidx).append("%'");

		if (executeSql(hmLogSql, replacesql.toString(), new ArrayList<Object>()) != 1) {
			return setFailInfo(dc, "保存失败！");
		}

		// a-->2
		replacesql = new StringBuffer("update SYS_RESOURCES set ALLORDIDX=");
		replacesql.append(ConvertSqlDefault.getStrJoinSQL("'" + replaceallordidx + "'", ConvertSqlDefault.isnullSQL(ConvertSqlDefault.substrSQL("ALLORDIDX", (allordidx.length() + 5) + "", ConvertSqlDefault.lengthSQL("ALLORDIDX")), "''")));
		replacesql.append(" where ALLORDIDX like 'aaaa%'");

		// replacesql = new StringBuffer("update SYS_RESOURCES set ALLORDIDX='").append(replaceallordidx).append("'||nvl(substr(ALLORDIDX,").append(allordidx.length() + 5).append(",length(ALLORDIDX)),'') where ALLORDIDX like 'aaaa%'");
		if (executeSql(hmLogSql, replacesql.toString(), new ArrayList<Object>()) != 1) {
			return setFailInfo(dc, "保存失败！");
		}

		dc.setBusiness("RID", rid);
		return setSuccessInfo(dc, "更新成功！");
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
