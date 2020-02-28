package com.kind.base.dic.service.sysdmb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kind.common.automation.ActPostposition;
import com.kind.common.constant.ConCommon;
import com.kind.common.utils.CodeSwitching;
import com.kind.common.utils.ConvertSql;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.Pagination;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * 
 * @author LIUHAORAN
 * 
 *         2018年4月20日
 */
public class SysDmbHandle {

	/**
	 * 代码表的查询
	 * 
	 * @param hmLogSql
	 * @param dc
	 * @param bas
	 * @return
	 */
	public int selectList(HashMap<String, String> hmLogSql, DataContext dc, BaseActionService bas) {
		List<Object> paramData = new ArrayList<Object>();
		ConvertSql convertSql = new ConvertSql(bas.getDbType());

		StringBuffer sql = new StringBuffer("SELECT ").append(convertSql.getStrJoinSQL("DMLX", "','", "DM")).append(" UUID,DM.DMLX,DM.DM,DM.DMNR,DM.BZ,BZSM FROM SYS_DMB DM WHERE 1=1");

		String dmlx = dc.getRequestString("DMLX");
		if (!StrUtil.isEmpty(dmlx)) {
			sql.append(" AND DMLX = ?");
			paramData.add(dmlx);
		}
		String dm = dc.getRequestString("DM");
		if (!StrUtil.isEmpty(dm)) {
			sql.append(" AND DM like ?");
			// paramData.add("%" + dm + "%");
			ConvertSqlDefault.addLikeEscapeStr(sql, dm, "%%", paramData);
		}
		String dmnr = dc.getRequestString("DMNR");
		if (!StrUtil.isEmpty(dmnr)) {
			sql.append(" AND DMNR like ?");
			// paramData.add("%" + dmnr + "%");
			ConvertSqlDefault.addLikeEscapeStr(sql, dmnr, "%%", paramData);
		}
		String bz = dc.getRequestString("BZ");
		if (!StrUtil.isEmpty(bz)) {
			sql.append(" AND BZ like ?");
			// paramData.add("%" + bz + "%");
			ConvertSqlDefault.addLikeEscapeStr(sql, bz, "%%", paramData);
		}
		sql.append(" ORDER BY DMLX,DM");

		String page = dc.getRequestString("page");
		String pagesize = dc.getRequestString("rows");
		if (StrUtil.isBlank(page)) {
			page = "1";
		}
		if (StrUtil.isBlank(pagesize)) {
			pagesize = "10";
		}
		Pagination pdata = bas.getPagination(hmLogSql, convertSql.replace(sql), Integer.parseInt(page), Integer.parseInt(pagesize), paramData);
		dc.setBusiness(ConCommon.PAGINATION_ROWS, pdata.getResultList());
		dc.setBusiness(ConCommon.PAGINATION_TOTAL, pdata.getTotalRows());
		// 代码转换
		JSONObject config = new JSONObject();
		config.put("type", "LISTTURNCODE");
		config.put("target", ConCommon.PAGINATION_ROWS);
		config.put("TURNCODE", JSONArray.parse("[{'field': 'APP','dmlx': 'COMMON.sys_appname','appid':'COMMON'}]"));
		JSONArray tmp = new JSONArray();
		tmp.add(config);
		ActPostposition.postpositionAction(dc, tmp);

		return bas.setSuccessInfo(dc);
	}

