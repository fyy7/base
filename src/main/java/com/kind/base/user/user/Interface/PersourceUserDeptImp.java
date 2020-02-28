/**
 * @Title: PersourceUserDeptImp.java 
 * @Package com.kind.user.Interface 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年8月28日 上午10:35:51 
 * @version V1.0   
 */
package com.kind.base.user.user.Interface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * 接收人事系统中部门/人员信息
 * 
 * @author huanglei
 *
 */
@Service
@Action(requireLogin = true, action = "#persource_User_dept_imp", description = "接收人事系统中部门/人员信息", powerCode = "userpower.branch_8", requireTransaction = true)

public class PersourceUserDeptImp extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String type = dc.getRequestString("_SENDFLAG");
		String[] types = new String[1];
		if (type.indexOf(",") > 0) {
			types = type.split(",");
		} else {
			types[0] = type;
		}
		int result = 0;
		for (int i = 0; i < types.length; i++) {
			result = saveDataByType(hmLogSql, dc, types[i]);
			if (result == 0) {
				setFailInfo(dc, types[i] + "保存操作执行异常！");
				break;
			}
		}

		return result;
	}

	private void setDefualFlag(DataBean bean) {
		bean.set("data_exchange_flag", "1");
	}

	/**
	 * 获取requestjson数组
	 * 
	 * @param dc
	 * @param name
	 * @return
	 */
	private JSONObject getJsonObjct(DataContext dc, String name) {
		Object tmpobj = dc.getRequestObject(name);
		JSONObject backOjbect;
		if (tmpobj instanceof String) {
			backOjbect = JSONObject.parseObject(((String) tmpobj));
		} else {
			backOjbect = (JSONObject) tmpobj;
		}
		return backOjbect;
	}

	/**
	 * 获取requestjson对象
	 * 
	 * @param dc
	 * @param name
	 * @return
	 */
	private JSONArray getJsonArray(DataContext dc, String name) {
		Object tmpobj = dc.getRequestObject(name);
		JSONArray backArray;
		if (tmpobj instanceof String) {
			backArray = JSONArray.parseArray(((String) tmpobj));
		} else {
			backArray = (JSONArray) tmpobj;
		}
		return backArray;
	}

	/**
	 * 根据类型执行相应表的 保存操作
	 * 
	 * @param hmLogSql
	 * @param dc
	 * @param type
	 * @return
	 */
	private int saveDataByType(HashMap<String, String> hmLogSql, DataContext dc, String type) {
		DataBean bean = null, tmp = null;

		String key = dc.getRequestString("KEY");
		if (StrUtil.isEmpty(key)) {
			return setFailInfo(dc, "主键不能为空");
		}
		int result = 0;
		switch (type.toUpperCase()) {
		case "DEPT":
			/* 单主表 保存 */

			JSONObject deptData = getJsonObjct(dc, "DEPT");

			if (StrUtil.isEmpty(deptData.getString("DEPTID"))) {
				break;
			}
			bean = new DataBean("SYS_DEPARTMENT_INFO_IMAGE", "DEPTID", deptData);
			setDefualFlag(bean);
			result = SaveBeanTool.saveRowForBean(this, hmLogSql, bean);
			break;

		case "DEPTTYPE":
			/* 多记录从表 */

			JSONArray typeData = getJsonArray(dc, "DEPTTYPE");
			List<DataBean> typeList = new ArrayList<>();

			for (int i = 0; i < typeData.size(); i++) {
				tmp = new DataBean("SYS_DEPARTMENT_TYPE_IMAGE", "DEPTID,BVALUE", typeData.getJSONObject(i));
				setDefualFlag(tmp);
				typeList.add(tmp);
			}
			result = SaveBeanTool.saveRowForBeans(this, hmLogSql, typeList.toArray(new DataBean[typeList.size()]), "SYS_DEPARTMENT_TYPE_IMAGE", "DEPTID", key);
			break;

		case "USER":
			/* 人员基本信息 保存 */
			JSONObject useData = getJsonObjct(dc, "USER");

			if (StrUtil.isEmpty(useData.getString("PERSONID"))) {
				break;
			}

			// useData.put("WORKDATE", useData.getDate("WORKDATE"));
			// useData.put("DRIVINGDATE", useData.getDate("DRIVINGDATE"));
			// useData.put("ENTRYDATE", useData.getDate("ENTRYDATE"));
			// useData.put("SEPARATIONTIME", useData.getDate("SEPARATIONTIME"));
			// useData.put("RETIREMENTTIME", useData.getDate("RETIREMENTTIME"));
			// useData.put("JOINPARTYTIME", useData.getDate("JOINPARTYTIME"));
			// useData.put("BIRTHDATE", useData.getDate("BIRTHDATE"));

			bean = new DataBean("HR_PERSONNEL_BASE_IMAGE", "PERSONID", useData);

			setDefualFlag(bean);
			result = SaveBeanTool.saveRowForBean(this, hmLogSql, bean);
			break;
		case "USERDEPT":
			/* 认知记录保存 */

			JSONArray userdeptData = getJsonArray(dc, "USERDEPT");
			if (userdeptData == null) {
				return 0;
			}
			List<DataBean> userdeptList = new ArrayList<>();
			for (int i = 0; i < userdeptData.size(); i++) {
				JSONObject tmpobj = userdeptData.getJSONObject(i);

				tmpobj.put("GRADERANKTIME", tmpobj.getDate("GRADERANKTIME"));

				tmpobj.put("POSITIONTIME", tmpobj.getDate("POSITIONTIME"));

				tmp = new DataBean("HR_PERSONNEL_DEPT_POST_IMAGE", "PERSONDEPTPOSTID", tmpobj);
				setDefualFlag(tmp);

				userdeptList.add(tmp);
			}
			result = SaveBeanTool.saveRowForBeans(this, hmLogSql, userdeptList.toArray(new DataBean[userdeptList.size()]), "HR_PERSONNEL_DEPT_POST_IMAGE", "PERSONID", key);
			break;
		default:
			result = -9;
			break;
		}
		return result;
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
