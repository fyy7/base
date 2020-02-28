package com.kind.base.attachment.service.attachment;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.LoggerUtil;

import cn.hutool.core.date.DateUtil;

/**
 * 修改附件信息
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = true, action = "#modify_attachment", description = "修改附件信息", powerCode = "attachment.admin_4", requireTransaction = true)
public class ModifyAttachmentService extends BaseActionService {
	private static final String SERVICE_NAME = "修改附件信息";
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(AddAttachmentService.class);

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		try {
			DataBean bean = this.getBean(dc, "ATTACHMENT_RECORDS", "PK_UID");
			bean.setCmd(ConCommon.CMD_U);
			bean.set("LAST_MODIFY_TIME", DateUtil.now());
			bean.set("STATUS", "00");

			int result = this.update(hmLogSql, bean);
			if (result == 0) {
				return setFailInfo(dc, String.format("%s失败！", SERVICE_NAME));
			}
		} catch (Exception e) {
			log.info(String.format("%s异常！%s", SERVICE_NAME, LoggerUtil.getStackTrace(e)));
			return setFailInfo(dc, String.format("%s异常！", SERVICE_NAME));
		}

		return setSuccessInfo(dc, String.format("%s成功！", SERVICE_NAME));
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
