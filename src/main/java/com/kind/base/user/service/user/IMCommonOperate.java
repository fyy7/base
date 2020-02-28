/**
 * @Title: IMCommonOperate.java 
 * @Package com.kind.user.service.user 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年12月18日 下午7:55:53 
 * @version V1.0   
 */
package com.kind.base.user.service.user;

import java.util.Date;
import java.util.HashMap;

import com.alibaba.fastjson.JSONObject;
import com.kind.base.user.webservice.IMWebServiceUtil;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * @author huanglei
 *
 */
public class IMCommonOperate {

	public static int delOrgan(BaseActionService service, DataContext dc, String organid) {
		// 与即时通讯的交互
		String method = "DelOrg";
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("OrgId", organid);
		JSONObject json = IMWebServiceUtil.send(method, data);
		if (json.getIntValue("ReturnCode") != 0) {
			service.setFailInfo(dc, "同步即时通讯信息失败：" + json.getString("Description") + "," + json.getString("DetailInfo"));
			return 0;
		}
		return 1;
	}

	public static int updateOrganBean(BaseActionService service, DataContext dc, DataBean bean, String cmd) {
		// 与即时通讯的交互
		String method = "A".equals(cmd) ? "AddOrg" : "UpdateOrg";
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("OrgId", bean.getString("ORGANID"));
		data.put("Name", bean.getString("NAME"));
		data.put("ParentId", bean.getString("PARENTID"));
		// 排序号，加上10000，让其排在相关部门的后面
		data.put("OrgIdx", Integer.parseInt(bean.getString("ORDIDX")) + 10000);
		JSONObject json = IMWebServiceUtil.send(method, data);
		if (json.getIntValue("ReturnCode") != 0) {
			service.setFailInfo(dc, "同步即时通讯信息失败：" + json.getString("Description") + "," + json.getString("DetailInfo"));
			return 0;
		}
		return 1;

	}

	public static int updateDeptByBean(BaseActionService service, DataContext dc, DataBean bean, String cmd) {
		String method = "A".equals(cmd) ? "AddOrg" : "UpdateOrg";
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("OrgId", bean.getString("DEPTID"));
		data.put("Name", bean.getString("DEPTNAME"));
		data.put("ParentId", bean.getString("PARENTID"));
		data.put("OrgIdx", bean.getString("ORDIDX").replace(".0", ""));
		JSONObject json = IMWebServiceUtil.send(method, data);
		if (json.getIntValue("ReturnCode") != 0) {
			service.setFailInfo(dc, "同步即时通讯信息失败：" + json.getString("Description") + "," + json.getString("DetailInfo"));
			return 0;
		}
		return 1;

	}

	public static int delDept(BaseActionService service, DataContext dc, String deptid) {
		String method = "DelOrg";
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("OrgId", deptid);

		JSONObject json = IMWebServiceUtil.send(method, data);
		if (json.getIntValue("ReturnCode") != 0) {
			service.setFailInfo(dc, "同步即时通讯信息失败：" + json.getString("Description") + "," + json.getString("DetailInfo"));
			return 0;
		}
		return 1;
	}

	public static int delUser(BaseActionService service, DataContext dc, String opno) {
		String method = "DelUser";
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("UserId", opno);
		JSONObject json = IMWebServiceUtil.send(method, data);
		if (json.getIntValue("ReturnCode") != 0) {
			service.setFailInfo(dc, "同步即时通讯信息失败：" + json.getString("Description") + "," + json.getString("DetailInfo"));
			return 0;
		}
		return 1;
	}

	public static int updateUserByBean(BaseActionService service, DataContext dc, DataBean userBean, String cmd, String mainDeptId) {
		if (!"0".equals(userBean.getString("ENABLED"))) {
			// 启用的

			// 与即时通讯的交互
			String method = "A".equals(cmd) ? "AddUser" : "UpdateUser";
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("UserId", userBean.getString("OPNO"));
			data.put("Name", userBean.getString("OPNAME"));
			if (StrUtil.isNotEmpty(userBean.getString("PWD"))) {
				data.put("PasswordMd5", userBean.getString("PWD"));
			}
			data.put("UserSignon", userBean.getString("OPACCOUNT"));
			data.put("OrgId", mainDeptId);
			data.put("HeadImageIndex", "0");
			data.put("CreateTime", new Date());
			data.put("UserIdx", "0");
			JSONObject json = IMWebServiceUtil.send(method, data);
			if (json.getIntValue("ReturnCode") != 0) {
				service.setFailInfo(dc, "同步即时通讯信息失败：" + json.getString("Description") + "," + json.getString("DetailInfo"));
				return 0;
			}
		} else {
			if (delUser(service, dc, userBean.getString("OPNO")) == 0) {
				return 0;
			}
		}
		return 1;
	}
}
