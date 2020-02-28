package com.kind.base.user.service.user;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.utils.CodeSwitching;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

@Service
@Action(requireLogin = true, action = "#user_goto_change_dept", description = "切换部门", powerCode = "", requireTransaction = false)
public class GotoChangeDeptMain extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// TODO 自动生成的方法存根
		UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		if (userpo.getConsignorType() == 1) {
			userpo = userpo.getConsignorSysUserPO();
		}
		String opno = userpo.getOpNo();
		String deptid = userpo.getDeptId();
		String flag = dc.getRequestString("FLAG");
		String entrustFlag = StrUtil.emptyToDefault(CodeSwitching.getReidiosJsonDm("sys", "21"),"1");

		String time=DateUtil.now();
		StringBuffer sb_sql = new StringBuffer("SELECT B.DEPT_ID,C.ORGAN_ID,B.OPEMAN,B.CREATOR," + ConvertSqlDefault.getDatetimeToCharSQL("B.BEGINDATE") + " as BEGINDATE, " + ConvertSqlDefault.getDatetimeToCharSQL("B.ENDDATE") + "as ENDDATE,D.NAME,E.DEPTNAME FROM SYS_N_ROLEUSER_ENTRUST A,SYS_N_ROLES_ENTRUST B,SYS_N_USER_DEPT_INFO C,SYS_ORGANIZATION_INFO D,SYS_DEPARTMENT_INFO E WHERE A.ROLEID = B.ROLEID AND B.DEPT_ID=C.DEPT_ID AND C.DEPT_ID=E.DEPTID AND C.ORGAN_ID=D.ORGANID AND A.OPNO=? AND A.DEPT_ID=? AND B.BEGINDATE < '");
		sb_sql.append(ConvertSqlDefault.setDatetime(time)).append("' AND B.ENDDATE > '").append(ConvertSqlDefault.setDate(time)).append("'");
		sb_sql.append("group by b.DEPT_ID,C.ORGAN_ID,b.OPEMAN,b.CREATOR,b.BEGINDATE,b.ENDDATE,d.NAME,e.DEPTNAME");
		List<Map<String, Object>> entrust_role = this.getSelectList(hmLogSql, sb_sql.toString(), Arrays.asList(opno, deptid));
		if (entrust_role == null || entrust_role.size() == 0) {
			dc.setBusiness("CLIENT", "1");
		} else {
			dc.setBusiness("CLIENT", "2");
		}
		dc.setBusiness("ENTRUST_ROLE", entrust_role);
		
		dc.setBusiness("FLAG", flag);
		dc.setBusiness("APP_ID", dc.getRequestString("app_id"));

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		// TODO 自动生成的方法存根
		return "user/user/changeDept";
	}

	@Override
	public String verifyParameter(DataContext arg0) {
		// TODO 自动生成的方法存根
		return null;
	}

}
