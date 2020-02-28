package com.kind.base.dic.service.dcode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.utils.CodeSwitching;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.GenID;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年3月13日
 */
@Service
@Action(requireLogin = true, action = "#dcode_save", description = "参数管理数据保存", powerCode = "dictionary.code_2", requireTransaction = true)
public class SaveDcodeService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		int result;
		String appId = dc.getRequestString("APP_ID");
		if (StrUtil.isEmptyOrUndefined(appId)) {
			appId = CodeSwitching.getSystemAppId();
		}
		String cmd = dc.getRequestString("CMD");

		String uuid = ConCommon.CMD_A.equals(cmd) ? GenID.gen(36) : dc.getRequestString("UUID");
		String cno = dc.getRequestString("CNO");
		String categoryno = dc.getRequestString("CATEGORYNO");
		String parentno = dc.getRequestString("PARENTNO");
		String cvalue = dc.getRequestString("CVALUE");
		String ordidx = dc.getRequestString("ORDIDX");

		String oldCno = dc.getRequestString("OLD_CNO");
		String oldCategoryno = dc.getRequestString("OLD_CATEGORYNO");
		String oldParentno = dc.getRequestString("OLD_PARENTNO");
		String oldCvalue = dc.getRequestString("OLD_CVALUE");
		String oldOrdidx = dc.getRequestString("OLD_ORDIDX");

		/* 验证代码与代码名称是否重复 */
		List<Object> paramList = new ArrayList<Object>();
		boolean checkCnoRepeat = ConCommon.CMD_A.equals(cmd) || (ConCommon.CMD_U.equals(cmd) && !(StrUtil.equals(cno, oldCno) && StrUtil.equals(categoryno, oldCategoryno) && StrUtil.equals(cvalue, oldCvalue)));
		if (checkCnoRepeat) {
			String sql = "SELECT COUNT(1) FROM D_CODE WHERE APP_ID=? AND CATEGORYNO = ? AND (CNO = ? OR CVALUE = ?) AND UUID <> ?";
			paramList.add(appId);
			paramList.add(categoryno);
			paramList.add(cno);
			paramList.add(cvalue);
			paramList.add(uuid);
			int count = Integer.valueOf(String.valueOf(this.getOneFiledValue(hmLogSql, sql, paramList)));
			if (count > 0) {
				return this.setFailInfo(dc, "代码或代码名称重复！");
			}
		}
		/* 验证排序号是否重复 */
		boolean checkOrderRepeat = ConCommon.CMD_A.equals(cmd) || (ConCommon.CMD_U.equals(cmd) && !(StrUtil.equals(ordidx, oldOrdidx) && StrUtil.equals(parentno, oldParentno)));
		if (checkOrderRepeat) {
			String sql = "SELECT COUNT(1) FROM D_CODE WHERE APP_ID=? AND CATEGORYNO = ? AND PARENTNO = ? AND ORDIDX = ? AND UUID <> ?";
			paramList.clear();
			paramList.add(appId);
			paramList.add(categoryno);
			paramList.add(parentno);
			paramList.add(ordidx);
			paramList.add(uuid);
			int count = Integer.valueOf(String.valueOf(this.getOneFiledValue(hmLogSql, sql, paramList)));
			if (count > 0) {
				return this.setFailInfo(dc, "排序号重复！");
			}
		}

		DataBean bean = this.getBean(dc, "D_CODE", "UUID");
		bean.set("STDCODE", bean.get("CNO"));

		if (categoryno.equals(parentno)) {
			bean.set("ALLORDIDX", categoryno + "." + new DecimalFormat("0000").format(Integer.parseInt(ordidx)));
		} else {
			String selectAllOrdidxSql = "SELECT ALLORDIDX FROM D_CODE WHERE APP_ID=? AND CATEGORYNO = ? AND CNO = ?";
			paramList.clear();
			paramList.add(appId);
			paramList.add(categoryno);
			paramList.add(parentno);
			String newAllOrdidx = this.getOneFiledValue(hmLogSql, selectAllOrdidxSql, paramList).toString() + "." + new DecimalFormat("0000").format(Integer.parseInt(ordidx));
			bean.set("ALLORDIDX", newAllOrdidx);
		}

		if (ConCommon.CMD_A.equals(cmd)) {
			bean.set("UUID", uuid);
			result = this.insert(hmLogSql, bean);

		} else {
			result = this.update(hmLogSql, bean);

			// TODO edit by 20181031 更新下级代码的ALLORDIDX字段
			if (result == 1) {
				String oldAllOrdidx = dc.getRequestString("ALLORDIDX");
				StringBuffer sb = new StringBuffer("UPDATE D_CODE SET ALLORDIDX=");
				sb.append(ConvertSqlDefault.getStrJoinSQL("?", ConvertSqlDefault.substrSQL("ALLORDIDX", String.valueOf(oldAllOrdidx.length() + 1), ConvertSqlDefault.lengthSQL("ALLORDIDX") + "-" + String.valueOf(oldAllOrdidx.length()))));
				sb.append(" WHERE APP_ID=? AND ALLORDIDX <> ? AND ALLORDIDX LIKE ?");
				paramList.clear();
				paramList.add(bean.getString("ALLORDIDX"));
				paramList.add(appId);
				paramList.add(bean.getString("ALLORDIDX"));
				// paramList.add(oldAllOrdidx+"%");
				ConvertSqlDefault.addLikeEscapeStr(sb, oldAllOrdidx, "-%", paramList);
				result = this.executeSql(hmLogSql, sb.toString(), paramList);
			}
		}

		// 保存结果提示
		if (result == 0) {
			return this.setFailInfo(dc, "保存失败!");
		}

		CodeSwitching.putDataByCompleteKey(appId + ".code." + categoryno);
		CodeSwitching.putDataByCompleteKey(appId + ".code." + oldCategoryno);
		return this.setSuccessInfo(dc, "保存成功!");
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
