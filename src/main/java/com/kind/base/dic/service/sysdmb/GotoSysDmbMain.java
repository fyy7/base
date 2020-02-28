package com.kind.base.dic.service.sysdmb;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author WangXiaoyi
 *
 *         2018年2月28日
 */
@Service
@Action(requireLogin = true, action = "#sys_dmb_main", description = "系统代码管理主页", powerCode = "dictionary.sysdmb", requireTransaction = false)
public class GotoSysDmbMain extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// JSONObject cloudJson = CloudTool.getCloudJson(dc, "APP_LIST");
		// if (cloudJson.getShort(Constant.FRAMEWORK_G_RESULT) == 0) {
		// return setFailInfo(dc, cloudJson.getString(Constant.FRAMEWORK_G_MESSAGE));
		// }
		// dc.setBusiness("SYS_DMB_APP", cloudJson.get("APP_LIST"));

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "dic/sysdmb/form/sys_dmb_main";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
