package com.kind.base.attachment.cloudservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kind.common.utils.FastDfsClient;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 确认存储（FastDFS模式专用），更新 ATTACHMENT_INFO 表记录，并在 ATTACHMENT_RECORDS 表里添加一条记录。
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "#upload_attachment_confirm_fdfs", description = "确认存储（FastDFS模式专用）", powerCode = "", requireTransaction = true)
public class UploadAttachmentConfirmFdfsService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(UploadAttachmentConfirmFdfsService.class);

	private final String serviceName = "确认存储（FastDFS模式专用）";

	private FastDfsClient fdfsClient = new FastDfsClient();

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String msg = "";

		try {
			String token = dc.getRequestObject("TOKEN", "");
			String appId = dc.getRequestObject("APP_ID", "");
			String uid = UUID.randomUUID().toString().replaceAll("-", "");
			String attachmentUid = dc.getRequestObject("ATTACHMENT_UID", "");
			String attachmentName = dc.getRequestObject("ATTACHMENT_NAME", "");
			String checkType = dc.getRequestObject("CHECK_TYPE", "");
			String checkValue = dc.getRequestObject("CHECK_VALUE", "");
			String creatorId = dc.getRequestObject("CREATOR_ID", "");

			if (StrUtil.isBlank(appId) || StrUtil.isBlank(uid) || StrUtil.isBlank(attachmentUid)) {
				return this.setFailInfo(dc, "参数传值有误！");
			}

			String sql = " SELECT PK_UID, APP_ID, ATTACHMENT_SIZE, CHECK_TYPE, CHECK_VALUE, SAVE_PATH, STATUS FROM ATTACHMENT_INFO WHERE PK_UID = ? AND STATUS = 'TEMP' "; // 约定临时文件状态为TEMP
			List<Object> params = new ArrayList<Object>();
			params.add(attachmentUid);
			Map<String, Object> attachmentInfo = this.getSelectMap(hmLogSql, sql, params);
			if (attachmentInfo != null) {
				String fileId = MapUtil.getStr(attachmentInfo, "SAVE_PATH"); // FastDFS文件存储的FileId，约定fastdfs存储的文件在表里保存的save_path值以"fdfs:"开头，以便区分旧版附件存储路径
				fileId = fileId.substring(5);

				FastDfsClient.FileInfo fileInfo = fdfsClient.getFileInfo(fileId);

				// 更新附件信息
				DataBean info = new DataBean("ATTACHMENT_INFO", "PK_UID");
				info.set("PK_UID", attachmentUid);
				info.set("ATTACHMENT_SIZE", fileInfo.getSize());
				// info.set("CHECK_VALUE", fileInfo.getCrc32());
				info.set("CHECK_VALUE", "");
				info.set("STATUS", "00");
				this.update(hmLogSql, info);

				// 插入附件关联表
				Map<String, Object> values = new HashMap<String, Object>();
				values.put("PK_UID", uid);
				values.put("APP_ID", appId);
				values.put("ATTACHMENT_UID", attachmentUid);
				values.put("ATTACHMENT_NAME", attachmentName);
				values.put("CREATOR_ID", creatorId);
				values.put("CREATE_TIME", DateUtil.now());
				values.put("LAST_MODIFY_TIME", DateUtil.now());
				values.put("STATUS", "00");
				values.put("COMMENTS", "");
				DataBean record = new DataBean("ATTACHMENT_RECORDS", "PK_UID", values);
				insert(hmLogSql, record);
			} else {
				return this.setFailInfo(dc, "未找到临时文件信息记录[attachmentUid: " + attachmentUid + "]");
			}

			dc.setBusiness("uid", uid);
			dc.setBusiness("attachmentUid", attachmentUid);
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
