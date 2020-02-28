package com.kind.base.attachment.cloudservice;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 重命名附件
 * 
 * @author WANGXIAOYI
 *
 */
@Service
@Action(requireLogin = false, action = "#rename_attachment", description = "重命名附件", powerCode = "", requireTransaction = true)
public class RenameAttachmentService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String token = dc.getRequestString("TOKEN");
		String appId = dc.getRequestString("APP_ID");
		String uid = dc.getRequestString("UID");
		String newName = dc.getRequestString("NEW_NAME");

		if (StrUtil.isEmpty(appId) || StrUtil.isEmpty(uid) || StrUtil.isEmpty(newName)) {
			return setFailInfo(dc, "参数传值有误！");
		}

		DataBean bean = this.getBean(dc, "ATTACHMENT_RECORDS", "PK_UID");
		bean.set("PK_UID", uid);
		bean.set("APP_ID", appId);
		bean.set("ATTACHMENT_NAME", newName);
		bean.set("LAST_MODIFY_TIME", DateUtil.date());

		if (this.update(hmLogSql, bean) != 1) {
			return setFailInfo(dc, "保存失败！");
		} else {
			return setSuccessInfo(dc, "保存成功！");
		}
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
