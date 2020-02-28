package com.kind.base.user.service.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kind.common.stream.MqBean;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年2月22日
 */
@Service
@Action(requireLogin = true, action = "#user_delete", description = "删除用户", powerCode = "register.user", requireTransaction = true)
public class DeleteUser extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		List<MqBean> mqList = new ArrayList<>();
		MqBean mqBean = new MqBean();

		String privalue = dc.getRequestString("OPNO");
		if (StrUtil.isEmpty(privalue)) {
			return this.setFailInfo(dc, "非法删除！");
		}

		DataBean bean = new DataBean("SYS_N_USERS", "OPNO");
		bean.set("ISDEL", 1);
		bean.set("OPNO", privalue);

		// 删除参数
		mqBean = new MqBean(bean, MqBean.CMD_U, null);
		mqList.add(mqBean);
		if (this.update(hmLogSql, bean) == 0) {
			return this.setFailInfo(dc, "删除失败！");
		}

		// 删除成功提示
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
