package com.kind.base.attachment.cloudservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * 获取附件信息
 * 
 * @author WANGXIAOYI
 *
 */
@Service
@Action(requireLogin = false, action = "#get_attachment", description = "获取附件信息", powerCode = "", requireTransaction = false)
public class GetAttachmentService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String token = dc.getRequestString("TOKEN");
		String appId = dc.getRequestString("APP_ID");
		String uid = dc.getRequestString("UID");

		if (StrUtil.isEmpty(appId) || StrUtil.isEmpty(uid)) {
			return setFailInfo(dc, "参数传值有误！");
		}

		List<Object> param = new ArrayList<Object>();
		String sql = "select AR.PK_UID, AR.ATTACHMENT_NAME, AI.ATTACHMENT_TYPE, AI.ATTACHMENT_SUFFIX, AI.ATTACHMENT_SIZE, AI.SAVE_PATH, AI.CHECK_TYPE, AI.CHECK_VALUE, AR.CREATE_TIME, AR.CREATOR_ID from ATTACHMENT_RECORDS AR, ATTACHMENT_INFO AI where AR.ATTACHMENT_UID=AI.PK_UID and AR.APP_ID=? and AR.PK_UID=?";
		param.add(appId);
		param.add(uid);
		Map<String, Object> bean = this.getSelectMap(hmLogSql, sql, param);
		if (bean == null) {
			bean = new HashMap<String, Object>();
		}

		Map<String, Object> _bean = new HashMap<String, Object>();
		if (bean != null) {
			Set<String> keys = bean.keySet();
			for (String key : keys) {
				_bean.put(key.toLowerCase(), bean.get(key));
			}
		}

		dc.setBusiness("value", _bean);

		return setSuccessInfo(dc);
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
