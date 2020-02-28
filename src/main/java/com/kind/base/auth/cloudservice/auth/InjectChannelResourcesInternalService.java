package com.kind.base.auth.cloudservice.auth;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.kind.base.core.auth.ResourceService;
import com.kind.framework.auth.ResourceInfo;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.ResultBean;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

/**
 * 注入渠道系统资源，供内部系统调用
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "#inject_channel_resource_internal", description = "注入渠道系统资源，供内部系统调用", powerCode = "", requireTransaction = true)
public class InjectChannelResourcesInternalService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(InjectChannelResourcesInternalService.class);

	/**
	 * http://my.kind.com/nportal/restful?action=inject_channel_resource_internal&TOKEN=&APP_ID=APP0002&RESOURCES=RTP001,TSTRID001,测试资源01,ResourceTop|RTP001,TSTRID002,测试资源02,TSTRID001|RTP001,TSTRID002,测试资源02,TSTRID001|RTP001,TSTRID003,测试资源03,TSTRID002
	 */
	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		ResourceService resourceService = SpringUtil.getBean(ResourceService.class);

		Map<String, Object> rm = this.getSuccessBaseResultMap();
		String msg = "";
		try {
			// String token = dc.getRequestObject("TOKEN", "");
			String channelId = dc.getRequestObject("APP_ID", "");

			// 需要注入的资源，格式：资源类型,资源ID,资源显示名称,父级资源ID| ...
			String resourcesData = dc.getRequestObject("RESOURCES", "");

			String[] resourceInfos = resourcesData.split("\\|");
			Set<ResourceInfo> resources = new HashSet<ResourceInfo>();
			for (String resourceInfo : resourceInfos) {
				String[] fields = resourceInfo.split(",");
				if (fields.length < 4) {
					msg = "资源字段格式不正确[" + resourceInfo + "]，注入失败！";
					rm = this.getBaseResultMap(0, msg);
					log.info(msg + " 资源数据：" + resourcesData);

					dc.getBusiness().putAll(rm);
					return setSuccessInfo(dc);
				}

				ResourceInfo resource = new ResourceInfo();
				resource.setResourceChannelId(channelId);
				resource.setResourceType(fields[0]);
				resource.setResourceId(fields[1]);
				resource.setResourceTitle(fields[2]);
				resource.setResourceParentId(fields[3]);

				resources.add(resource);
			}

			// 资源数据入库
			ResultBean rb = resourceService.injectResources(channelId, resources);
			if (!rb.getSuccess()) {
				rm = this.getBaseResultMap(0, rb.getMsg());
			}
		} catch (Exception e) {
			msg = "[internal]资源注入异常！";
			rm = this.getBaseResultMap(0, msg);
			log.info(msg + System.getProperty("line.separator") + getStackTrace(e));

			dc.getBusiness().putAll(rm);

			return setFailInfo(dc);
		}

		dc.getBusiness().putAll(rm);

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "common/json";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

}
