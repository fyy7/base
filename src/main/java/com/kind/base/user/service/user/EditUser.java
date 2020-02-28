package com.kind.base.user.service.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.base.user.service.organ.OrganHandle;
import com.kind.common.constant.ConCommon;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年2月23日
 */
@Service
@Action(requireLogin = true, action = "#user_edit", description = "编辑用户", powerCode = "register.user", requireTransaction = false)
public class EditUser extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String opno = dc.getRequestString("OPNO");

		Map<String, Object> userMap = null;
		List<Map<String, Object>> deptList = null;
		List<Object> paramList = new ArrayList<Object>();
		if (!StrUtil.isEmpty(opno)) {
			/* 查询用户数据 */
			String selectuserSql = "SELECT * FROM SYS_N_USERS WHERE OPNO=?";
			paramList.add(opno);
			userMap = this.getSelectMap(hmLogSql, selectuserSql, paramList);

			/* 查询用户的部门与职务数据 */
			String selectDeptSql = "SELECT * FROM SYS_N_USER_DEPT_INFO WHERE OPNO=?";
			deptList = this.getSelectList(hmLogSql, selectDeptSql, paramList);
			if (deptList != null && deptList.size() > 0) {
				String selectOfficeSql = "SELECT A.OFFICE_TYPE,B.DMNR OFFICE_TYPE_NAME FROM SYS_N_USER_DEPT_OFFICE_TYPE A,ORGAN_DMB B WHERE A.OPNO=? AND A.DEPT_ID=? AND B.ORGAN_ID=? AND B.DMLX='OFFICE.TYPE' AND B.DM=A.OFFICE_TYPE";
				for (int i = 0; i < deptList.size(); i++) {
					Map<String, Object> deptMap = deptList.get(i);
					paramList.clear();
					paramList.add(opno);
					paramList.add(String.valueOf(deptMap.get("DEPT_ID")));
					paramList.add(String.valueOf(deptMap.get("ORGAN_ID")));
					List<Map<String, Object>> officeTypeArr = this.getSelectList(hmLogSql, selectOfficeSql, paramList);
					StringBuffer sbInfoNames = new StringBuffer("");
					StringBuffer sbInfoIds = new StringBuffer("");
					if (officeTypeArr != null && officeTypeArr.size() > 0) {
						for (Map<String, Object> officeMap : officeTypeArr) {
							sbInfoNames.append(",").append(String.valueOf(officeMap.get("OFFICE_TYPE_NAME")));
							sbInfoIds.append(",").append(String.valueOf(officeMap.get("OFFICE_TYPE")));
						}
						deptMap.put("OFFICE_TYPE_NAMES", sbInfoNames.substring(1));
						deptMap.put("OFFICE_TYPE_IDS", sbInfoIds.substring(1));
					} else {
						deptMap.put("OFFICE_TYPE_NAMES", "");
						deptMap.put("OFFICE_TYPE_IDS", "");
					}
				}
			}
			dc.setBusiness("SYS_DEPT_LIST", deptList);
			dc.setBusiness("BEAN", userMap);

			dc.setBusiness("CMD", ConCommon.CMD_A);
		} else {
			dc.setBusiness("CMD", ConCommon.CMD_U);
		}

		/* 查询机构数据 */
		UserPO userpo = (UserPO) dc.getSession().get(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		String organId = userpo.getOrgId() == null ? "00000000-0000-0000-1000-000000000000" : userpo.getOrgId();
		List<Map<String, Object>> superList = OrganHandle.getOrganAndLowerList(hmLogSql, this, organId);
		dc.setBusiness("SUPER_LIST", superList);

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "user/user/user_edit";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
