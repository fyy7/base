/**
 * 
 */
package com.kind.base.auth.userresource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.base.auth.cloudservice.auth.CommonService;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

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
@Action(requireLogin = false, action = "#get_login_info", description = "获取登录用户信息", powerCode = "", requireTransaction = false)
public class GetLoginInfo2 extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(getClass());

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		CommonService commonService = new CommonService();
		String token = dc.getRequestString("TOKEN");
		String appId = dc.getRequestString("APP_ID");
		String ret = commonService.doAccessValid(dc, token, appId);
		if (StrUtil.isNotEmpty(ret)) {
			return setFailInfo(dc, "获取用户信息失败！");
		}

		String deptid = dc.getRequestString("DEPT_ID");
		String opaccount = dc.getRequestString("OPACCOUNT");
		if (StrUtil.isEmpty(deptid) || StrUtil.isEmpty("OPACCOUNT")) {
			return setFailInfo(dc, "数据异常");
		}

		StringBuilder sql = new StringBuilder("SELECT A.OPNO AS USER_ID,A.OPACCOUNT,A.OPNAME,C.NAME AS ORGAN_NAME,D.DEPTNAME  AS DEPT_NAME,B.DEPT_ID FROM  SYS_N_USERS A,SYS_N_USER_DEPT_INFO B,SYS_ORGANIZATION_INFO C,SYS_DEPARTMENT_INFO D  WHERE A.OPNO=B.OPNO AND B.ORGAN_ID =C.ORGANID AND  B.DEPT_ID =D.DEPTID AND A.ISDEL!='1' AND C.ISDEL!='1' AND D.ISDEL!='1' AND A.ENABLED='1'");
		sql.append(" AND B.DEPT_ID=? AND A.OPACCOUNT=?");
		Map<String, Object> selectMap = this.getSelectMap(hmLogSql, sql.toString(), Arrays.asList(deptid, opaccount));
		if (selectMap != null) {
			dc.setBusiness("INFO", selectMap);
		} else {
			return setFailInfo(dc, "数据异常");
		}
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
