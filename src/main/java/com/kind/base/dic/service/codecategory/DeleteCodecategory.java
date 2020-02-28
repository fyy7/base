package com.kind.base.dic.service.codecategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.utils.CodeSwitching;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年2月27日
 */
@Service
@Action(requireLogin = true, action = "#codecategory_delete", description = "参数管理数据删除", powerCode = "dictionary.codecategory_8", requireTransaction = true)
public class DeleteCodecategory extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		List<Object> paramList = new ArrayList<Object>();
		String uuid = dc.getRequestString("UUID");
		if (StrUtil.isEmpty(uuid)) {
			return this.setFailInfo(dc, "非法删除!");
		}
		String sql = "SELECT CNO,APP_ID FROM D_CODECATEGORY WHERE UUID=?";
		paramList.add(uuid);
		Map<String, Object> map = this.getSelectMap(hmLogSql, sql, paramList);

		// 删除数据字典
		String deleteDcodeSql = "DELETE FROM D_CODE WHERE EXISTS(SELECT 1 FROM D_CODECATEGORY B WHERE D_CODE.APP_ID=B.APP_ID AND D_CODE.CATEGORYNO=B.CNO AND B.UUID=?)";
		if (this.executeSql(hmLogSql, deleteDcodeSql, paramList) == 0) {
			return this.setFailInfo(dc, "删除失败");
		}

		// 删除数据字典
		String deleteCategorySql = "DELETE FROM D_CODECATEGORY WHERE UUID =?";
		if (this.executeSql(hmLogSql, deleteCategorySql, paramList) != 1) {
			return this.setFailInfo(dc, "删除参数失败!");
		}

		CodeSwitching.removeByCompleteKey(String.valueOf(map.get("APP_ID")) + ".code." + String.valueOf(map.get("CNO")));
		return this.setSuccessInfo(dc, "删除成功!");
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
