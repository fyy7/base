package com.kind.base.attachment.cloudservice;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SafeUtils;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;

/**
 * 下载附件
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "#download_attachment", description = "下载附件", powerCode = "", requireTransaction = false)
public class DownloadAttachmentService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(DownloadAttachmentService.class);

	private final String serviceName = "下载附件";

	private String attachmentPathRoot = "";

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String msg = "";

		try {
			attachmentPathRoot = SpringUtil.getEnvProperty("attachment.path.root", "");

			String token = dc.getRequestObject("TOKEN", "");
			String appId = dc.getRequestObject("APP_ID", "");
			String uid = dc.getRequestObject("UID", "");
			long start = Long.parseLong(dc.getRequestObject("START", "0"));
			long length = Long.parseLong(dc.getRequestObject("LENGTH", "0"));
			String isTmpFileFlag = dc.getRequestObject("IS_TMP_FILE", ""); // 为空则读取持久文件，为1则读取临时文件

			boolean isTmpFile = false;
			if ("1".equals(isTmpFileFlag)) {
				isTmpFile = true;
			}

			if (StrUtil.isBlank(appId) || StrUtil.isBlank(uid)) {
				return this.setFailInfo(dc, "参数传值有误！");
			}

			String savePath = "";
			if (!isTmpFile) {
				String sql = " SELECT AI.SAVE_PATH FROM ATTACHMENT_RECORDS AR, ATTACHMENT_INFO AI WHERE AR.ATTACHMENT_UID = AI.PK_UID AND AR.PK_UID = ? ";
				List<Object> param = new ArrayList<Object>();
				param.add(uid);

				Map<String, Object> result = this.getSelectMap(hmLogSql, sql, param);
				if (result == null || result.get("SAVE_PATH") == null || StrUtil.isBlank((String) result.get("SAVE_PATH"))) {
					return this.setFailInfo(dc, "文件不存在！");
				}

				savePath = (String) result.get("SAVE_PATH");
			} else {
				attachmentPathRoot = SpringUtil.getEnvProperty("attachment.path.root", "");
				savePath = "/cache/" + uid;
			}

			savePath = SafeUtils.getSafePath(savePath);
			RandomAccessFile raf = new RandomAccessFile(attachmentPathRoot + savePath, "rw");

			long fileLength = raf.length();
			start = start >= 0 ? start : 0;
			start = start > fileLength - 1 ? fileLength : start; // start: [0, fileLength]
			long remainLength = fileLength - start;
			if (length < 0) {
				length = remainLength;
			}
			length = length > remainLength ? remainLength : length;

			raf.seek(start);
			byte[] readBytes = new byte[(int) length];
			raf.read(readBytes);

			raf.close();

			dc.setBusiness("remaining_size", remainLength - length);
			dc.setBusiness("content", Base64.encode(readBytes));
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
