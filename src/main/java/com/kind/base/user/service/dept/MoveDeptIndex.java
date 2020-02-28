package com.kind.base.user.service.dept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author LIUHAORAN
 *
 *         2018年2月28日
 */
@Service
@Action(requireLogin = true, action = "#dept_move", description = "上下移动部门", powerCode = "userpower.branch", requireTransaction = true)
public class MoveDeptIndex extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String did = dc.getRequestString("DEPTID");
		String flag = dc.getRequestString("FLAG");

		/* 查询移动顺序的部门数据 */
		List<Object> paramList = new ArrayList<Object>();
		String selectDeptSql = "SELECT * FROM SYS_DEPARTMENT_INFO WHERE ISDEL=0 AND DEPTID=?";
		paramList.add(did);
		List<Map<String, Object>> deptListMap = this.getSelectList(hmLogSql, selectDeptSql, paramList);
		if (deptListMap == null || deptListMap.size() != 1) {
			return this.setFailInfo(dc, "获取资源信息失败！");
		}
		Object ordidx = deptListMap.get(0).get("ORDIDX");

		/* 查询目标部门的数据 */
		List<Map<String, Object>> replacedept;
		String sql;
		if (ConCommon.NUM_1.equals(flag)) {
			// 上移
			sql = "SELECT * FROM SYS_DEPARTMENT_INFO WHERE ISDEL=0 AND PARENTID=? and DEPTID<>? AND ORDIDX<? ORDER BY ORDIDX DESC";
		} else {
			// 下移
			sql = "SELECT * FROM SYS_DEPARTMENT_INFO WHERE ISDEL=0 AND PARENTID=? and DEPTID<>? AND ORDIDX>? ORDER BY ORDIDX";
		}
		paramList.clear();
		paramList.add(deptListMap.get(0).get("PARENTID"));
		paramList.add(did);
		paramList.add(ordidx);
		replacedept = this.getSelectList(hmLogSql, sql, paramList);
		if (replacedept == null || replacedept.size() == 0) {
			return this.setSuccessInfo(dc, "没有可移动的目标！");
		}

		DataBean bean = new DataBean("SYS_DEPARTMENT_INFO", "DEPTID");
		bean.set("DEPTID", did);
		bean.set("ORDIDX", replacedept.get(0).get("ORDIDX"));

		DataBean replaceBean = new DataBean("SYS_DEPARTMENT_INFO", "DEPTID");
		replaceBean.set("DEPTID", replacedept.get(0).get("DEPTID"));
		replaceBean.set("ORDIDX", ordidx);

		if (this.update(hmLogSql, bean) != 1) {
			return this.setFailInfo(dc, "排序保存失败！");
		}

		if (this.update(hmLogSql, replaceBean) != 1) {
			return this.setFailInfo(dc, "排序保存失败！");
		}

		// 更新上下文ORDIDX 上下文排序号，格式如0.0001.0001,0.0002.0002,0.0011.0005
		String allordidx = String.valueOf(deptListMap.get(0).get("ALLORDIDX"));
		String replaceallordidx = String.valueOf(replacedept.get(0).get("ALLORDIDX"));

		StringBuffer replacesql = new StringBuffer("UPDATE SYS_DEPARTMENT_INFO SET ALLORDIDX=");
		replacesql.append(ConvertSqlDefault.getStrJoinSQL("'aaaa'", ConvertSqlDefault.isnullSQL(ConvertSqlDefault.substrSQL("ALLORDIDX", (allordidx.length() + 1) + "", ConvertSqlDefault.lengthSQL("ALLORDIDX")), "''"))).append(" where ALLORDIDX like ?");
		paramList.clear();
		// paramList.add(allordidx + "%");
		ConvertSqlDefault.addLikeEscapeStr(replacesql, allordidx, "-%", paramList);

		// 1-->a
		if (this.executeSql(hmLogSql, replacesql.toString(), paramList) != 1) {
			return this.setFailInfo(dc, "保存失败！");
		}

		// 2-->1
		replacesql = new StringBuffer("UPDATE SYS_DEPARTMENT_INFO SET ALLORDIDX=");
		replacesql.append(ConvertSqlDefault.getStrJoinSQL("'" + allordidx + "'", ConvertSqlDefault.isnullSQL(ConvertSqlDefault.substrSQL("ALLORDIDX", (allordidx.length() + 1) + "", ConvertSqlDefault.lengthSQL("ALLORDIDX")), "''"))).append(" where ALLORDIDX like ?");
		paramList.clear();
		// paramList.add(replaceallordidx + "%");
		ConvertSqlDefault.addLikeEscapeStr(replacesql, replaceallordidx, "-%", paramList);

		if (this.executeSql(hmLogSql, replacesql.toString(), paramList) != 1) {
			return this.setFailInfo(dc, "保存失败！");
		}

		// a-->2
		replacesql = new StringBuffer("UPDATE SYS_DEPARTMENT_INFO SET ALLORDIDX=");
		replacesql.append(ConvertSqlDefault.getStrJoinSQL("'" + replaceallordidx + "'", ConvertSqlDefault.isnullSQL(ConvertSqlDefault.substrSQL("ALLORDIDX", "5", ConvertSqlDefault.lengthSQL("ALLORDIDX")), "''"))).append(" where ALLORDIDX like 'aaaa%'");
		paramList.clear();

		if (this.executeSql(hmLogSql, replacesql.toString(), paramList) != 1) {
			return this.setFailInfo(dc, "保存失败！");
		}
		dc.setBusiness("DEPTID", did);

		return this.setSuccessInfo(dc, "保存成功！");
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
