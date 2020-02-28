package com.kind.base.dic.service.codecategory;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author LIUHAORAN
 *
 *         2018年2月27日
 */
@Service
@Action(requireLogin = true, action = "#codecategory_main", description = "数据字典分类管理主页", powerCode = "dictionary.codecategory", requireTransaction = false)
public class GotoCodecategoryMain extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		// JSONObject cloudJson = CloudTool.getCloudJson(dc, "APP_LIST");
		// if (cloudJson.getShort(Constant.FRAMEWORK_G_RESULT) == 0) {
		// return setFailInfo(dc, cloudJson.getString(Constant.FRAMEWORK_G_MESSAGE));
		// }
		// dc.setBusiness("BEAN_APP", cloudJson.get("APP_LIST"));

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "dic/codecategory/codecategory_main";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
