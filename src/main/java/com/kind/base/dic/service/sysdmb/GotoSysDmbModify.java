package com.kind.base.dic.service.sysdmb;

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
 * 
 * @author WangXiaoyi
 * 
 *         2018年2月28日
 */

@Service
@Action(requireLogin = true, action = "#sys_dmb_modify", description = "系统代码维护", powerCode = "dictionary.sysdmb_2", requireTransaction = false)
public class GotoSysDmbModify extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String privalue = dc.getRequestString("PRIVALUE");
		String appId = dc.getRequestString("APP_ID");
		String cmd = dc.getRequestString("CMD");
		if(StrUtil.isEmptyOrUndefined(appId)) {
			appId=CodeSwitching.getSystemAppId();
		}
		if (!StrUtil.isEmpty(privalue)) {
			// 如是其他字段类型需改此处
			String[] arrprivalue = privalue.split(",");
			boolean isTrue = arrprivalue.length < 2;
			if (isTrue) {
				return this.setFailInfo(dc, "主键信息不符！");
			}
			String sql = "SELECT * FROM SYS_DMB WHERE DMLX=? AND DM=? AND APP_ID=?";
			List<Object> param = new ArrayList<Object>();
			param.add(arrprivalue[0]);
			param.add(arrprivalue[1]);
			param.add(appId);
			dc.setBusiness("SYS_DMB_INFO", this.getSelectMap(hmLogSql, sql, param));
		} else {
			Map<String, Object> map = new HashMap<>(16);
			map.put("DMLX", null);
			map.put("APP_ID", appId);
			dc.setBusiness("SYS_DMB_INFO", map);
		}

		dc.setBusiness("CMD", cmd);

		// JSONObject cloudJson = CloudTool.getCloudJson(dc, "APP_LIST");
		// if (cloudJson.getShort(Constant.FRAMEWORK_G_RESULT) == 0) {
		// return setFailInfo(dc, cloudJson.getString(Constant.FRAMEWORK_G_MESSAGE));
		// }
		// dc.setBusiness("SYS_DMB_APP", cloudJson.get("APP_LIST"));

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "dic/sysdmb/form/sys_dmb_modify";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

}
