package com.kind.base.blackip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 *
 */
@Service
@Action(requireLogin = true, action = "#blackip_save", description = "IP黑名单数据保存", powerCode = "", requireTransaction = true)
public class SaveBlackIpService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		DataBean bean = this.getBean(dc, "SYS_BLACK_IP", "BLACK_IP");
		String black_ip_old = dc.getRequestString("BLACK_IP_OLD");

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		bean.set("create_date", sdf.format(date));

		int result = 0;
		if (StrUtil.isNotBlank(black_ip_old)) {
			result = this.update(hmLogSql, bean);
		} else {
			result = this.insert(hmLogSql, bean);
		}

		if (result != 1) {
			return setFailInfo(dc, "保存失败！");
		} else {
			// 重载黑名单IP相关信息
			ReloadBackIpToRedis.reload();

			return setSuccessInfo(dc, "保存成功！");
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
