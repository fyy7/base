package com.kind.base.core;

/**
 * @author Cxy
 * @version 创建时间：2019年3月13日 类说明
 */

public interface IConstant {
	public static final String RESET_PASSWORD = "reset_password";
	public static final String CFG_KEY_PLATFORM_RESTPW_SM4_KEY = "platform.restpw.sm4.key";
	public static final String VALIDITY_PERIOD_TIME = "validity.period.time";

	/*
	 * public static String getResetPassWord() { if(RESET_PASSWORD==null) RESET_PASSWORD = "reset_password"; return RESET_PASSWORD; }
	 */

	interface instanceType {
		String NORMAL = "normal";
		String SINGLETON = "singleton";
		String THREAD_SINGLETON = "thread_singleton";
	}

	interface TITLE {
		String t1 = "报盘数据信息管理";
		String t2 = "银行信息管理";
		String t3 = "社保机构信息管理";
		String t4 = "接口信息管理";
		String t5 = "系统日志查询";
	}
	
	public static interface CfgKey {
		public static final String KIND_BASE_LOGIN_CHECK_INNERIP = "kind.base.login.checkInnerIp";
	}
}
