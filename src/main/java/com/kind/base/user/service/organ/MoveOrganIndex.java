package com.kind.base.user.service.organ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.stream.MqBean;
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
@Action(requireLogin = true, action = "#organ_move", description = "上下移动机构", powerCode = "userpower.organ", requireTransaction = true)
public class MoveOrganIndex extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(MoveOrganIndex.class);

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// 消息List
		List<MqBean> mqList = new ArrayList<>();

		String oid = dc.getRequestString("ORGANID");
		String flag = dc.getRequestString("FLAG");

		/* 获取机构信息 */
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(oid);
		String selectOrganSql = "SELECT * FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND ORGANID=?";
		Map<String, Object> organMap = this.getSelectMap(hmLogSql, selectOrganSql, paramList);
		if (organMap == null) {
			return this.setFailInfo(dc, "获取机构信息失败！");
		}

		/* 查询移动时需要调换的目标机构信息 */
		Object ordidx = organMap.get("ORDIDX");
		String parentId = String.valueOf(organMap.get("PARENTID"));
		String sql;
		DataBean bean = new DataBean("SYS_ORGANIZATION_INFO", "ORGANID");
		bean.set("ORGANID", organMap.get("ORGANID"));
		if (ConCommon.NUM_1.equals(flag)) {
			// 上移
			sql = "SELECT * FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND PARENTID=? AND ORGANID<>? AND ORDIDX < ? ORDER BY ORDIDX DESC";
		} else {
			// 下移
			sql = "SELECT * FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND PARENTID=? AND ORGANID<>? AND ORDIDX > ? ORDER BY ORDIDX";
		}
		paramList.clear();
		paramList.add(parentId);
		paramList.add(oid);
		paramList.add(ordidx);
		List<Map<String, Object>> replaceListMap = this.getSelectList(hmLogSql, sql, paramList);
		if (replaceListMap == null || replaceListMap.size() == 0) {
			return this.setSuccessInfo(dc, "没有可移动的目标！");
		}
		Map<String, Object> replaceMap = replaceListMap.get(0);

		/* 更新本机构和目标机构排序字段 */
		String replaceOrdidx = String.valueOf(replaceMap.get("ORDIDX"));
		String replaceOrganId = String.valueOf(replaceMap.get("ORGANID"));

		bean.set("ORDIDX", replaceOrdidx);
		bean.setKeyField("ORGANID");
		bean.setTableName("SYS_ORGANIZATION_INFO");

		MqBean mqBean = new MqBean(bean, MqBean.CMD_U, null);
		mqList.add(mqBean);

		if (this.update(hmLogSql, bean) != 1) {
			return this.setFailInfo(dc, "排序保存失败！");
		}

		DataBean replaceBean = new DataBean("SYS_ORGANIZATION_INFO", "ORGANID");
		replaceBean.set("ORDIDX", ordidx);
		replaceBean.set("ORGANID", replaceOrganId);
		replaceBean.setKeyField("ORGANID");
		replaceBean.setTableName("SYS_ORGANIZATION_INFO");
		mqBean = new MqBean(replaceBean, MqBean.CMD_U, null);
		mqList.add(mqBean);
		if (this.update(hmLogSql, replaceBean) != 1) {
			return this.setFailInfo(dc, "排序保存失败！");
		}

		/* 更新本机构和目标机构的子机构的排序字段 */
		// 更新上下文ORDIDX,上下文排序号，格式如0.0001.0001,0.0002.0002,0.0011.0005
		String allordidx = String.valueOf(organMap.get("ALLORDIDX"));
		String replaceallordidx = String.valueOf(replaceMap.get("ALLORDIDX"));

		StringBuffer replacesql = new StringBuffer("UPDATE SYS_ORGANIZATION_INFO SET ALLORDIDX=");
		replacesql.append(ConvertSqlDefault.getStrJoinSQL("'TEMP'", ConvertSqlDefault.isnullSQL(ConvertSqlDefault.substrSQL("ALLORDIDX", (allordidx.length() + 1) + "", ConvertSqlDefault.lengthSQL("ALLORDIDX")), "''"))).append(" WHERE ALLORDIDX LIKE ?");
		// 1-->a
		paramList.clear();
		// paramList.add(allordidx + "%");
		ConvertSqlDefault.addLikeEscapeStr(replacesql, allordidx, "-%", paramList);

		mqBean = new MqBean(replacesql.toString(), paramList, null);
		mqList.add(mqBean);
		if (this.executeSql(hmLogSql, replacesql.toString(), paramList) == 0) {
			return this.setFailInfo(dc, "保存失败！");
		}

		// 2-->1
		replacesql = new StringBuffer("UPDATE SYS_ORGANIZATION_INFO SET ALLORDIDX=");
		replacesql.append(ConvertSqlDefault.getStrJoinSQL("'" + allordidx + "'", ConvertSqlDefault.isnullSQL(ConvertSqlDefault.substrSQL("ALLORDIDX", (allordidx.length() + 1) + "", ConvertSqlDefault.lengthSQL("ALLORDIDX")), "''"))).append(" WHERE ALLORDIDX LIKE ?");
		paramList.clear();
		// paramList.add(replaceallordidx + "%");
		ConvertSqlDefault.addLikeEscapeStr(replacesql, replaceallordidx, "-%", paramList);
		mqBean = new MqBean(replacesql.toString(), paramList, null);
		mqList.add(mqBean);
		if (this.executeSql(hmLogSql, replacesql.toString(), paramList) == 0) {
			return this.setFailInfo(dc, "保存失败！");
		}

		// a-->2
		replacesql = new StringBuffer("UPDATE SYS_ORGANIZATION_INFO SET ALLORDIDX=");
		replacesql.append(ConvertSqlDefault.getStrJoinSQL("'" + replaceallordidx + "'", ConvertSqlDefault.isnullSQL(ConvertSqlDefault.substrSQL("ALLORDIDX", "5", ConvertSqlDefault.lengthSQL("ALLORDIDX")), "''"))).append(" WHERE ALLORDIDX LIKE 'TEMP%'");
		paramList.clear();
		log.debug("a-->2:" + replacesql.toString());
		mqBean = new MqBean(replacesql.toString(), paramList, null);
		mqList.add(mqBean);
		if (this.executeSql(hmLogSql, replacesql.toString(), paramList) == 0) {
			return this.setFailInfo(dc, "保存失败！");
		}

		dc.setBusiness("ORGANID", oid);
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
