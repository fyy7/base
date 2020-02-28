package com.kind.base.user.service.organ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.stream.MqBean;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author LIUHAORAN
 *
 *         2018年2月22日
 */
@Service
@Action(requireLogin = true, action = "#organ_deletes", description = "删除组织结构", powerCode = "userpowers.organ", requireTransaction = true)
public class DeleteOrgan extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// 消息List
		List<MqBean> mqList = new ArrayList<>();

		String oid = dc.getRequestString("ORGANID");

		List<Object> paramList = new ArrayList<Object>();
		paramList.add(oid);
		String parentCountSql = "SELECT COUNT(1) FROM SYS_ORGANIZATION_INFO WHERE PARENTID=? AND ISDEL=0";
		String count = String.valueOf(this.getOneFiledValue(hmLogSql, parentCountSql, paramList));
		if (!ConCommon.NUM_0.equals(count)) {
			return this.setFailInfo(dc, "组织机构存在子机构，不能删除！");
		}

		String selectCountSql2 = "SELECT COUNT(1) FROM SYS_DEPARTMENT_INFO WHERE ORGANID=? AND ISDEL=0";
		count = String.valueOf(this.getOneFiledValue(hmLogSql, selectCountSql2, paramList));
		if (!ConCommon.NUM_0.equals(count)) {
			return this.setFailInfo(dc, "组织机构存在部门，不能删除！");
		}

		DataBean bean = new DataBean("SYS_ORGANIZATION_INFO", "ORGANID");
		bean.set("ISDEL", 1);
		bean.set("ORGANID", oid);
		MqBean mqBean = new MqBean(bean, MqBean.CMD_U, null);
		mqList.add(mqBean);

		if (this.update(hmLogSql, bean) == 0) {
			return this.setFailInfo(dc, "删除组织机构失败！");
		}

		// 同时清理机构管理员
		bean = new DataBean("SYS_N_USERS", "OPNO,OPTYPE");
		bean.set("ISDEL", 1);
		bean.set("OPTYPE", 1);
		bean.set("OPNO", oid);

		mqBean = new MqBean(bean, MqBean.CMD_U, null);
		mqList.add(mqBean);
		if (this.update(hmLogSql, bean) == 0) {
			return this.setFailInfo(dc, "删除机构管理员失败！");
		}
		return this.setSuccessInfo(dc, "删除成功！");
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
