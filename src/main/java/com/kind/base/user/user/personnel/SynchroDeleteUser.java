package com.kind.base.user.user.personnel;

import java.util.Arrays;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.base.user.service.user.IMCommonOperate;
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
@Action(requireLogin = true, action = "#synchro_user_delete", description = "删除用户", powerCode = "userpower.personnel.synchro_3", requireTransaction = true)
public class SynchroDeleteUser extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String privalue = dc.getRequestString("OPNO");
		if (StrUtil.isEmpty(privalue)) {
			return this.setFailInfo(dc, "非法删除！");
		}
		// 判断
		if (getSelectMap(hmLogSql, "select 1 from hr_personnel_base_image where isdel=1 and personid=?", Arrays.asList(privalue)) == null) {
			return this.setFailInfo(dc, "人事信息该用户不为删除状态，无法直接删除！");
		}

		DataBean bean = new DataBean("SYS_N_USERS", "OPNO");
		bean.set("ISDEL", 1);
		bean.set("OPNO", privalue);

		// 删除参数
		if (this.update(hmLogSql, bean) == 0) {
			return this.setFailInfo(dc, "删除失败！");
		}
		if (this.executeSql(hmLogSql, "update hr_personnel_base_image set data_exchange_flag='0' where personid=?", Arrays.asList(privalue)) != 1) {
			return this.setFailInfo(dc, "跟新同步信息失败！");
		}

		if (IMCommonOperate.delUser(this, dc, privalue) == 0) {
			return 0;
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
