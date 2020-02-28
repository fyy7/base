package com.kind.base.user.service.organofficetype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kind.common.stream.MqBean;
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
@Action(requireLogin = true, action = "#organ_office_type_delete", description = "机构职务类型删除", powerCode = "", requireTransaction = true)
public class DelOrganOfficeTypeService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// 消息List
		List<MqBean> mqList = new ArrayList<>();

		DataBean bean = getBean(dc, "ORGAN_DMB", "DMLX,DM,ORGAN_ID");
		bean.set("DMLX", dc.getRequestString("DMLX"));
		bean.set("DM", dc.getRequestString("DM"));
		bean.set("ORGAN_ID", dc.getRequestString("ORGAN_ID"));
		int result = delete(hmLogSql, bean);

		MqBean mqBean = new MqBean(bean, MqBean.CMD_D, null);
		mqList.add(mqBean);

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
