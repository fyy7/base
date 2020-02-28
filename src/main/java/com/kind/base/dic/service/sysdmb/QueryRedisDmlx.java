package com.kind.base.dic.service.sysdmb;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.common.utils.CodeSwitching;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年3月13日
 */
@Service
@Action(requireLogin = true, action = "#query_redis_code_info", description = "代码类型选择项查询", powerCode = "", requireTransaction = false)
public class QueryRedisDmlx extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String dmlx = dc.getRequestString("dmlx");
		String dm = dc.getRequestString("dm");
		String type = dc.getRequestString("type");
		if (StrUtil.isEmpty(dmlx)) {
			return setFailInfo(dc, "参数不能为空!");
		}

		if (StrUtil.isEmpty(dm)) {
			if (StrUtil.isEmpty(type)) {
				type = "1";
			}
			switch (type.toUpperCase()) {
			case "0":
				dc.setBusiness("value", CodeSwitching.getReidiosJsonObj(dmlx));
				break;
			case "1":
				dc.setBusiness("value", CodeSwitching.getReidiosJsonArr(dmlx));
				break;
			}
		} else {
			dc.setBusiness("value", CodeSwitching.getReidiosJsonDm(dmlx, dm));
		}

		return setSuccessInfo(dc);
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
