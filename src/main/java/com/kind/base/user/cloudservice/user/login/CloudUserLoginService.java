package com.kind.base.user.cloudservice.user.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.constant.ConProperties;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.common.utils.Encrypt;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.Pagination;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.KryoUtils;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * @author ZHENGJIAYUN
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = false, action = "#cloud_user_login", description = "用户登录验证", powerCode = "", requireTransaction = false)
public class CloudUserLoginService extends BaseActionService {
	public static String SYS_N_USERS_PASSWORD_COLUM = "PWD";
	public static String SYS_N_USERS_ENABLED_COLUM = "ENABLED";
	public static String SYS_N_USERS_OPTYPE_COLUM = "OPTYPE";
	public static String SYS_N_USERS_OPACCOUNT_COLUM = "OPACCOUNT";

	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(getClass());
	private String opaccount;
	private String password;

	private String organId = null;
	private String organName = null;
	private String deptId = null;
	private String opno = null;
	private String opname = null;
	private String deptName = null;
	private String organNlevel = null;

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		opaccount = dc.getRequestString("username");
		password = dc.getRequestString("password");
		deptId = dc.getRequestString("deptId");

		Map<String, Object> user = null;

		StringBuffer sql = new StringBuffer("SELECT * FROM SYS_N_USERS WHERE isdel !='1' and OPACCOUNT=?");

		List<Object> paramList = new ArrayList<Object>();

		paramList.add(opaccount);
		// 验证合法性
		user = getSelectMap(hmLogSql, sql.toString(), paramList);
		if (user == null) {
			return setFailInfo(dc, "帐号错误！");
		}

		if (StrUtil.isNotBlank(password)) {
			// 改为与.net那边的即时通讯的加密算法一致
			// 加密方式
			StringBuffer opnoMd5 = new StringBuffer(Encrypt.getAspMD5(user.get("OPNO").toString(), ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE));
			String defaultPwdMD5 = Encrypt.getAspMD5(password, ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE);
			String pwdMd5 = Encrypt.getAspMD5(opnoMd5.append(defaultPwdMD5).toString(), ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE);
			// 密码不正确
			if (!pwdMd5.equals(user.get(SYS_N_USERS_PASSWORD_COLUM).toString())) {
				return setFailInfo(dc, "帐号或密码错误！");
			}
		}

		// 操作员被禁用
		if (!ConCommon.NUM_1.equals(user.get(SYS_N_USERS_ENABLED_COLUM).toString())) {
			return this.setFailInfo(dc, "您的操作员编号" + opaccount + "已被禁用，不能登录系统，如有疑问，请与系统管理员联系！");

		}
		// 角色和用户角色类别的验证
		if (user.get(SYS_N_USERS_OPTYPE_COLUM) == null) {
			return setFailInfo(dc, "该用户的角色类别无法登录。");
		}

		// 是否超级管理员的验证 (防止数据库sys_n_user表的OPTYPE字段被改成0之后出现多个管理员)
		if (((Number) user.get(SYS_N_USERS_OPTYPE_COLUM)).intValue() == 0 && !SpringUtil.getEnvProperty(ConProperties.APP_SUPER_ACCOUNT).equals(user.get(SYS_N_USERS_OPACCOUNT_COLUM).toString())) {
			return setFailInfo(dc, "用户的角色类别错误，无法登录。");
		}

		log.debug("opaccount=" + opaccount + ",password=" + password);

