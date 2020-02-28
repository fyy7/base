package com.kind.base.workflow.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kind.HandleSpringCloudService;
import com.kind.common.service.MicroServerUrlManager;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;
import com.kind.workflow.interfaces.IWFlowPowerCode;

import cn.hutool.core.util.StrUtil;

/**
 * 权限代码在业务系统上的处理
 *
 */
public class PowerCodeResourceImpl implements IWFlowPowerCode {
	private static com.kind.framework.log.Logger logger = com.kind.framework.log.LogService.getLogger(PowerCodeResourceImpl.class);

	@Override
	public int handle(BaseActionService service, DataContext dc, DataBean bean) {

		JSONObject json = new JSONObject();
		String rid = bean.getString("APP_ID") + "_wf";
		JSONArray resources = JSONObject.parseArray("[{'RID':'" + rid + "','TITLE':'工作流权限代码','PID':'" + rid + "','VIS':'1','EXT1':'workflow','GRUP':'1','ORD':'1','CHLD':[]}]");
		json.put("RID", rid + "_" + bean.getString("RESOURCEID"));
		json.put("TITLE", StrUtil.isBlank(bean.getString("REMARK")) ? bean.getString("RESOURCEID") : bean.getString("REMARK"));
		json.put("PID", rid);
		json.put("VIS", "1");
		json.put("EXT1", "workflow");
		json.put("GRUP", "0");
		json.put("CHLD", new JSONArray());

		json.put("CRTP", "00005");
		dc.setReqeust("INTERNAL", "1");

		resources.getJSONObject(0).getJSONArray("CHLD").add(json);

		HandleSpringCloudService hscs = SpringUtil.getBean("handleSpringCloudService", HandleSpringCloudService.class);
		dc.getRequest().put("APP_ID", bean.getString("APP_ID"));
		dc.getRequest().put("RESOURCES", resources.toJSONString());

		logger.debug(resources.toJSONString());

		// 微服务资源注入
		JSONObject jsonInfo = hscs.handleService(MicroServerUrlManager.getAuthServerUrl(), "inject_channel_resource", dc);
		if (jsonInfo.getShort(Constant.FRAMEWORK_G_RESULT) == 0) {
			return service.setFailInfo(dc, "获取微服务失败:" + jsonInfo.getString(Constant.FRAMEWORK_G_MESSAGE));
		}
		return 1;
	}

}
