package com.kind.base.attachment.service.attachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.kind.common.utils.CodeSwitching;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * 附件管理首页
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = true, action = "#goto_attachment_main_page", description = "附件管理首页", powerCode = "attachment.admin", requireTransaction = false)
public class GotoAttachmentMainPageService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(GotoAttachmentMainPageService.class);

	private String serviceName = "附件管理首页";

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		try {
			// 假删除的，停用的状态不查询出来
			StringBuffer sql = new StringBuffer("SELECT APP_NAME, APP_ID, APP_TYPE FROM PORTAL_APP_INFO WHERE IS_DEL=0 and APP_STATUS=1 ");
			sql.append(" ORDER BY APP_ORDER");
			List<Map<String, Object>> reoust = this.getSelectList(hmLogSql, sql.toString(), new ArrayList<Object>());
			dc.setBusiness("APP_LIST", reoust);

			JSONArray attachmentTypes = CodeSwitching.getReidiosJsonArr("dmb.ATTACHMENT.TYPE");
			dc.setBusiness("ATTACHMENT_TYPE", attachmentTypes);

			JSONArray attachmentCheckTypes = CodeSwitching.getReidiosJsonArr("dmb.ATTACHMENT.CHECKTYPE");
			dc.setBusiness("ATTACHMENT_CHECKTYPE", attachmentCheckTypes);
		} catch (Exception e) {
			String msg = String.format("[%s]异常！", serviceName);
			log.info(msg + System.getProperty("line.separator") + getStackTrace(e));

			return setFailInfo(dc, msg);
		}

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "attachment/attachment_main";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

}
