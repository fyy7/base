package com.kind.base.core.log;

import com.kind.framework.core.DbHelper;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.log.ILogSave;

import cn.hutool.core.util.StrUtil;

/**
 * 日志保存类
 * 
 * @author zhengjiayun
 *
 *         2018年3月12日
 */
public class LogSave implements ILogSave {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(LogSave.class);

	@Override
	public void save(DataBean bean) {
		if (StrUtil.isNotBlank(bean.getString("OPNO"))) {
			// 采用异步方式处理日志保存
			LogSaveThread log = new LogSaveThread(bean);
			log.start();
		}
	}

	/**
	 * 日志保存异步处理
	 * 
	 * @author zhengjiayun
	 *
	 *         2018年3月12日
	 */
	class LogSaveThread extends Thread {
		private DataBean bean;

		public LogSaveThread(DataBean bean) {
			this.bean = bean;
		}

		@Override
		public void run() {
			// 这里先入库了，可以发给mq处理
			DbHelper dbhelper = new DbHelper();
			dbhelper.insert(null, bean);

			// HandleSpringCloudService hscs = SpringUtil.getBean("handleSpringCloudService", HandleSpringCloudService.class);
			// DataContext dc = new DataContext();
			//
			// dc.setBusiness("logBean", bean);
			//
			// log.debug("---------日志数据------：" + bean.getJsonString());
			//
			// JSONObject json = hscs.handleService(MicroServerUrlManager.getMonitorServerUrl(), "save_syslogs", dc);
			// if (json.getShort(Constant.FRAMEWORK_G_RESULT) == 0) {
			// log.debug("-----获取微服务失败：" + json.getString(Constant.FRAMEWORK_G_MESSAGE));
			// }
		}
	}
}
