package com.kind.base.attachment.service.attachment;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * 多附件上传
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = true, action = "#goto_attachment_multiupload_page", description = "多附件上传页", powerCode = "attachment.admin_2", requireTransaction = false)
public class GotoAttachmentMultiUploadPageService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "attachment/attachment_multi_upload";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

}
