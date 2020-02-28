package com.kind.base.user.cloudservice.user;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author chenzhiwei
 *
 *         2018年4月9日
 */
@Service
@Action(requireLogin = true, action = "#user_change_dept_check", description = "切换用户信息验证", powerCode = "", requireTransaction = false)
public class ChangeDeptCheckService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		/**
		 * 仅判断用户切换数据是否合法，不验证admin及机构管理员
		 */

		UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		// UserPO userpoNew = new UserPO();
		String deptid = dc.getRequestString("DEPT_ID");

		if (StrUtil.isEmpty(deptid)) {
			return this.setFailInfo(dc, "切换失败，参数异常！");
		}

		if (userpo.getConsignorType() == 1) {// 委托人
			userpo = userpo.getConsignorSysUserPO();
		}
		String flag = dc.getRequestString("flag");
		if ("2".equals(flag)) {// 转换为委托人
			String opno = dc.getRequestString("OPNO");
			StringBuffer sql = new StringBuffer("SELECT B.CREATOR,B.DEPT_ID FROM   SYS_N_ROLES_ENTRUST B ,SYS_N_ROLEUSER_ENTRUST A  WHERE  A.ROLEID = B.ROLEID AND B.CREATOR=? AND  A.OPNO=? and B.DEPT_ID=? and A.DEPT_ID=? ");

			sql.append(" and BEGINDATE < ").append(ConvertSqlDefault.setDatetimeSQL(DateUtil.now()));
			sql.append(" and ENDDATE > ").append(ConvertSqlDefault.setDatetimeSQL(DateUtil.now()));
			List<Map<String, Object>> entrust_role = this.getSelectList(hmLogSql, sql.toString(), Arrays.asList(opno, userpo.getOpNo(), deptid, userpo.getDeptId()));
			Boolean a = false;
			if (entrust_role == null || entrust_role.size() == 0) {
				return this.setFailInfo(dc, "切换失败，未查询到可用的委托！");

			}
			// 委托需要委托人用户信息查询出来,并判断账户是否可登录状态
			Map<String, Object> map = this.getSelectMap(hmLogSql, "select OPACCOUNT from  SYS_N_USERS WHERE OPNO=? AND ISDEL='0' AND ENABLED='1' ", Arrays.asList(opno));
			if (map == null || ObjectUtil.isNull(map.get("OPACCOUNT"))) {
				return this.setFailInfo(dc, "切换失败，委托账户存在异常！");
			}
			dc.setBusiness("OPACCOUNT", map.get("OPACCOUNT"));
			// userpoNew.setConsignorType(1);
			// userpoNew.setConsignorSysUserPO(userpo);
			// userpoNew.setOpName(dc.getRequestString("OPNONAME") + "(" + userpo.getOpName() + ")");
			// userpoNew.setOpNo(dc.getRequestString("OPNO"));
		} else {

			StringBuffer sql = new StringBuffer("SELECT OPNO,DEPT_ID FROM SYS_N_USER_DEPT_INFO WHERE OPNO=? and DEPT_ID=? ");
			Map<String, Object> oneMap = this.getSelectMap(hmLogSql, sql.toString(), Arrays.asList(userpo.getOpNo(), deptid));
			if (oneMap == null) {
				return this.setFailInfo(dc, "切换失败，验证未通过！");
			}
			// userpoNew = userpo;

		}
		// userpoNew.setOrgId(dc.getRequestString("ORGANID"));
		// userpoNew.setOrgName(dc.getRequestString("ORGAN_NAME"));
		// userpoNew.setDeptId(dc.getRequestString("DEPT_ID"));
		// userpoNew.setDeptName(dc.getRequestString("DEPT_NAME"));
		// dc.setSession(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM), userpoNew);
		// ResourceService resourceService = new ResourceService();
		// resourceService.cleanRedisCache(userpoNew);
		return this.setSuccessInfo(dc, "修改成功!");
	}

	@Override
	public String pageAddress(DataContext arg0) {
		return null;
	}

	@Override
	public String verifyParameter(DataContext arg0) {
		return null;
	}
}
