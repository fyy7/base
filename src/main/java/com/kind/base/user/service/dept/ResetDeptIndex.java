package com.kind.base.user.service.dept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author LIUHAORAN
 *
 *         2018年2月28日
 */
@Service
@Action(requireLogin = true, action = "#dept_reset", description = "重新设置部门排序字段", powerCode = "userpower.branch", requireTransaction = true)
public class ResetDeptIndex extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		List<Object> paramList = new ArrayList<Object>();
		String organId = dc.getRequestString("ORGANID");

		String selectDeptSql = "SELECT * FROM SYS_DEPARTMENT_INFO WHERE ISDEL=0 AND PARENTID=? ORDER BY ORDIDX";
		paramList.add(organId);
		List<Map<String, Object>> depts = this.getSelectList(hmLogSql, selectDeptSql, paramList);
		String parentallordidx = "0.0001";
		for (int i = 0; i < depts.size(); i++) {
			String allordidx = parentallordidx + "." + String.valueOf(new java.text.DecimalFormat("0000").format(i + 1));
			DataBean bean = new DataBean("SYS_DEPARTMENT_INFO", "DEPTID");
			bean.set("DLEVEL", 1);
			bean.set("ORDIDX", String.valueOf(i + 1));
			bean.set("ALLORDIDX", allordidx);
			bean.set("DEPTID", String.valueOf(depts.get(i).get("DEPTID")));

			if (this.update(hmLogSql, bean) != 1) {
				return this.setFailInfo(dc, "更新失败！");
			}

			if (updateAllOrdIdx(hmLogSql, String.valueOf(depts.get(i).get("DEPTID")), allordidx, 1) != 1) {
				return this.setFailInfo(dc, "更新失败！");
			}
		}

		return this.setSuccessInfo(dc, "更新成功！");
	}

	private int updateAllOrdIdx(HashMap<String, String> hmLogSql, String oid, String parentallordidx, int rlevel) {
		List<Object> paramList = new ArrayList<Object>();
		String sql = "SELECT * FROM SYS_DEPARTMENT_INFO WHERE ISDEL=0 AND PARENTID=? ORDER BY ORDIDX";
		paramList.add(oid);
		List<Map<String, Object>> depts = this.getSelectList(hmLogSql, sql, paramList);

		for (int i = 0; i < depts.size(); i++) {
			String allordidx = parentallordidx + "." + String.valueOf(new java.text.DecimalFormat("0000").format(i + 1));

			DataBean bean = new DataBean("SYS_DEPARTMENT_INFO", "DEPTID");
			bean.set("DLEVEL", String.valueOf(rlevel + 1));
			bean.set("ORDIDX", String.valueOf(i + 1));
			bean.set("ALLORDIDX", allordidx);
			bean.set("DEPTID", depts.get(i).get("DEPTID"));

			if (this.update(hmLogSql, bean) != 1) {
				return 0;
			}

			// 递归执行
			if (updateAllOrdIdx(hmLogSql, String.valueOf(depts.get(i).get("DEPTID")), allordidx, rlevel + 1) != 1) {
				return 0;
			}
		}
		return 1;
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
