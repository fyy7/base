package com.kind.base.attachment.service.attachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.kind.common.attachment.AttachmentFdfsService;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.LoggerUtil;

/**
 * 添加附件(FDFS)
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = true, action = "#add_attachment_fdfs", description = "添加附件(FDFS)", powerCode = "attachment.admin_2", requireTransaction = true)
public class AddAttachmentFdfsService extends BaseActionService {
	private static final String SERVICE_NAME = "添加附件 ";
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(AddAttachmentFdfsService.class);

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		try {
			DataBean bean = this.getBean(dc, "ATTACHMENT_INFO", "PK_UID");

			String token = "";
			AttachmentFdfsService fas = new AttachmentFdfsService();
			JSONObject result = fas.uploadConfirm(token, bean.getString("APP_ID"), bean.getString("CACHE_UID"), bean.getString("ATTACHMENT_NAME"), bean.getString("CHECK_TYPE"), bean.getString("CHECK_VALUE"), "");

			if (result.getShort(Constant.FRAMEWORK_G_RESULT) == 0) {
				return setFailInfo(dc, String.format("%s失败！%s", SERVICE_NAME, result.getString(Constant.FRAMEWORK_G_MESSAGE)));
			} else {
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
