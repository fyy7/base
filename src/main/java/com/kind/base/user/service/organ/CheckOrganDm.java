package com.kind.base.user.service.organ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author LIUHAORAN
 *
 *         2018年2月22日
 */
@Service
@Action(requireLogin = true, action = "#organ_checkdm", description = "检测机构代码", powerCode = "userpower.organ", requireTransaction = false)
public class CheckOrganDm extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String id = dc.getRequestString("ID");
		String ddm = dc.getRequestString("DM");
		String cmd = dc.getRequestString("CMD");

		List<Object> data = new ArrayList<Object>();
		String sql = "";
		if (ConCommon.CMD_U.equals(cmd)) {
			data.add(ddm);
			data.add(id);
			sql = "SELECT COUNT(1) AS NUM FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND ORGANDM=? AND ORGANID<>?";

		} else {
			data.add(ddm);
			sql = "SELECT COUNT(1) AS NUM FROM SYS_ORGANIZATION_INFO WHERE ISDEL=0 AND ORGANDM=?";
		}
		int count = 0;
		List<Map<String, Object>> cname = this.getSelectList(hmLogSql, sql.toString(), data);
		Object num = cname.get(0).get("NUM");
		if ((cname.size() > 0) && (num != null)) {
			count = ((Number) num).intValue();

		}
		dc.setBusiness("NUM", count);
		return this.setSuccessInfo(dc);
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
