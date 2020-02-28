package com.kind.base.syslog;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * @author zhengjiayun
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = true, action = "#main_syslogs", description = "系统日志查询", powerCode = "jb.basic.syslogs", requireTransaction = false)
public class MainSyslogsService extends BaseActionService {
	private com.kind.framework.log.Logger logger = com.kind.framework.log.LogService.getLogger(getClass());

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		/* 查询语句与条件 */
		// HandleSpringCloudService hscs = SpringUtil.getBean("handleSpringCloudService", HandleSpringCloudService.class);
		// JSONObject json = hscs.handleService(MicroServerUrlManager.getNPortalServerUrl(), "appconfig_list_all", dc);
		// if (json.getShort(Constant.FRAMEWORK_G_RESULT) == 0) {
		// logger.debug("-----获取微服务失败：" + json.getString(Constant.FRAMEWORK_G_MESSAGE));
		// return this.setFailInfo(dc, "获取微服务失败:" + json.getString(Constant.FRAMEWORK_G_MESSAGE));
		// } else {
		// dc.setBusiness("app_array", json.getJSONArray("APP_LIST"));
		// }

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "syslog/main";
	}
}
