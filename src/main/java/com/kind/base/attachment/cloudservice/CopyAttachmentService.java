package com.kind.base.attachment.cloudservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.GenID;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 复制附件
 * 
 * @author WANGXIAOYI
 *
 */
@Service
@Action(requireLogin = false, action = "#copy_attachment", description = "复制附件", powerCode = "", requireTransaction = true)
public class CopyAttachmentService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// String token = dc.getRequestString("TOKEN");
		String appId = dc.getRequestString("APP_ID");
		String uid = dc.getRequestString("UID");
		String createId = dc.getRequestString("CREATOR_ID");

		if (StrUtil.isEmpty(appId) || StrUtil.isEmpty(uid) || StrUtil.isEmpty(createId)) {
			return setFailInfo(dc, "参数传值有误！");
		}

		List<Object> param = new ArrayList<Object>();
		String sql = "select * from ATTACHMENT_RECORDS where PK_UID=?";
		param.add(uid);
		Map<String, Object> map = this.getSelectMap(hmLogSql, sql, param);
		if (map == null) {
			map = new HashMap<String, Object>();
		}

		DataBean bean = this.getBean(dc, "ATTACHMENT_RECORDS", "PK_UID");
		bean.set("PK_UID", GenID.gen(64));
		bean.set("APP_ID", appId);
		bean.set("ATTACHMENT_UID", map.get("ATTACHMENT_UID"));
		bean.set("ATTACHMENT_NAME", map.get("ATTACHMENT_NAME"));
		bean.set("CREATOR_ID", createId);
		bean.set("CREATE_TIME", DateUtil.date());
		bean.set("LAST_MODIFY_TIME", DateUtil.date());
		bean.set("STATUS", "00");
		bean.set("COMMENTS", "");

		if (this.insert(hmLogSql, bean) != 1) {
			return setFailInfo(dc, "复制失败！");
		} else {
			dc.setBusiness("uid", bean.get("PK_UID"));
			return setSuccessInfo(dc, "复制成功！");
		}
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
