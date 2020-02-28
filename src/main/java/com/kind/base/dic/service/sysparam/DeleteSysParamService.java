package com.kind.base.dic.service.sysparam;

import java.util.Arrays;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.common.utils.CodeSwitching;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author majiantao
 *
 *         2018年2月27日
 */
@Service
@Action(requireLogin = true, action = "#delete_sysparam", description = "参数管理数据删除", powerCode = "dictionary.parameter_8", requireTransaction = true)
public class DeleteSysParamService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String uuid = dc.getRequestString("UUID");

		DataBean bean = getSelectDataBean(hmLogSql, "SELECT * FROM SYS_SYSPARM WHERE UUID=?", Arrays.asList(uuid));
		bean.setKeyField("UUID");
		bean.setTableName("SYS_SYSPARM");
		bean.set("UUID", uuid);
		int result = 0;

		result = this.delete(hmLogSql, bean);
		if (result != 1) {
			return setFailInfo(dc, "删除失败！");
		}
		// 重置系统参数radis缓存
		CodeSwitching.putDataByFlag("sys");

		return setSuccessInfo(dc, "删除成功！");
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
