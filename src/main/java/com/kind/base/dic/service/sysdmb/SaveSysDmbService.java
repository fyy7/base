package com.kind.base.dic.service.sysdmb;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * 
 * @author WangXiaoyi
 * 
 *         2018年2月28日
 */

@Service
@Action(requireLogin = true, action = "#sys_dmb_save", description = "保存系统代码", powerCode = "userpower.depttype_2", requireTransaction = true)
public class SaveSysDmbService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		return (new SysDmbHandle()).saveData(hmLogSql, dc, this);
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
