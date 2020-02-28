package com.kind.base.attachment.service.attachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.kind.common.attachment.AttachmentService;
import com.kind.common.constant.ConCommon;
import com.kind.framework.cache.CacheManager;
import com.kind.framework.cache.redis.AbstractRedisClientManager;
import com.kind.framework.cache.redis.IRedisClient;
import com.kind.framework.cache.redis.KryoRedisSerializer;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.LoggerUtil;

import cn.hutool.core.util.StrUtil;

/**
 * 添加附件
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = true, action = "#add_attachment", description = "添加附件", powerCode = "attachment.admin_2", requireTransaction = true)
public class AddAttachmentService extends BaseActionService {
	private static final String SERVICE_NAME = "添加附件";
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(AddAttachmentService.class);

	private KryoRedisSerializer<Map<String, String>> serializer = new KryoRedisSerializer<Map<String, String>>();

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		try {
			AbstractRedisClientManager redisClientManager = CacheManager.getRedisClientManager();
			IRedisClient client = redisClientManager.getClient("default");

			DataBean bean = this.getBean(dc, "ATTACHMENT_INFO", "PK_UID");
			// bean.setCmd(ConCommon.CMD_A);
			// bean.set("ATTACHMENT_SIZE", 0);
			// bean.set("CREATE_TIME", DateUtil.now());
			// bean.set("CHECK_VALUE", "");
			// bean.set("STATUS", "00");

			// int result = this.insert(hmLogSql, bean);
			// if (result == 0) {
			// return setFailInfo(dc, String.format("%s失败！[ATTACHMENT_INFO]", SERVICE_NAME));
			// }

			// 缓存不存储在业务系统本地，直接存储到附件服务器
			// String cacheFilePath = SpringUtil.getEnvProperty("attachment.path.cache") + "/" + bean.getString("CACHE_UID");
			// File cacheFile = new com.kind.framework.common.CommonFile(cacheFilePath);
			// if (!cacheFile.exists()) {
			// return setFailInfo(dc, String.format("%s失败，服务器临时文件丢失，请刷新重试！", SERVICE_NAME));
			// }
			// bean.set("ATTACHMENT_SIZE", cacheFile.length());
			//
			// // 文件校验
			// String checkType = bean.getString("CHECK_TYPE");
			// String checkValue = "";
			// switch (checkType) {
			// case "00": // md5
			// checkValue = DigestUtil.md5Hex(cacheFile);
			// break;
			// case "01": // crc16
			//
			// break;
			// }
			// bean.set("CHECK_VALUE", checkValue);

			String token = "";
			String chacheUid = bean.getString("CACHE_UID");
			String name = bean.getString("ATTACHMENT_NAME");
			String suffix = "";
			String checkType = "";
			String checkValue = "";
			try {
				// Map<String, String> info = UploadAttachmentImpl.cacheFileInfos.get(chacheUid);

				Object cache = client.hGet(ConCommon.RedisKey.ATTACHMENT_CACHE_FILE_INFO, chacheUid);
				if (cache == null) {
					return setFailInfo(dc, String.format("%s失败！文件基本信息不存在。%s", SERVICE_NAME, chacheUid));
				}

				Map<String, String> info = serializer.deserialize((byte[]) cache);
				if (info == null) {
					info = new HashMap<String, String>();
				}

				if (StrUtil.isBlank(name)) {
					name = info.get("name"); // 若客户端未上送则从缓存里取附件名称
				}

				suffix = info.get("suffix");
				checkType = info.get("checkType");
				checkValue = info.get("checkValue");
			} catch (Exception e) {
				return setFailInfo(dc, String.format("%s失败！%s文件基本信息不存在。%s", SERVICE_NAME, chacheUid, e.getMessage()));
			}

			AttachmentService as = new AttachmentService();
			// JSONObject result = as.upload(token, bean.getString("APP_ID"), FileUtil.readBytes(cacheFile), bean.getString("ATTACHMENT_NAME"),
			// bean.getString("ATTACHMENT_TYPE"), bean.getString("ATTACHMENT_SUFFIX"), checkType, checkValue, bean.getString("CREATOR_ID"));
			JSONObject result = as.uploadConfirm(token, bean.getString("APP_ID"), bean.getString("CACHE_UID"), name, bean.getString("ATTACHMENT_TYPE"), suffix, checkType, checkValue, bean.getString("CREATOR_ID"));

			if (result.getShort(Constant.FRAMEWORK_G_RESULT) == 0) {
				return setFailInfo(dc, String.format("%s失败！%", SERVICE_NAME, result.getString(Constant.FRAMEWORK_G_MESSAGE)));
			} else {
				// UploadAttachmentImpl.cacheFileInfos.remove(chacheUid);
				client.hDel(ConCommon.RedisKey.ATTACHMENT_CACHE_FILE_INFO, chacheUid);

				// 更新COMMENTS
				String uid = result.getString("uid");
				List<Object> param = new ArrayList<Object>();
				StringBuffer sql = new StringBuffer(" UPDATE ATTACHMENT_RECORDS AR SET AR.COMMENTS = ? WHERE PK_UID = ? ");
				param.add(dc.getRequestString("COMMENTS"));
				param.add(uid);

				int ret = this.executeSql(hmLogSql, sql.toString(), param);
				if (ret > 0) {
					dc.setBusiness(Constant.FRAMEWORK_G_MESSAGE, result.getString(Constant.FRAMEWORK_G_MESSAGE));
				} else {
					return setSuccessInfo(dc, String.format("%s成功，但添加备注信息失败，请手动修改！", SERVICE_NAME));
				}
			}

			// cacheFile.delete();
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
