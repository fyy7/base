package com.kind.base.dic.service.codecategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
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
@Action(requireLogin = true, action = "#codecategory_edit", description = "参数管理修改页", powerCode = "dictionary.codecategory_2", requireTransaction = false)
public class GotoCodecategoryEdit extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String cmd = dc.getRequestString("CMD");
		String uuid = dc.getRequestString("UUID");
		if (ConCommon.CMD_U.equals(cmd) && !StrUtil.isEmpty(uuid)) {
			String sql = "SELECT CNO,CNAME,ALLOWMGR,ALLOWEXPORT,NOTES,ALLOWEVA,APP_ID,UUID FROM D_CODECATEGORY WHERE UUID=?";
			List<Object> param = new ArrayList<Object>();
			param.add(uuid);
			Map<String, Object> bean = this.getSelectMap(hmLogSql, sql, param);
			dc.setBusiness("BEAN", bean);
		} else {
			Map<String, Object> map = new HashMap<>(16);
			map.put("APP_ID", null);
			dc.setBusiness("BEAN", map);
		}

		dc.setBusiness("CMD", cmd);

		// JSONObject cloudJson = CloudTool.getCloudJson(dc, "APP_LIST");
		// if (cloudJson.getShort(Constant.FRAMEWORK_G_RESULT) == 0) {
		// return setFailInfo(dc, cloudJson.getString(Constant.FRAMEWORK_G_MESSAGE));
		// }
		// dc.setBusiness("BEAN_APP", cloudJson.get("APP_LIST"));

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "dic/codecategory/codecategory_edit";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
