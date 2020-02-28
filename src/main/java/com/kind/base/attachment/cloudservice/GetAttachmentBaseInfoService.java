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
 * 获取附件基本信息（attachment_info）
 * 
 * @author yanhang
 *
 */
@Service
@Action(requireLogin = false, action = "#get_attachment_base", description = "获取附件基本信息", powerCode = "", requireTransaction = false)
public class GetAttachmentBaseInfoService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// String token = dc.getRequestString("TOKEN");
		String appId = dc.getRequestString("APP_ID");
		String uid = dc.getRequestString("UID");

		if (StrUtil.isEmpty(appId) || StrUtil.isEmpty(uid)) {
			return setFailInfo(dc, "参数传值有误！");
		}

		List<Object> param = new ArrayList<Object>();
		String sql = "select PK_UID, APP_ID, ATTACHMENT_TYPE, ATTACHMENT_SUFFIX, ATTACHMENT_SIZE, CREATOR_ID, CREATE_TIME, SAVE_PATH, CHECK_TYPE, CHECK_VALUE, STATUS, COMMENTS from ATTACHMENT_INFO where PK_UID = ?";
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
