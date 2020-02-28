/**
 * 
 */
package com.kind.base.user.cloudservice.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

/**
 * Copyright (c) 2017-2018 KIND Corp. 2017-2018,All Rights Reserved. This software is published under the KIND Team. License version 1.0, a copy of which has been included with this distribution in the LICENSE.txt file.
 *
 * @File name: GetUserOppcount.java
 * @Description: TODO(描述类的作用)
 * @Create on: 2018年9月30日
 * @author: @author
 *
 * @ChangeList --------------------------------------------------- Date Editor ChangeReasons
 *
 */

@Service
@Action(requireLogin = true, action = "#get_no_oppcount_user", description = "获取无用户账号的人员", powerCode = "", requireTransaction = false)
public class GetUserOppcount extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(getClass());

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String userids = dc.getRequestString("USER_ID");
		log.debug("USERID:" + userids);
		String[] arrUser = userids.split(";");
		List<Object> paramList = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder("select OPNO from  SYS_N_USERS where isdel=0 and opno in (");
		log.debug("USERID:" + userids);
		for (int i = 0; i < arrUser.length; i++) {
			sql.append("?");
			if (i < arrUser.length - 1) {
				sql.append(",");
			}
			paramList.add(arrUser[i]);
		}
		sql.append(")");
		List<Map<String, Object>> userList = this.getSelectList(hmLogSql, sql.toString(), paramList);

		dc.setBusiness("USER_LIST", userList);

		return this.setSuccessInfo(dc);
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
