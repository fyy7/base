package com.kind.base.dic.service.codecategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.utils.CodeSwitching;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.GenID;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年2月27日
 */
@Service
@Action(requireLogin = true, action = "#codecategory_save", description = "参数管理数据保存", powerCode = "dictionary.codecategory_2", requireTransaction = true)
public class SaveCodecategoryService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		int result = 0;
		List<Object> paramList = new ArrayList<Object>();
		String cmd = dc.getRequestString("CMD");
		String oldCno = dc.getRequestString("OLD_CNO");
		String cno = dc.getRequestString("CNO");
		String appId = dc.getRequestString("APP_ID");
		if (StrUtil.isEmptyOrUndefined(appId)) {
			appId = CodeSwitching.getSystemAppId();
		}
		/* 验证CNO是否重复 */
		boolean pkChange = ConCommon.CMD_U.equals(cmd) && (!StrUtil.equals(oldCno, cno));
		boolean add = ConCommon.CMD_A.equals(cmd);
		DataBean bean = this.getBean(dc, "D_CODECATEGORY", "UUID");
		if (add && pkChange) {

			String checkRepeatSql = "SELECT COUNT(1) FROM D_CODECATEGORY WHERE  CNO=?";
			paramList.add(cno);
			String count = String.valueOf(this.getOneFiledValue(hmLogSql, checkRepeatSql, paramList));
			if (!ConCommon.NUM_0.equals(count)) {
				return this.setFailInfo(dc, "分类编号已存在!");
			}
		}

		/* 执行保存 */

		if (ConCommon.CMD_A.equals(cmd)) {
			bean.set("APP_ID", appId);
			bean.set("UUID", GenID.gen(36));
			result = this.insert(hmLogSql, bean);
		} else {
			result = this.update(hmLogSql, bean);
		}

		if (result == 0) {
			return this.setFailInfo(dc, "保存失败！");
		}

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
