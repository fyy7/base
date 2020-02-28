package com.kind.base.core.log;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.log.ILogUser;
import com.kind.framework.utils.GenID;
import com.kind.framework.utils.SpringUtil;
import com.kind.framework.utils.StringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * 日志用户设置
 * 
 * @author zhengjiayun
 * @modify gqj 2019年5月21日
 *
 * 
 */
public class LogUser implements ILogUser {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(getClass());

	@Override
	public void initLog(HttpServletRequest request, DataBean bean, Long num) {
		bean.setTableName("SYS_LOGS");

		bean.set("LOGID", GenID.gen(20));

		Object obj = request.getAttribute(Constant.FRAMEWORK_G_RESULT);
		if (!StrUtil.isBlankIfStr(obj) && "0".equals(String.valueOf(obj))) {
			bean.set("LOGLEVEL", "E");
		} else {
			bean.set("LOGLEVEL", "I");
		}

		// zipkin中的traceid值
		// Tracer tracer = SpringUtil.getBean("sleuthTracer", Tracer.class);
		// if (tracer != null && tracer.getCurrentSpan() != null) {
		// log.debug("--------zipkin traceid=" + tracer.getCurrentSpan().traceIdString());
		// bean.set("TRACEID", tracer.getCurrentSpan().traceIdString());
		// }

		bean.set("LOGTYPE", "SYS");
		bean.set("OPTIME", new Date());
		// IP地址

		Object logIp = request.getAttribute(Constant.APP_SYS_LOGS_IP);
		if (null == logIp || "null".equalsIgnoreCase(String.valueOf(logIp))) {
			logIp = StringUtil.getIpAddress(request);
		} else {
			logIp = request.getAttribute(Constant.APP_SYS_LOGS_IP);
		}

		bean.set("LOGIP", logIp);
		bean.set("OP_MILLISECOND", (System.currentTimeMillis() - num));

		// 公用goto的处理
		// if ("goto".equals(request.getParameter(Constant.FRAMEWORK_ACTION))) {
		// bean.set("REQUEST_URL", request.getServletPath() + "?" + request.getQueryString());
		// } else {
		// bean.set("REQUEST_URL", request.getServletPath() + "?" + Constant.FRAMEWORK_ACTION + "=" + request.getParameter(Constant.FRAMEWORK_ACTION));
		// }

		String queryParam = request.getQueryString();
		if (StrUtil.isBlank(queryParam)) {
			queryParam = Constant.FRAMEWORK_ACTION + "=" + request.getParameter(Constant.FRAMEWORK_ACTION);
		} else {
			queryParam = queryParam.replaceAll("action=KDA_\\w+", Constant.FRAMEWORK_ACTION + "=" + request.getParameter(Constant.FRAMEWORK_ACTION));
		}

		bean.set("REQUEST_URL", request.getServletPath() + "?" + queryParam);

		// 获取完整请求地址
		bean.set("REQUEST_URL2", request.getRequestURL() + "?" + queryParam);

		// log.debug(this.getClass().getName() + ",linkid=====" + request.getAttribute(Constant.APP_SYS_LOGS_LINKID)));
		// 关联日志id
		bean.set("LINKID", request.getAttribute(Constant.APP_SYS_LOGS_LINKID));
		bean.set("SRCTYPE", request.getAttribute(Constant.APP_SYS_LOGS_SRC_TYPE) == null ? "0" : request.getAttribute(Constant.APP_SYS_LOGS_SRC_TYPE));

		log.debug("-----beanJson-----:" + bean.getJsonString());

		StringBuffer content = new StringBuffer("");
		if (request.getAttribute(Constant.FRAMEWORK_G_MESSAGE) != null) {
			bean.set("OP_RESULT", request.getAttribute(Constant.FRAMEWORK_G_MESSAGE));
		}

		obj = request.getAttribute(Constant.FRAMEWORK_G_EXECUTE_SQL);
		if (!StrUtil.isBlankIfStr(obj)) {
			content.append("执行语句:").append(obj);
		}
		bean.set("CONTENTS", content.toString());
	}

	@Override
	public void setLogUser(HttpServletRequest request, DataBean logBean) {
		// 系统ID
		logBean.set("APP_ID", SpringUtil.getEnvProperty(Constant.APP_SYSTEM_APPID));

		UserPO userPo = null;

		// session标识
		String sessionType = SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM);

		// 从session中判断有，取session
		if (request.getSession().getAttribute(sessionType) != null) {
			userPo = (UserPO) request.getSession().getAttribute(sessionType);
			if (userPo != null) {
				logBean.set("OPNO", userPo.getOpNo());
				logBean.set("OPNAME", userPo.getOpName() + "[" + userPo.getOrgName() + "," + userPo.getDeptName() + "]");
				return;
			}
		}

		// 从Attribute中判断有，取Attribute
		if (request.getAttribute(sessionType) != null) {
			userPo = (UserPO) request.getAttribute(sessionType);
			if (userPo != null) {
				logBean.set("OPNO", userPo.getOpNo());
				logBean.set("OPNAME", userPo.getOpName() + "[" + userPo.getOrgName() + "," + userPo.getDeptName() + "]");
				return;
			}
		}

		if (request.getAttribute(Constant.APP_SYS_LOGS_LINKID) == null) {
			logBean.set("OPNO", "--");
			logBean.set("OPNAME", "--");
			return;
		}

		logBean.set("OPNO", "-微服务调用-");
		logBean.set("OPNAME", "-微服务调用-");
	}

}
