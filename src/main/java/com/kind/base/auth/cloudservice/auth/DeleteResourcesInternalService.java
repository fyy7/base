package com.kind.base.auth.cloudservice.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.base.core.auth.ResourceService;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.ResultBean;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

/**
 * 删除资源，供内部调用
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "#delete_resources_service_internal", description = "删除资源，供内部调用", powerCode = "", requireTransaction = true)
public class DeleteResourcesInternalService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(DeleteResourcesInternalService.class);

	/**
	 * http://my.kind.com/auth/restful?action=delete_resources_service&TOKEN=&APP_ID=APP0002&RTYPE=99999&RIDS=TSTRID001|TSTRID002|TSTRID003
	 */
	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		ResourceService resourceService = SpringUtil.getBean(ResourceService.class);

		// Set<ResourceInfo> resources = new HashSet<ResourceInfo>();
		Map<String, Object> rm = this.getSuccessBaseResultMap();
		String msg = "";
		try {
			// String token = dc.getRequestObject("TOKEN", "");
			String channelId = dc.getRequestObject("APP_ID", "");
			String rtype = dc.getRequestObject("RTYPE", ""); // 资源类型
			String rids = dc.getRequestObject("RIDS", ""); // 资源ID列表，格式：ID1|ID2...

			ResultBean rb = resourceService.deleteResources(channelId, rtype, rids, false);
			if (!rb.getSuccess()) {
				return setFailInfo(dc, rb.getMsg());
			}
		} catch (Exception e) {
			msg = "删除资源异常！";
			rm = this.getBaseResultMap(0, msg);
			log.info(msg + System.getProperty("line.separator") + getStackTrace(e));

			dc.getBusiness().putAll(rm);
			// throw new RuntimeException(msg + e.getMessage());
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
