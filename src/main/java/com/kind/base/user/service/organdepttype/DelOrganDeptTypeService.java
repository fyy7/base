package com.kind.base.user.service.organdepttype;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * 
 * @author chenzhiwei
 * 
 *         2018年4月24日
 */

@Service
@Action(requireLogin = true, action = "#organ_dept_type_delete", description = "机构部门类型删除", powerCode = "", requireTransaction = true)
public class DelOrganDeptTypeService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		DataBean bean = getBean(dc, "ORGAN_DMB", "DMLX,DM,ORGAN_ID");
		bean.set("DMLX", dc.getRequestString("DMLX"));
		bean.set("DM", dc.getRequestString("DM"));
		bean.set("ORGAN_ID", dc.getRequestString("ORGAN_ID"));
		int result = delete(hmLogSql, bean);
		if (result != 1) {
			return setFailInfo(dc, "删除失败！");
		} else {
			return setSuccessInfo(dc, "删除成功！");
		}
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
