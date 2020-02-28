/**
 * @Title: GotoResourceMain.java 
 * @Package com.kind.portal.service.resource 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月5日 下午4:39:00 
 * @version V1.0   
 */
package com.kind.base.auth.service.roles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.kind.base.core.auth.ResourceService;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.GenID;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * @author HUANGLEI
 *
 */
@Service
@Action(requireLogin = true, action = "#sys_role_operate_role_save", description = "角色授权", powerCode = "resource.role", requireTransaction = true)
public class Roles2UserSave extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String roleid = dc.getRequestString("ROLEID");
		String flag = dc.getRequestString("FLAG");
		String rows = dc.getRequestString("ROWS");
		String type = dc.getRequestString("TYPE");

		if (StrUtil.isEmpty(rows)) {
			return this.setFailInfo(dc, "没有获取到数据，无需操作！");
		}
		rows = rows.replaceAll("@", "\"");

		String s_curr_date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

		JSONArray arr_datas = JSON.parseArray(rows);
		if (arr_datas == null || arr_datas.size() < 1) {
			return this.setFailInfo(dc, "没有获取到数据，无需操作！");
		}

		// flag =0 用户列表，1为部门列表
		// type=1 增加，0为取消
		if ("1".equals(type)) {
			// 增加授权
			if ("0".equals(flag)) {
				// 用户列表
				for (int i = 0; i < arr_datas.size(); i++) {
					DataBean bean = new DataBean("SYS_N_ROLEUSER", "PID");
					bean.set("PID", GenID.gen(32));
					bean.set("OPNO", arr_datas.getJSONObject(i).get("OPNO"));
					// bean.set("CREATOR", ((UserBean) dc.getObjValue("SESSION_USER_BEAN")).getSysUserPO().getOpNo());
					bean.set("ROLEID", roleid);
					// bean.set("OPEMAN", ((UserBean) dc.getObjValue("SESSION_USER_BEAN")).getSysUserPO().getOpName());
					bean.set("OPETIME", s_curr_date);
					bean.set("DEPT_ID", arr_datas.getJSONObject(i).get("DEPT_ID"));
					if (insert(hmLogSql, bean) != 1) {
						return this.setFailInfo(dc, "数据操作失败！");
					}
				}
			} else {
				// 部门列表
				for (int i = 0; i < arr_datas.size(); i++) {
					DataBean bean = new DataBean("SYS_N_ROLE_DEPARTMENT", "PID");
					bean.set("PID", GenID.gen(32));
					bean.set("ROLEID", roleid);
					// bean.set("CREATOR", ((UserBean) dc.getObjValue("SESSION_USER_BEAN")).getSysUserPO().getOpNo());
					// bean.set("OPEMAN", ((UserBean) dc.getObjValue("SESSION_USER_BEAN")).getSysUserPO().getOpName());
					bean.set("OPETIME", s_curr_date);
					bean.set("DEPT_ID", arr_datas.getJSONObject(i).get("DEPT_ID"));
					if (insert(hmLogSql, bean) != 1) {
						return this.setFailInfo(dc, "数据操作失败！");
					}
				}
			}
		} else {
			StringBuffer sb_persion = new StringBuffer("");
			// 取消授权
			if ("0".equals(flag)) {
				StringBuffer sb_depts = new StringBuffer("");
				StringBuffer sb_opnos = new StringBuffer("");
				// 机构管理员
				int i_supper_admin = 0;
				// 用户列表
				for (int i = 0; i < arr_datas.size(); i++) {
					DataBean bean = new DataBean("SYS_N_ROLEUSER", "OPNO,DEPT_ID,ROLEID");
					bean.set("OPNO", arr_datas.getJSONObject(i).get("OPNO"));
					bean.set("ROLEID", roleid);
					bean.set("DEPT_ID", arr_datas.getJSONObject(i).get("DEPT_ID"));

					if (i_supper_admin == 0 && "DEPT_ID".equals(arr_datas.getJSONObject(i).get("DEPT_ID"))) {
						i_supper_admin = 1;
					}

					if (delete(hmLogSql, bean) != 1) {
						return this.setFailInfo(dc, "数据操作失败！");
					}
					sb_depts.append("'").append(arr_datas.getJSONObject(i).get("DEPT_ID")).append("',");
					sb_opnos.append("'").append(arr_datas.getJSONObject(i).get("OPNO")).append("',");
					// sb_persion.append(arr_datas.getJSONObject(i).get("OPNO")).append("@").append(arr_datas.getJSONObject(i).get("DEPT_ID")).append("//");
				}

				if (sb_depts.toString().length() > 0) {
					if (i_supper_admin == 0) {
						// 这里部门需要排序
						StringBuffer sb_sql = new StringBuffer("select A.OPNO,B.DEPT_ID FROM SYS_N_USERS A,SYS_N_USER_DEPT_INFO B,SYS_ORGANIZATION_INFO C,SYS_DEPARTMENT_INFO D WHERE A.OPNO=B.OPNO");
						sb_sql.append(" AND B.DEPT_ID=D.DEPTID AND D.ORGANID=C.ORGANID");
						sb_sql.append(" AND B.DEPT_ID in(").append(sb_depts.substring(0, sb_depts.length() - 1)).append(")");
						sb_sql.append(" AND A.OPNO in(").append(sb_opnos.substring(0, sb_opnos.length() - 1)).append(")");
						sb_sql.append(" ORDER BY C.ALLORDIDX,D.ALLORDIDX");

						List<Map<String, Object>> arr_person = getSelectList(hmLogSql, sb_sql.toString(), new ArrayList<Object>());
						if (arr_person != null && arr_person.size() > 0) {
							for (Map<String, Object> d_bean : arr_person) {
								sb_persion.append(d_bean.get("OPNO")).append("@").append(d_bean.get("DEPT_ID")).append("//");
							}
						}
					} else {
						// 机构管理员
						String[] s_arr_opno = sb_opnos.substring(0, sb_opnos.length() - 1).replaceAll("'", "").split(",");
						if (s_arr_opno != null && s_arr_opno.length > 0) {
							for (String s : s_arr_opno) {
								sb_persion.append(s).append("@").append("DEPT_ID").append("//");
							}
						}
					}
				}
			} else {
				StringBuffer sb_depts = new StringBuffer("");
				// 部门列表
				for (int i = 0; i < arr_datas.size(); i++) {
					DataBean bean = new DataBean("SYS_N_ROLE_DEPARTMENT", "DEPT_ID,ROLEID");
					bean.set("ROLEID", roleid);
					bean.set("DEPT_ID", arr_datas.getJSONObject(i).get("DEPT_ID"));
					if (delete(hmLogSql, bean) != 1) {
						return this.setFailInfo(dc, "数据操作失败！");
					}
					sb_depts.append("'").append(arr_datas.getJSONObject(i).get("DEPT_ID")).append("',");
				}
				if (sb_depts.toString().length() > 0) {
					// 这里部门需要排序
					StringBuffer sb_sql = new StringBuffer("select A.OPNO,B.DEPT_ID FROM SYS_N_USERS A,SYS_N_USER_DEPT_INFO B,SYS_ORGANIZATION_INFO C,SYS_DEPARTMENT_INFO D WHERE A.OPNO=B.OPNO");
					sb_sql.append(" AND B.DEPT_ID=D.DEPTID AND D.ORGANID=C.ORGANID");
					sb_sql.append(" AND B.DEPT_ID in(").append(sb_depts.substring(0, sb_depts.length() - 1)).append(")");
					sb_sql.append(" ORDER BY C.ALLORDIDX,D.ALLORDIDX");

					List<Map<String, Object>> arr_person = getSelectList(hmLogSql, sb_sql.toString(), new ArrayList<Object>());
					if (arr_person != null && arr_person.size() > 0) {
						for (Map<String, Object> d_bean : arr_person) {
							sb_persion.append(d_bean.get("OPNO")).append("@").append(d_bean.get("DEPT_ID")).append("//");
						}
					}
				}
			}
		}
		// 权限回收
		ResourceService rs = SpringUtil.getBean(ResourceService.class);
		rs.recoveryRights();
		return this.setSuccessInfo(dc, "操作成功！");
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
