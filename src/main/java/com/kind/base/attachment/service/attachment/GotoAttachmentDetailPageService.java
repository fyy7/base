package com.kind.base.attachment.service.attachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.utils.CodeSwitching;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * 附件详情页
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = true, action = "#goto_attachment_detail_page", description = "附件详情页", powerCode = "attachment.admin", requireTransaction = false)
public class GotoAttachmentDetailPageService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(GotoAttachmentDetailPageService.class);

	private String serviceName = "附件详情页";

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		try {
			String appId = CodeSwitching.getSystemAppId();
			String cmd = dc.getRequestObject("CMD", "");

			dc.setBusiness("CMD", cmd);

			Map<String, Object> entity = new HashMap<String, Object>();
			if (ConCommon.CMD_A.equalsIgnoreCase(cmd)) {
				entity.put("CREATE_TIME", "系统生成");
				entity.put("CHECK_VALUE", "系统生成");
				entity.put("ATTACHMENT_SUFFIX", "自动生成");
				entity.put("ATTACHMENT_SIZE", "系统生成");
				entity.put("SAVE_PATH", "系统生成");
			} else if (ConCommon.CMD_U.equalsIgnoreCase(cmd)) {
				List<Object> param = new ArrayList<Object>();
				StringBuffer sql = new StringBuffer("SELECT AR.PK_UID, AR.APP_ID, AI.ATTACHMENT_TYPE, AR.ATTACHMENT_NAME, AI.ATTACHMENT_SUFFIX, AI.ATTACHMENT_SIZE, AI.SAVE_PATH, AI.CHECK_TYPE, AI.CHECK_VALUE, ").append(ConvertSqlDefault.getDatetimeToCharSQL("AR.CREATE_TIME")).append(" CREATE_TIME, AR.CREATOR_ID, AR.COMMENTS FROM ATTACHMENT_RECORDS AR, ATTACHMENT_INFO AI WHERE AR.ATTACHMENT_UID = AI.PK_UID ");
				sql.append(" AND AR.PK_UID = ?");
				param.add(dc.getRequestString("UID"));

				Map<String, Object> result = this.getSelectMap(hmLogSql, sql.toString(), param);
				entity.putAll(result);
			}

			dc.setBusiness("ENTITY", entity);
		} catch (Exception e) {
			String msg = String.format("[%s]异常！", serviceName);
			log.info(msg + System.getProperty("line.separator") + getStackTrace(e));

			return setFailInfo(dc, msg);
		}

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "attachment/attachment_detail";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

}
