package com.kind.base.attachment.cloudservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.LoggerUtil;

import cn.hutool.core.util.StrUtil;

/**
 * 修改附件状态信息，修改 ATTACHMENT_INFO 表里 STATUS 值 和 关联表 ATTACHMENT_RECORDS 里的与之相关的记录的 STATUS 值。
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = true, action = "#change_attachment_info_status", description = "修改附件状态信息", powerCode = "attachment.admin_4", requireTransaction = true)
public class ChangeAttachmentInfoStatusService extends BaseActionService {
	private static final String SERVICE_NAME = "修改附件状态信息";
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(ChangeAttachmentInfoStatusService.class);

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		try {
			String token = dc.getRequestObject("TOKEN", "");
			String appId = dc.getRequestObject("APP_ID", "");
			String uid = dc.getRequestObject("UID", "");
			String status = dc.getRequestObject("STATUS", "");

			if (StrUtil.isBlank(appId) || StrUtil.isBlank(uid) || StrUtil.isBlank(status) || status.length() > 4) {
				return this.setFailInfo(dc, "参数传值有误！");
			}

			String sql = " UPDATE ATTACHMENT_INFO SET STATUS = ? WHERE UUID = ? ";
			List<Object> params = new ArrayList<Object>();
			params.add(uid);
			params.add(status);
			int result = this.executeSql(hmLogSql, sql, params);

			String sql2 = " UPDATE ATTACHMENT_RECORDS SET STATUS = ? WHERE ATTACHMENT_UID = ? ";
			params = new ArrayList<Object>();
			params.add(status);
			params.add(uid);
			int result2 = this.executeSql(hmLogSql, sql2, params);

			if (result == 0 || result2 == 0) {
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
