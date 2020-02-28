package com.kind.base.dic.service.sysparam;

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
 * @author majiantao
 *
 *         2018年2月27日
 */
@Service
@Action(requireLogin = true, action = "#sysparam_modify", description = "参数管理修改页", powerCode = "dictionary.parameter_2", requireTransaction = false)
public class GotoSysParamModifyService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String cmd = dc.getRequestString("CMD");
		String uuid = dc.getRequestString("UUID");
		if (ConCommon.CMD_U.equals(cmd) && !StrUtil.isEmpty(uuid)) {
			String sql = "SELECT APP_ID,PARAID, PARANAME, PARAVALUE, INITDATE , UUID, NOTES FROM SYS_SYSPARM WHERE UUID=?";
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
		return "dic/sysparam/sysparam_modify";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
