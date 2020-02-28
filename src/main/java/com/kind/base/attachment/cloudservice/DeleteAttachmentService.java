package com.kind.base.attachment.cloudservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * 删除附件
 * 
 * @author WANGXIAOYI
 *
 */
@Service
@Action(requireLogin = false, action = "#delete_attachment", description = "删除附件", powerCode = "", requireTransaction = true)
public class DeleteAttachmentService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String token = dc.getRequestString("TOKEN");
		String appId = dc.getRequestString("APP_ID");
		String uid = dc.getRequestString("UID");

		if (StrUtil.isEmpty(appId) || StrUtil.isEmpty(uid)) {
			return setFailInfo(dc, "APP_ID和UID不能为空，删除失败!");
		}

		// 删除附件
		List<Object> param = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("delete from ATTACHMENT_RECORDS where APP_ID = ? and PK_UID IN (");
		param.add(appId);

		String[] _uids = uid.split(",");
		for (int i = 0; i < _uids.length; i++) {
			String val = _uids[i].trim();
			if (StrUtil.isBlank(val)) {
				continue;
			}

			sql.append("?");
			if (i != _uids.length - 1) {
				sql.append(", ");
			}

			param.add(val);
		}

		sql.append(" ) ");

		if (this.executeSql(hmLogSql, sql.toString(), param) != 1) {
			return setFailInfo(dc, "删除附件失败!");
		}

		return setSuccessInfo(dc, "删除成功!");
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
