package com.kind.base.dic.service.dcode;

import java.util.HashMap;

import org.springframework.stereotype.Service;

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
@Action(requireLogin = true, action = "#dcode_main", description = "数据字典管理主页", powerCode = "dictionary.code", requireTransaction = false)
public class GotoDcodeMain extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String thisAppId = CodeSwitching.getSystemAppId();

		String rid = dc.getRequestString("RID");
		dc.setBusiness("RID", rid == null ? "" : rid);

		dc.setBusiness("THIS_APP_ID", thisAppId);
		// JSONArray array = CodeSwitching.getReidiosJsonArr("code.HR06", "APP0028");

		// JSONObject cloudJson = CloudTool.getCloudJson(dc, "APP_LIST");
		// if (cloudJson.getShort(Constant.FRAMEWORK_G_RESULT) == 0) {
		// return setFailInfo(dc, cloudJson.getString(Constant.FRAMEWORK_G_MESSAGE));
		// }
		// dc.setBusiness("BEAN_APP", cloudJson.get("APP_LIST"));

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "dic/dcode/dcode_main";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
