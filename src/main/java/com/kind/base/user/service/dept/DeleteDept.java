package com.kind.base.user.service.dept;

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
@Action(requireLogin = true, action = "#dept_delete", description = "删除部门", powerCode = "userpower.branch", requireTransaction = true)
public class DeleteDept extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		List<MqBean> mqList = new ArrayList<>();

		List<Object> paramList = new ArrayList<Object>();
		String did = dc.getRequestString("DEPTID");
		String selectCountSql1 = "SELECT COUNT(1) FROM SYS_DEPARTMENT_INFO WHERE PARENTID=? AND ISDEL=0";
		paramList.add(did);
		String count = String.valueOf(this.getOneFiledValue(hmLogSql, selectCountSql1, paramList));
		if (!ConCommon.NUM_0.equals(count)) {
			return this.setFailInfo(dc, "部门存在子部门，不能删除！");
		}
		String selectCountSql2 = "SELECT COUNT(1) FROM SYS_N_USER_DEPT_INFO WHERE DEPT_ID=?";
		count = String.valueOf(this.getOneFiledValue(hmLogSql, selectCountSql2, paramList));
		if (!ConCommon.NUM_0.equals(count)) {
			return this.setFailInfo(dc, "部门存在人员，不能删除！");
		}

		DataBean bean = new DataBean("SYS_DEPARTMENT_INFO", "DEPTID");
		bean.set("ISDEL", 1);
		bean.set("DEPTID", did);
		MqBean mqBean = new MqBean(bean, MqBean.CMD_U, null);
		mqList.add(mqBean);
		if (this.update(null, bean) == 0) {
			return this.setFailInfo(dc, "删除资源失败！");
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
