package com.kind.base.workflow.test;

import java.util.HashMap;

import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.workflow.bo.WFlowInstanceBean;
import com.kind.workflow.instance.service.BusinessFormInstance;

public class BusinessFormInstanceImpl extends BusinessFormInstance {

	@Override
	public int ruleCode(DataContext dc, String instanceUuid, BaseActionService baseService, WFlowInstanceBean instanceBean, HashMap<String, String> hmLogSql) throws Exception {
		instanceBean.setInstanceName("测试流程更新扩展字段001");
		// 开始更新标记
		instanceBean.setUpdateFlag(1);
		instanceBean.set("c10_0", "c10_0字段");
		instanceBean.set("c30_1", "c30_1字段");

		return 1;
	}

}