		String userOpType = String.valueOf(user.get("OPTYPE"));
		opno = String.valueOf(user.get("OPNO"));
		opname = String.valueOf(user.get("OPNAME"));
		boolean isAdmin = (user != null && (ConCommon.NUM_0.equals(userOpType) || ConCommon.NUM_1.equals(userOpType)));
		if (isAdmin) {
			/* OPTYPE为0或1的是管理员，查询管理员机构 */
			paramList = new ArrayList<Object>();

			// 是否超级管理员的验证 (防止数据库SYS_N_USER表的OPTYPE字段被改成0之后出现多个管理员)
			if (ConCommon.NUM_0.equals(userOpType) && ConProperties.SUPER_ACCOUNT.equals(opaccount)) {
				// 超级管理员,用省局作为机构
				sql = new StringBuffer("SELECT a.ORGANID,a.NAME,a.NLEVEL FROM SYS_ORGANIZATION_INFO a WHERE a.ISDEL=0 ");

				// 部门非空的，直接用该 部门 机构
				if (StrUtil.isNotBlank(deptId)) {
					sql.append(" and exists(select 1 from  SYS_DEPARTMENT_INFO b where b.DEPTID=? and a.ORGANID=b.ORGANID)");
					paramList.add(deptId);
				} else {
					sql.append(" AND a.NLEVEL=2");
				}

			} else if (ConCommon.NUM_1.equals(userOpType)) {
				// 机构管理员
				sql = new StringBuffer("SELECT A.ORGANID,A.NAME,A.NLEVEL FROM SYS_ORGANIZATION_INFO A WHERE A.ISDEL=0 AND A.PARENTID!='ORGANIZATIONTOP000000000000000000000' AND ");
				sql.append(ConvertSqlDefault.getStrJoinSQL("A.ACCOUNT_PERFIX", "'" + ConProperties.SUPER_ACCOUNT + "'")).append("=?");
				paramList.add(opaccount);
			} else {
				return this.setFailInfo(dc, "错误数据类型");
			}
			List<Map<String, Object>> organ = this.getSelectList(hmLogSql, sql.toString(), paramList);
			if (organ != null && organ.size() > 0) {
				organId = String.valueOf(organ.get(0).get("ORGANID"));
				organName = String.valueOf(organ.get(0).get("NAME"));
				organNlevel = String.valueOf(organ.get(0).get("NLEVEL"));
			}

			/* 查询用户部门 */
			sql = new StringBuffer("SELECT A.DEPTID,A.DEPTNAME FROM SYS_DEPARTMENT_INFO A WHERE A.ISDEL=0 AND A.ORGANID=?");
			paramList.clear();
			paramList.add(organId);

			// 部门非空的，直接用该 部门
			if (StrUtil.isNotBlank(deptId)) {
				sql.append(" and a.DEPTID=?");
				paramList.add(deptId);
			}

			sql.append(" ORDER BY DLEVEL,ORDIDX,ALLORDIDX");
			Pagination pdata = this.getPagination(hmLogSql, sql.toString(), 1, 1, paramList);
			List<Map<String, Object>> dept = pdata.getResultList();
			if (dept != null && dept.size() > 0) {
				deptId = String.valueOf(dept.get(0).get("DEPTID"));
				deptName = String.valueOf(dept.get(0).get("DEPTNAME"));
			}

		} else if (user == null || user.size() != 1) {
			/* 查询普通用户 */
			paramList = new ArrayList<Object>();
			paramList.add(opaccount);
			// 【待改：(select 1 from sys_sysparm where paraid=9 and paravalue=D.grouptype)】
			// sql = new StringBuffer("SELECT A.*,D.ORGANID UUIT,C.DEPTID DEPT_ID,C.DEPTNAME,D.NAME UNITNAME,D.NLEVEL FROM SYS_N_USERS A,SYS_N_USER_DEPT_INFO B,SYS_DEPARTMENT_INFO C,SYS_ORGANIZATION_INFO D WHERE exists(select 1 from sys_sysparm where paraid=9 and paravalue=D.grouptype) and D.ORGANID=C.ORGANID AND B.MAIN_DEPT_FLAG=1 AND B.DEPT_ID=C.DEPTID AND D.ORGANID=B.ORGAN_ID AND A.OPNO=B.OPNO AND A.OPACCOUNT =?");
			sql = new StringBuffer("SELECT A.*,D.ORGANID UUIT,C.DEPTID DEPT_ID,C.DEPTNAME,D.NAME UNITNAME,D.NLEVEL FROM SYS_N_USERS A,SYS_N_USER_DEPT_INFO B,SYS_DEPARTMENT_INFO C,SYS_ORGANIZATION_INFO D ");
			sql.append(" WHERE D.ORGANID=C.ORGANID  AND B.DEPT_ID=C.DEPTID AND D.ORGANID=B.ORGAN_ID AND A.OPNO=B.OPNO AND A.OPACCOUNT =?");
			sql.append(" AND A.ISDEL=0 AND C.ISDEL=0 AND D.ISDEL=0 ");

			// 部门非空的，直接用该 部门
			if (StrUtil.isNotBlank(deptId)) {
				sql.append(" and C.DEPTID=?");
				paramList.add(deptId);
			} else {
				// 没有指定部门的，按主部门获取
				sql.append(" AND B.MAIN_DEPT_FLAG = 1");
			}

			user = this.getSelectMap(hmLogSql, sql.toString(), paramList);
			if (user == null) {
				return setFailInfo(dc, "帐号信息错误！");
			}

			deptId = String.valueOf(user.get("DEPT_ID"));
			deptName = String.valueOf(user.get("DEPTNAME"));
			organId = String.valueOf(user.get("UUIT"));
			organName = String.valueOf(user.get("UNITNAME"));
			organNlevel = String.valueOf(user.get("NLEVEL"));
		}

		/* 设置Session状态 */
		UserPO userpo = new UserPO();
		userpo.setOpAccount(opaccount);
		userpo.setDeptId(deptId);
		userpo.setDeptName(deptName);
		userpo.setOrgId(organId);
		userpo.setOpNo(opno);

		userpo.setOpName(opname);
		userpo.setOrgName(organName);
		userpo.setNlevel(organNlevel);
		userpo.setOpType(Integer.valueOf(userOpType));

		dc.setBusiness("USERPO", KryoUtils.serializationObject(userpo));

		return setSuccessInfo(dc, "登录成功！");

	}

	@Override
	public String pageAddress(DataContext dc) {
		return null;
	}
}
