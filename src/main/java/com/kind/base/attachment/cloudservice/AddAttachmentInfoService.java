package com.kind.base.attachment.cloudservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 添加附件基本信息记录，在 ATTACHMENT_INFO 表里添加一条数据。 单个文件，不进行追加续传。
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "#add_attachment_info", description = "添加附件基本信息记录", powerCode = "", requireTransaction = true)
public class AddAttachmentInfoService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(AddAttachmentInfoService.class);

	private final String serviceName = "添加附件基本信息记录";

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String msg = "";

		try {
			String token = dc.getRequestObject("TOKEN", "");
			String appId = dc.getRequestObject("APP_ID", "");
			String uid = UUID.randomUUID().toString().replaceAll("-", "");
			String attachmentType = dc.getRequestObject("ATTACHMENT_TYPE", "");
			String attachmentSuffix = dc.getRequestObject("ATTACHMENT_SUFFIX", "");
			String checkType = dc.getRequestObject("CHECK_TYPE", "");
			String checkValue = dc.getRequestObject("CHECK_VALUE", "");
			String creatorId = dc.getRequestObject("CREATOR_ID", "");
			String savePath = dc.getRequestObject("SAVE_PATH", "");
			String status = dc.getRequestObject("STATUS", ""); // 约定临时文件状态为TEMP
			String comments = dc.getRequestObject("COMMENTS", "");

			if (StrUtil.isBlank(appId) || StrUtil.isBlank(uid)) {
				return this.setFailInfo(dc, "参数传值有误！");
			}

			// 判断附件表里是否有记录
			String sql = " SELECT COUNT(1) FROM ATTACHMENT_INFO WHERE PK_UID = ? ";
			List<Object> param = new ArrayList<Object>();
			param.add(uid);
			if (Integer.parseInt(String.valueOf(getOneFiledValue(hmLogSql, sql, param))) == 0) {
				Map<String, Object> values = new HashMap<String, Object>();
				values.put("PK_UID", uid);
				values.put("APP_ID", appId);
				values.put("ATTACHMENT_TYPE", attachmentType);
				values.put("ATTACHMENT_SUFFIX", attachmentSuffix);
				values.put("ATTACHMENT_SIZE", 0);
				values.put("CREATOR_ID", creatorId);
				values.put("CREATE_TIME", DateUtil.now());
				values.put("CHECK_TYPE", checkType);
				values.put("CHECK_VALUE", checkValue);
				values.put("SAVE_PATH", savePath);
				values.put("STATUS", status);
				values.put("COMMENTS", comments);

				// 添加附件表记录和附件关联表记录
				DataBean db = new DataBean("ATTACHMENT_INFO", "PK_UID", values);
				insert(hmLogSql, db);
			}

			dc.setBusiness("uid", uid);
		} catch (Exception e) {
			msg = serviceName + "异常！";
			log.info(msg + System.getProperty("line.separator") + getStackTrace(e));

			return this.setFailInfo(dc, msg + e.getMessage());
		}

		return this.setSuccessInfo(dc, serviceName + "成功！");
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
