package com.kind.base.dic.service.dcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.utils.CodeSwitching;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author LIUHAORAN
 *
 *         2018年3月13日
 */
@Service
@Action(requireLogin = true, action = "#dcode_delete", description = "参数管理数据删除", powerCode = "dictionary.code_8", requireTransaction = true)
public class DeleteDcode extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		List<Object> paramList1 = new ArrayList<Object>();
		String categoryno = dc.getRequestString("CATEGORYNO2");
		String cno = dc.getRequestString("CNO2");
		String appId = dc.getRequestString("APP_ID2");

		String deleteSql = "DELETE FROM D_CODE WHERE APP_ID=? AND CATEGORYNO = ? AND CNO = ?";
		paramList1.add(appId);
		paramList1.add(categoryno);
		paramList1.add(cno);
		int result = this.executeSql(hmLogSql, deleteSql, paramList1);

		if (result == 1) {
			/*
			 * 删除子代码， 需求不确定注释
			 * 
			 * String deleteChildSql = "DELETE FROM D_CODE WHERE APP_ID=? AND ALLORDIDX LIKE ?";
			 * 
			 * paramList.clear();
			 * 
			 * paramList.add(appId);
			 * 
			 * paramList.add(allordidx+"%");
			 * 
			 * this.executeSql(hmLogSql, deleteChildSql, paramList);
			 */
			List<Object> paramList = new ArrayList<Object>();
			paramList.add(appId);
			paramList.add(categoryno);
			String sql = "SELECT COUNT(1) FROM D_CODE WHERE APP_ID=? AND CATEGORYNO = ?";
			String count = String.valueOf(this.getOneFiledValue(hmLogSql, sql, paramList));
			if (ConCommon.NUM_0.equals(count)) {
				CodeSwitching.removeByCompleteKey(appId + ".code." + categoryno);
			} else {
				CodeSwitching.putDataByCompleteKey(appId + ".code." + categoryno);
			}

			return this.setSuccessInfo(dc, "删除成功!");
		}
		return this.setFailInfo(dc, "删除失败!");

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
