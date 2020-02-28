package com.kind.base.dic.service.dcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.utils.CodeSwitching;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年3月13日
 */
@Service
@Action(requireLogin = true, action = "#dcode_move", description = "上下移动DCODE代码", powerCode = "dictionary.code_2", requireTransaction = true)
public class MoveDcodeIndex extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String allOrdidx1 = null;
		String allOrdidx2 = null;
		String categoryno = dc.getRequestString("CATEGORYNO");
		String cno = dc.getRequestString("CNO");
		String parentno = dc.getRequestString("PARENTNO");
		String appId = dc.getRequestString("APP_ID");
		String style = dc.getRequestString("STYLE");

		List<Object> param = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("SELECT * FROM D_CODE WHERE APP_ID=? AND CATEGORYNO = ? AND PARENTNO = ? ORDER BY ORDIDX");
		param.add(appId);
		param.add(categoryno);
		param.add(parentno);
		List<Map<String, Object>> codes = this.getSelectList(hmLogSql, sql.toString(), param);

		if (ConCommon.NUM_1.equals(style)) {
			/* 上移 */
			int result = -1;
			for (int i = 0; i < codes.size(); i++) {
				if (StrUtil.equals(cno, String.valueOf(codes.get(i).get("CNO")))) {
					result = i - 1;
					break;
				}
			}
			if (result > -1) {
				sql.setLength(0);
				sql.append("UPDATE D_CODE SET ORDIDX = ?, ALLORDIDX = ? WHERE APP_ID = ? AND CATEGORYNO = ? AND CNO = ? AND PARENTNO = ?");
				List<Object> list = new ArrayList<>();
				list.add(Integer.valueOf(String.valueOf(codes.get(result + 1).get("ORDIDX"))));
				list.add(codes.get(result + 1).get("ALLORDIDX"));
				list.add(appId);
				list.add(codes.get(result).get("CATEGORYNO"));
				list.add(codes.get(result).get("CNO"));
				list.add(codes.get(result).get("PARENTNO"));
				if (this.executeSql(hmLogSql, sql.toString(), list) == 0) {
					return this.setFailInfo(dc);
				}

				list.clear();
				list.add(Integer.valueOf(String.valueOf(codes.get(result).get("ORDIDX"))));
				list.add(codes.get(result).get("ALLORDIDX"));
				list.add(appId);
				list.add(codes.get(result + 1).get("CATEGORYNO"));
				list.add(codes.get(result + 1).get("CNO"));
				list.add(codes.get(result + 1).get("PARENTNO"));
				if (this.executeSql(hmLogSql, sql.toString(), list) == 0) {
					return this.setFailInfo(dc);
				}

				allOrdidx1 = String.valueOf(codes.get(result + 1).get("ALLORDIDX"));
				allOrdidx2 = String.valueOf(codes.get(result).get("ALLORDIDX"));
			}
		} else {
			/* 下移 */
			int result = codes.size();
			for (int i = 0; i < codes.size(); i++) {
				if (StrUtil.isNotEmpty(cno) && cno.equals(codes.get(i).get("CNO"))) {
					result = i + 1;
					break;
				}
			}
			if (result < codes.size()) {
				sql.setLength(0);
				sql.append("UPDATE D_CODE SET ORDIDX = ?, ALLORDIDX = ? WHERE APP_ID = ? AND CATEGORYNO = ? AND CNO = ? AND PARENTNO = ?");
				List<Object> list = new ArrayList<>();
				list.add(Integer.valueOf(String.valueOf(codes.get(result - 1).get("ORDIDX"))));
				list.add(codes.get(result - 1).get("ALLORDIDX"));
				list.add(appId);
				list.add(codes.get(result).get("CATEGORYNO"));
				list.add(codes.get(result).get("CNO"));
				list.add(codes.get(result).get("PARENTNO"));
				if (this.executeSql(hmLogSql, sql.toString(), list) == 0) {
					return this.setFailInfo(dc);
				}

				list.clear();
				list.add(Integer.valueOf(String.valueOf(codes.get(result).get("ORDIDX"))));
				list.add(codes.get(result).get("ALLORDIDX"));
				list.add(appId);
				list.add(codes.get(result - 1).get("CATEGORYNO"));
				list.add(codes.get(result - 1).get("CNO"));
				list.add(codes.get(result - 1).get("PARENTNO"));

				if (this.executeSql(hmLogSql, sql.toString(), list) == 0) {
					return this.setFailInfo(dc);
				}

				allOrdidx1 = String.valueOf(codes.get(result - 1).get("ALLORDIDX"));
				allOrdidx2 = String.valueOf(codes.get(result).get("ALLORDIDX"));
			}
		}

		/* 更新子代码的ALLORDIDX字段 */
		if (allOrdidx1 != null && allOrdidx2 != null) {
			String allOrdidx1Temp = allOrdidx1.replace(".", "-");
			StringBuffer sb = new StringBuffer("UPDATE D_CODE SET ALLORDIDX=");
			sb.append(ConvertSqlDefault.getStrJoinSQL("?", ConvertSqlDefault.substrSQL("ALLORDIDX", String.valueOf(allOrdidx1.length() + 1), ConvertSqlDefault.lengthSQL("ALLORDIDX") + "-" + String.valueOf(allOrdidx1.length()))));
			sb.append(" WHERE APP_ID=? AND ALLORDIDX <> ? AND ALLORDIDX LIKE ?");

			param.clear();
			param.add(allOrdidx1Temp);
			param.add(appId);
			param.add(allOrdidx2);

			// param.add(allOrdidx2 + "%");
			ConvertSqlDefault.addLikeEscapeStr(sql, allOrdidx2, "-%", param);

			if (this.executeSql(hmLogSql, sb.toString(), param) == 0) {
				return this.setFailInfo(dc);
			}

			param.clear();
			param.add(allOrdidx2);
			param.add(appId);
			param.add(allOrdidx1);
			param.add(allOrdidx1 + "%");
			if (this.executeSql(hmLogSql, sb.toString(), param) == 0) {
				return this.setFailInfo(dc);
			}

			param.clear();
			param.add(allOrdidx1);
			param.add(appId);
			param.add(allOrdidx2);
			param.add(allOrdidx1Temp + "%");
			if (this.executeSql(hmLogSql, sb.toString(), param) == 0) {
				return this.setFailInfo(dc);
			}
		}
		CodeSwitching.putDataByCompleteKey(appId + ".code." + categoryno);

		return this.setSuccessInfo(dc, "移动成功！");
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
