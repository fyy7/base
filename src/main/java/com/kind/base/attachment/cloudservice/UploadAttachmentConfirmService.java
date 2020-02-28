package com.kind.base.attachment.cloudservice;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 确认存储附件
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "#confirm_upload", description = "确认存储附件", powerCode = "", requireTransaction = true)
public class UploadAttachmentConfirmService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(UploadAttachmentConfirmService.class);

	private final String serviceName = "确认存储附件";

	private String attachmentPathRoot = "";

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String msg = "";

		try {
			attachmentPathRoot = SpringUtil.getEnvProperty("attachment.path.root", "");

			String token = dc.getRequestObject("TOKEN", "");
			String appId = dc.getRequestObject("APP_ID", "");
			String uid = dc.getRequestObject("UID", "");
			String attachmentName = dc.getRequestObject("ATTACHMENT_NAME", "");
			String attachmentType = dc.getRequestObject("ATTACHMENT_TYPE", "");
			String attachmentSuffix = dc.getRequestObject("ATTACHMENT_SUFFIX", "");
			String checkType = dc.getRequestObject("CHECK_TYPE", "");
			String checkValue = dc.getRequestObject("CHECK_VALUE", "");
			String creatorId = dc.getRequestObject("CREATOR_ID", "");

			if (StrUtil.isBlank(appId) || StrUtil.isBlank(uid) || StrUtil.isBlank(attachmentName)) {
				// throw new RuntimeException("参数传值有误！");
				return this.setFailInfo(dc, "参数传值有误！");
			}

			String path = getCacheFilePath(uid);

			// 判断文件是否存在
			File cacheFile = new com.kind.framework.common.CommonFile(path);
			if (!cacheFile.exists()) {
				return this.setFailInfo(dc, String.format("临时文件[%s]不存在！", uid));
			}

			// String chacheFileMd5 = DigestUtil.md5Hex(cacheFile);
			// boolean hasChacheFileMd5Exists = false; // 文件MD5值是否已经存在于ATTACHMENT_INFO表中，若存在则认为已经存在相同文件
			// String _sql = " SELECT PK_UID FROM ATTACHMENT_INFO WHERE FILE_MD5 = ? ";
			// List<Object> _param = new ArrayList<Object>();
			// _param.add(chacheFileMd5);
			// String _UID = getOneFiledValue(hmLogSql, _sql, _param) == null ? null : (String) getOneFiledValue(hmLogSql, _sql, _param);
			// if (!StrUtil.isBlank(_UID)) {
			//
			// } else {
			//
			// }

			// 文件校验 暂时不进行校验值计算
			// if (!StrUtil.isBlank(checkType) && !StrUtil.isBlank(checkValue)) {
			// String _checkValue = "";
			// switch (checkType) {
			// case "00": // md5
			// _checkValue = DigestUtil.md5Hex(cacheFile);
			// break;
			// case "01": // crc16
			//
			// break;
			// }
			//
			// if (!_checkValue.equalsIgnoreCase(checkValue)) {
			// return this.setFailInfo(dc, String.format("临时文件[%s]校验值不正确[服务器校验值：%s，传参校验值：%s]！", uid, _checkValue, checkValue));
			// }
			// }

			// 保存临时文件到持久区
			String destFilePath = getPermanentPath();
			String relatedDestFilePath = destFilePath.replace(attachmentPathRoot, "");
			FileUtil.copy(cacheFile, new com.kind.framework.common.CommonFile(destFilePath + uid), true);

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
				values.put("ATTACHMENT_SIZE", cacheFile.length());
				values.put("CREATOR_ID", creatorId);
				values.put("CREATE_TIME", DateUtil.now());
				values.put("CHECK_TYPE", checkType);
				values.put("CHECK_VALUE", checkValue);
				values.put("SAVE_PATH", relatedDestFilePath + uid);
				// values.put("FILE_MD5", chcheFileMd5); // 文件md5值
				// values.put("NEED_LOGIN_DOWNLOAD", "0"); // 是否需要登录才能下载，0否，1是
				values.put("STATUS", "00");
				values.put("COMMENTS", "");

				// 添加附件表记录和附件关联表记录
				DataBean db = new DataBean("ATTACHMENT_INFO", "PK_UID", values);
				insert(hmLogSql, db);
			}

			// 插入关联记录到附件关联记录表
			sql = " SELECT COUNT(1) FROM ATTACHMENT_RECORDS WHERE PK_UID = ? AND ATTACHMENT_UID = ? ";
			param.clear();
			param.add(uid);
			param.add(uid);
			if (Integer.parseInt(String.valueOf(getOneFiledValue(hmLogSql, sql, param))) == 0) {
				Map<String, Object> values = new HashMap<String, Object>();
				values.put("PK_UID", uid);
				values.put("APP_ID", appId);
				values.put("ATTACHMENT_UID", uid);
				values.put("ATTACHMENT_NAME", attachmentName);
				values.put("CREATOR_ID", creatorId);
				values.put("CREATE_TIME", DateUtil.now());
				values.put("LAST_MODIFY_TIME", DateUtil.now());
				values.put("STATUS", "00");
				values.put("COMMENTS", "");

				// 添加附件表记录和附件关联表记录
				DataBean db = new DataBean("ATTACHMENT_RECORDS", "PK_UID", values);
				insert(hmLogSql, db);
			}

			dc.setBusiness("uid", uid);
			dc.setBusiness("path", relatedDestFilePath + uid);
			cacheFile.delete();
		} catch (Exception e) {
			msg = serviceName + "异常！";
			log.info(msg + System.getProperty("line.separator") + getStackTrace(e));

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

	/**
	 * 获取用于持久存储文件的路径，以斜杠结尾
	 * 
	 * @param uid
	 * @return
	 */
	private String getPermanentPath() {
		return attachmentPathRoot + File.separator + DateUtil.today() + File.separator;
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
