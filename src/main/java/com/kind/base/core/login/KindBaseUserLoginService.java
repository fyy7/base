package com.kind.base.core.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.kisso.common.encrypt.MD5;
import com.baomidou.kisso.security.token.SSOToken;
import com.kind.base.core.IConstant;
import com.kind.common.constant.ConCommon;
import com.kind.common.constant.ConProperties;
import com.kind.common.constant.ConSession;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.common.utils.Encrypt;
import com.kind.framework.auth.AuthManager;
import com.kind.framework.auth.SubjectAuthInfo.SubjectType;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.Pagination;
import com.kind.framework.core.bean.ResultBean;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SSoTakenUtils;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author ZHENGJIAYUN
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = false, action = "#kind_base_user_login", description = "用户登录验证", powerCode = "", requireTransaction = false)
public class KindBaseUserLoginService extends BaseActionService {

	private static com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(KindBaseUserLoginService.class);

	public static String SYS_N_USERS_PASSWORD_COLUM = "PWD";
	public static String SYS_N_USERS_ENABLED_COLUM = "ENABLED";
	public static String SYS_N_USERS_OPTYPE_COLUM = "OPTYPE";
	public static String SYS_N_USERS_OPACCOUNT_COLUM = "OPACCOUNT";

	// private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(getClass());
	private String opaccount;
	private String password;
	private String verifycode;
	private String verifycodeSession;

	@Override
	public String verifyParameter(DataContext dc) {

		String verifyStr = "";
		opaccount = dc.getRequestString("username");
		password = dc.getRequestString("password");
		verifycode = dc.getRequestString("verifycode");

		verifycodeSession = dc.getSessionString(ConSession.SESSION_VERIFY_CODE);
		String d = StrUtil.isNotEmpty(verifyStr) ? "," : "";
		// 普通
		if (StrUtil.isEmpty(opaccount)) {
			verifyStr = verifyStr + d + "没有输入用户名！";
		}
		if (StrUtil.isEmpty(password)) {
			verifyStr = verifyStr + d + "没有输入密码！";

		}

		if (!verifycode.equals(verifycodeSession)) {
			verifyStr = verifyStr + d + "验证码验证失败！";
		}

		// 清除验证码，让其失效，防爆破
		dc.setRemoveSession(ConSession.SESSION_VERIFY_CODE);

		if (StrUtil.isEmpty(verifyStr)) {
			return null;
		}
		return verifyStr;
	}

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String ip = (String) (dc.getHeader().get("ip") == null ? "" : dc.getHeader().get("ip"));
		if (StrUtil.isNullOrUndefined(ip) || (!"0".equals(SpringUtil.getEnvProperty(IConstant.CfgKey.KIND_BASE_LOGIN_CHECK_INNERIP, "")) && !NetUtil.isInnerIP(ip))) {
			return this.setFailInfo(dc, "非法IP，登录失败！");
		}

		// 调用统一授权中心进行登录操作
		SSOToken token = dc.getSsoToken();

		opaccount = dc.getRequestString("username");
		if (token != null) {
			JSONObject encJson = SSoTakenUtils.getJson(token);
			log.debug("获取登录对象：" + encJson.toJSONString());
			opaccount = (opaccount == null ? encJson.getString(Constant.KISSO_JWT_ENC_JSON_ACCOUNT) : opaccount);
			// 部门ID
			dc.setReqeust("deptId", encJson.getString(Constant.KISSO_JWT_ENC_JSON_DEPTID));
		}

		if (StrUtil.isBlank(opaccount)) {
			return this.setFailInfo(dc, "帐号为空，登录失败！");
		}

		// 用户账号
		dc.setReqeust("username", opaccount);

		// 登录认证操作
		ResultBean rb = userLogin(hmLogSql, dc);
		if (!rb.getSuccess()) {
			return this.setFailInfo(dc, rb.getMsg());
		}

		Object userpoObj = rb.getValue();
		if (userpoObj == null || !(userpoObj instanceof UserPO)) {
			return this.setFailInfo(dc, "未获取到USERPO对象！");
		}

		UserPO userpo = (UserPO) userpoObj;

