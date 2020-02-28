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
 * 获取附件信息列表
 * 
 * @author yanhang
 *
 */
@Service
@Action(requireLogin = false, action = "#get_attachments", description = "获取附件信息列表", powerCode = "", requireTransaction = false)
public class GetAttachmentsService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String token = dc.getRequestString("TOKEN");
		String appId = dc.getRequestString("APP_ID");
		String uids = dc.getRequestString("UIDS"); // uid1|uid2|...

		if (StrUtil.isEmpty(appId) || StrUtil.isEmpty(uids)) {
			return setFailInfo(dc, "参数传值有误！");
		}

		StringBuffer sql = new StringBuffer(" select AR.PK_UID, AR.ATTACHMENT_NAME, AI.ATTACHMENT_TYPE, AI.ATTACHMENT_SUFFIX, AI.ATTACHMENT_SIZE, AI.SAVE_PATH, AI.CHECK_TYPE, AI.CHECK_VALUE, AR.CREATE_TIME, AR.CREATOR_ID ");
		sql.append(" from ATTACHMENT_RECORDS AR, ATTACHMENT_INFO AI ");
		sql.append(" where AR.ATTACHMENT_UID = AI.PK_UID and AR.APP_ID = ? ");

		List<Object> param = new ArrayList<Object>();
		param.add(appId);

		String[] uidsArray = uids.split("\\|");
		if (uidsArray.length > 0) {
			sql.append(" and AR.PK_UID in ( ");

			for (int i = 0; i < uidsArray.length; i++) {
				String uid = uidsArray[i];
				if (StrUtil.isNotBlank(uid)) {
					sql.append("?");
					if (i != uidsArray.length - 1) {
						sql.append(",");
					}

					param.add(uid);
				}
			}

			sql.append(")");
		}

		List<Map<String, Object>> beans = this.getSelectList(hmLogSql, sql.toString(), param);

		List<Map<String, Object>> _beans = new ArrayList<Map<String, Object>>();
		if (beans != null) {
			for (Map<String, Object> bean : _beans) {
				Map<String, Object> _bean = new HashMap<String, Object>();
				Set<String> keys = bean.keySet();
				for (String key : keys) {
					_bean.put(key.toLowerCase(), bean.get(key));
				}

				_beans.add(_bean);
			}
		}

		dc.setBusiness("values", _beans);

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
