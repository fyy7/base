package com.kind.base.attachment.cloudservice;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 上传附件(临时文件)
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "#upload_attachment", description = "上传附件(临时文件)", powerCode = "", requireTransaction = false)
public class UploadAttachmentService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(UploadAttachmentService.class);

	private final String serviceName = "上传附件";

	private String attachmentPathRoot = "";

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// Map<String, Object> rm = this.getSuccessBaseResultMap();
		String msg = "";

		String uid = "";
		try {
			attachmentPathRoot = SpringUtil.getEnvProperty("attachment.path.root", "");

			String token = dc.getRequestObject("TOKEN", "");
			String appId = dc.getRequestObject("APP_ID", "");
			uid = dc.getRequestObject("UID", "");
			String startStr = dc.getRequestObject("START", "0");
			String content = dc.getRequestObject("CONTENT", "");

			if (StrUtil.isBlank(appId) || StrUtil.isBlank(content)) {
				// throw new RuntimeException("参数传值有误！");
				return this.setFailInfo(dc, "参数传值有误！");
			}

			if (StrUtil.isBlank(startStr)) {
				startStr = "0";
			}
			long start = Long.parseLong(startStr);

			if (StrUtil.isBlank(uid)) {
				uid = UUID.randomUUID().toString().replaceAll("-", "");
				// 创建空文件
				FileUtil.touch(getCacheFilePath(uid));
			}

			if (!FileUtil.exist(getCacheFilePath(uid))) {
				return this.setFailInfo(dc, msg + String.format("临时文件[%]不存在或已被删除，上传失败，请重新上传！", uid));
			}

			// 待写入的内容
			byte[] contentBytes = Base64.decode(content);

			RandomAccessFile raf = new RandomAccessFile(getCacheFilePath(uid), "rw");
			long length = raf.length();
			raf.seek(start > length - 1 ? length : start);
			raf.write(contentBytes);

			dc.setBusiness("uid", uid);
			dc.setBusiness("size", raf.length());

			raf.close();
		} catch (Exception e) {
			msg = serviceName + "异常！缓存UID: " + uid;
			log.info(msg + System.getProperty("line.separator") + getStackTrace(e));

			try {
				// 异常则删除缓存
				if (StrUtil.isNotBlank(uid)) {
					FileUtil.touch(getCacheFilePath(uid));
				}
			} catch (Exception e2) {
			}

			return this.setFailInfo(dc, msg + e.getMessage());
		}

		return this.setSuccessInfo(dc, serviceName + "成功！");
	}

	/**
	 * 获取指定uid对应的临时文件绝对路径
	 * 
	 * @return
	 */
	private String getCacheFilePath(String uid) {
		return attachmentPathRoot + File.separator + "cache" + File.separator + uid;
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