	/**
	 * 代码保存操作
	 * 
	 * @param hmLogSql
	 * @param dc
	 * @param bas
	 * @return
	 */
	public int saveData(HashMap<String, String> hmLogSql, DataContext dc, BaseActionService bas) {
		int result = 0;
		String cmd = dc.getRequestString("CMD");

		String dmlx = dc.getRequestString("DMLX");
		String dm = dc.getRequestString("DM");
		String oldDmlx = dc.getRequestString("OLD_DMLX");
		String oldDm = dc.getRequestString("OLD_DM");


		boolean illegalParam = StrUtil.isEmpty(dmlx) || StrUtil.isEmpty(dm) || (ConCommon.CMD_U.equals(cmd) && (StrUtil.isEmpty(oldDmlx) || StrUtil.isEmpty(oldDm)));
		if (illegalParam) {
			return bas.setFailInfo(dc, "非法参数！");
		}

		DataBean bean = bas.getBean(dc, "SYS_DMB", "DMLX,DM");

		boolean pkeyChange = ConCommon.CMD_U.equals(cmd) && !(StrUtil.equals(dmlx, oldDmlx) && StrUtil.equals(dm, oldDm));
		boolean add = ConCommon.CMD_A.equals(cmd);
		if (add || pkeyChange) {
			List<Object> param = new ArrayList<Object>();
			param.add(dmlx);
			param.add(dm);
			String sql = "SELECT * FROM SYS_DMB WHERE DMLX=? AND DM=? ";
			if (bas.getSelectMap(hmLogSql, sql, param) != null) {
				return bas.setFailInfo(dc, "该代码已经存在！");
			}
		}

		if (ConCommon.CMD_A.equals(cmd)) {
			String appid = dc.getRequestString("APP_ID");
			if(StrUtil.isEmptyOrUndefined(appid)) {
				bean.set("APP_ID", CodeSwitching.getSystemAppId());
			}
			result = bas.insert(hmLogSql, bean);
		} else if (ConCommon.CMD_U.equals(cmd)) {

			DataBean db = bas.getBean(dc, "SYS_DMB", "DMLX,DM");

			db.set("DMLX_OLD", oldDmlx);
			db.set("DM_OLD", oldDm);
			result = bas.update(hmLogSql, db);
		}

		if (result == 0) {
			return bas.setFailInfo(dc, "保存失败！");
		}

		/* 更新代码表缓存 */
		String oldKey = "dmb." + oldDmlx;
		String newKey = "dmb." + dmlx;
		if (!oldKey.equals(newKey)) {
			CodeSwitching.putDataByCompleteKey(oldKey);
		}
		CodeSwitching.putDataByCompleteKey(newKey);

		return bas.setSuccessInfo(dc, "保存成功！");
	}

	/**
	 * 代码表的删除操作
	 * 
	 * @param hmLogSql
	 * @param dc
	 * @param bas
	 * @return
	 */
	public int delData(HashMap<String, String> hmLogSql, DataContext dc, BaseActionService bas) {
		String privalue = dc.getRequestString("PRIVALUE");

		List<Object> param = new ArrayList<Object>();

		// 没有主键，不能删除
		if (StrUtil.isEmpty(privalue)) {
			return bas.setFailInfo(dc, "主键信息为空！");
		}

		// 生成删除语句
		String[] arrprivalue = privalue.split(",");
		boolean isTrue = arrprivalue.length < 2;
		if (isTrue) {
			return bas.setFailInfo(dc, "主键信息不符！");
		}
		String dmlx = arrprivalue[0];
		String dm = arrprivalue[1];

		/* 若直接删除代码类型，同时删除其附属代码 */
		boolean textIsDmlx = "DMLX".equals(dmlx);

		if (textIsDmlx) {
			String deleteSql = "DELETE FROM SYS_DMB WHERE DMLX=?";
			param.add(dm);

			if (bas.executeSql(hmLogSql, deleteSql, param) == 0) {
				return bas.setFailInfo(dc, "删除失败！");
			}

			// 更新代码表缓存
			CodeSwitching.removeByCompleteKey("dmb." + dm);
		}

		DataBean bean = bas.getBean(dc, "SYS_DMB", "DMLX,DM");
		bean.set("DMLX", dmlx);
		bean.set("DM", dm);

		if (bas.delete(hmLogSql, bean) == 0) {
			return bas.setFailInfo(dc, "删除失败！");
		}

		// 更新代码表缓存
		CodeSwitching.putDataByCompleteKey("dmb." + dmlx);

		return bas.setSuccessInfo(dc, "删除成功！");
	}
}
