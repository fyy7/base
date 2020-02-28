package com.kind.base.user.service.organ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.stream.MqBean;
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
@Action(requireLogin = true, action = "#organ_reset", description = "重新设置机构排序字段", powerCode = "userpower.organ", requireTransaction = true)
public class ResetOrganIndex extends BaseActionService {
	MqBean mqBean = new MqBean();

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		List<Object> paramList = new ArrayList<Object>();
		String sql = "SELECT * FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND PARENTID='OrganizationTop' ORDER BY ORDIDX";
		List<Map<String, Object>> organs = this.getSelectList(hmLogSql, sql, paramList);
		String parentallordidx = "0";
		for (int i = 0; i < organs.size(); i++) {
			// allordidx补0
			String allordidx = parentallordidx + "." + String.valueOf(new java.text.DecimalFormat("0000").format(i + 1));
			String organId = String.valueOf(organs.get(0).get("ORGANID"));

			DataBean bean = new DataBean("SYS_ORGANIZATION_INFO", "ORGANID");
			bean.set("NLEVEL", 1);
			bean.set("ORDIDX", String.valueOf(i + 1));
			bean.set("ALLORDIDX", allordidx);
			bean.set("ORGANID", organId);

			if (this.update(hmLogSql, bean) != 1) {
				return this.setFailInfo(dc, "更新失败！");
			}

			if (updateAllOrdIdx(dc, organId, allordidx, 1, hmLogSql) != 1) {
				return this.setFailInfo(dc, "更新失败！");
			}

		}

		return this.setSuccessInfo(dc, "更新成功！");
	}

	private int updateAllOrdIdx(DataContext ac, String oid, String parentallordidx, int rlevel, HashMap<String, String> hmLogSql) {
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(oid);
		String sql = "SELECT * FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND PARENTID=? ORDER BY ORDIDX";
		List<Map<String, Object>> organs = this.getSelectList(hmLogSql, sql, paramList);

		for (int i = 0; i < organs.size(); i++) {
			// allordidx补0
			String allordidx = parentallordidx + "." + String.valueOf(new java.text.DecimalFormat("0000").format(i + 1));
			String organId = String.valueOf(organs.get(i).get("ORGANID"));

			DataBean bean = new DataBean("SYS_ORGANIZATION_INFO", "ORGANID");
			bean.set("NLEVEL", String.valueOf(rlevel + 1));
			bean.set("ORDIDX", String.valueOf(i + 1));
			bean.set("ALLORDIDX", allordidx);
			bean.set("ORGANID", organId);

			if (this.update(hmLogSql, bean) != 1) {
				return 0;
			}

			// 递归执行
			if (updateAllOrdIdx(ac, organId, allordidx, rlevel + 1, hmLogSql) != 1) {
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
