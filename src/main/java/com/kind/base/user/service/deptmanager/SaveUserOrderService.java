package com.kind.base.user.service.deptmanager;

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
 * @author chenzhiwei
 *
 *         2018年4月10日
 */
@Service
@Action(requireLogin = true, action = "#user_order_save", description = "保存用户排序", powerCode = "", requireTransaction = true)
public class SaveUserOrderService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String deptId = dc.getRequestString("DEPTID");
		String unit = dc.getRequestString("ORGANID");
		String[] user_order = null;
		String[] opno = null;

		List<MqBean> mqList = new ArrayList<>();
		MqBean mqBean = new MqBean();
		try {
			user_order = (String[]) dc.getRequestObject("ORDIDX");
			opno = (String[]) dc.getRequestObject("OPNO");
		} catch (Exception e) {
			DataBean bean = this.getBean(dc, "SYS_N_USER_DEPT_INFO", "OPNO,DEPT_ID,ORGAN_ID");
			bean.set("DEPT_ID", deptId);
			bean.set("ORGAN_ID", unit);
			bean.set("ORDIDX", dc.getRequestObject("ORDIDX"));
			bean.set("OPNO", dc.getRequestObject("OPNO"));
			if (this.update(hmLogSql, bean) == 0) {
				return this.setFailInfo(dc, "执行失败");
			}

			mqBean = new MqBean(bean, MqBean.CMD_U, null);
			mqList.add(mqBean);
			if (mqList.size() > 0) {
				return this.setSuccessInfo(dc, "保存成功！");
			}
		}
		if (user_order != null) {
			for (int i = 0; i < user_order.length; i++) {
				DataBean bean = this.getBean(dc, "SYS_N_USER_DEPT_INFO", "OPNO,DEPT_ID,ORGAN_ID");
				bean.set("DEPT_ID", deptId);
				bean.set("ORGAN_ID", unit);
				bean.set("ORDIDX", Integer.valueOf(user_order[i]));
				bean.set("OPNO", opno[i]);
				if (this.update(hmLogSql, bean) == 0) {
					return this.setFailInfo(dc, "执行失败");
				}

				mqBean = new MqBean(bean, MqBean.CMD_U, null);
				mqList.add(mqBean);
			}

			if (mqList.size() > 0) {
				return this.setSuccessInfo(dc, "保存成功！");
			}
		}
		return this.setFailInfo(dc, "保存失败！");
	}

	@Override
	public String pageAddress(DataContext arg0) {
		return null;
	}

	@Override
	public String verifyParameter(DataContext arg0) {
		return null;
	}

}