		// 设置登录session信息中
		dc.setSession(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM), userpo);
		// 为了页面能获取session名称
		dc.setSession("session_name", SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));

		// 记录登录时间，用于切换到其他系统时，判断其他系统的session是否过时（是否小于生成cookie时间），若过时则需清掉sessio登录缓存，进入模拟登录
		long loginTime = System.currentTimeMillis();
		if (token == null) {
			// 设置单点登录信息
			SSOToken ssoToken = SSOToken.create();
			// 生成cookie时间
			ssoToken.setTime(loginTime);

			// 自定义的json对象，存放业务相关数据
			JSONObject data = new JSONObject();
			data.put(Constant.KISSO_JWT_ENC_JSON_ACCOUNT, opaccount);
			data.put(Constant.KISSO_JWT_ENC_JSON_DEPTID, userpo.getDeptId());

			// 处理自定义的key值
			SSoTakenUtils.setData(ssoToken, data);

			dc.setSsoToken(ssoToken);
			// 设置加载sso信息
			dc.setSsoTokenFlag(true);
		}

		/** added by yanhang **/
		AuthManager.onLogin(dc, SubjectType.USER, userpo);
		dc.setSession(Constant.LOGIN_TIME, loginTime);

		// 用户账号与部门id做md5唯一
		dc.setCookie(SpringUtil.getEnvProperty("kisso.config.cookieName", "") + "_md5", MD5.toMD5(opaccount + userpo.getDeptId()), Integer.MAX_VALUE);

		return setSuccessInfo(dc, "登录成功！");
	}

	// com.kind.cloudservice.user.login.CloudUserLoginService
	private ResultBean userLogin(HashMap<String, String> hmLogSql, DataContext dc) {
		String opaccount = dc.getRequestString("username");
		String password = dc.getRequestString("password");
		String deptId = dc.getRequestString("deptId");

		String organId = null;
		String organName = null;
		String opno = null;
		String opname = null;
		String deptName = null;
		String organNlevel = null;

		Map<String, Object> user = null;

		StringBuffer sql = new StringBuffer("SELECT * FROM SYS_N_USERS WHERE isdel !='1' and OPACCOUNT=?");

		List<Object> paramList = new ArrayList<Object>();

		paramList.add(opaccount);
		// 验证合法性
		user = getSelectMap(hmLogSql, sql.toString(), paramList);
		if (user == null) {
			// return setFailInfo(dc, "帐号错误！");
			return new ResultBean(false, "帐号错误！");
		}

		if (StrUtil.isNotBlank(password)) {
			// 改为与.net那边的即时通讯的加密算法一致
			// 加密方式
			StringBuffer opnoMd5 = new StringBuffer(Encrypt.getAspMD5(user.get("OPNO").toString(), ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE));
			String defaultPwdMD5 = Encrypt.getAspMD5(password, ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE);
			String pwdMd5 = Encrypt.getAspMD5(opnoMd5.append(defaultPwdMD5).toString(), ConProperties.APP_USER_PASSWORD_TYPE_DEFAULT_VALUE);
			// 密码不正确
			if (!pwdMd5.equals(user.get(SYS_N_USERS_PASSWORD_COLUM).toString())) {
				// return setFailInfo(dc, "帐号或密码错误！");
				return new ResultBean(false, "帐号或密码错误！");
			}
		}

		// 操作员被禁用
		if (!ConCommon.NUM_1.equals(user.get(SYS_N_USERS_ENABLED_COLUM).toString())) {
			// return this.setFailInfo(dc, "您的操作员编号" + opaccount + "已被禁用，不能登录系统，如有疑问，请与系统管理员联系！");
			return new ResultBean(false, "您的操作员编号" + opaccount + "已被禁用，不能登录系统，如有疑问，请与系统管理员联系！");

		}
		// 角色和用户角色类别的验证
		if (user.get(SYS_N_USERS_OPTYPE_COLUM) == null) {
			// return setFailInfo(dc, "该用户的角色类别无法登录。");
			return new ResultBean(false, "该用户的角色类别无法登录。");
		}

		// 是否超级管理员的验证 (防止数据库sys_n_user表的OPTYPE字段被改成0之后出现多个管理员)
		if (((Number) user.get(SYS_N_USERS_OPTYPE_COLUM)).intValue() == 0 && !SpringUtil.getEnvProperty(ConProperties.APP_SUPER_ACCOUNT).equals(user.get(SYS_N_USERS_OPACCOUNT_COLUM).toString())) {
			// return setFailInfo(dc, "用户的角色类别错误，无法登录。");
			return new ResultBean(false, "用户的角色类别错误，无法登录。");
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
				sql = new StringBuffer("SELECT A.ORGANID,A.NAME,A.NLEVEL FROM SYS_ORGANIZATION_INFO A WHERE A.ISDEL=0 AND ");
				sql.append(ConvertSqlDefault.getStrJoinSQL("A.ACCOUNT_PERFIX", "'" + ConProperties.SUPER_ACCOUNT + "'")).append("=?");
				paramList.add(opaccount);
			} else {
				// return this.setFailInfo(dc, "错误数据类型");
				return new ResultBean(false, "错误数据类型");
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
				// return setFailInfo(dc, "帐号信息错误！");
				return new ResultBean(false, "帐号信息错误！");
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

		ResultBean rb = new ResultBean(true, "获取用户信息成功");
		rb.setValue(userpo);

		return rb;
	}

	@Override
	public String pageAddress(DataContext dc) {
		return null;
	}
}
