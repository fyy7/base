/**
 * @Title: GotoResourceMain.java 
 * @Package com.kind.portal.service.resource 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月5日 下午4:39:00 
 * @version V1.0   
 */
package com.kind.base.auth.service.entrust;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.GenID;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 权限委托管理
 * 
 * @author WangXiaoyi
 *
 */
@Service
@Action(requireLogin = true, action = "#sys_entrust_save", description = "权限委托管理之新增、修改", powerCode = "resource.entrust", requireTransaction = true)

public class EntrustSave extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String rolerightsaddlist = dc.getRequestString("RoleRights_Add");
		String rolerightsdellist = dc.getRequestString("RoleRights_Del");
		String beginData = 	dc.getRequestString("BEGINDATE");
		String endData = 	dc.getRequestString("ENDDATE");
		if(StrUtil.isEmpty(beginData) || StrUtil.isEmpty(endData)) {
		return setFailInfo(dc,"委托期限不能为空！");
		}

		DataBean bean = getBean(dc, "SYS_N_ROLES_ENTRUST", "ROLEID");
		String roleId = String.valueOf(bean.get("ROLEID"));
		String roleName = String.valueOf(bean.get("ROLENAME"));

		UserPO userPo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));

		String sql = "select ROLEID from SYS_N_ROLES_ENTRUST where ROLEID<>? and ROLENAME =? and CREATOR=?";
		if (getSelectMap(hmLogSql, sql, Arrays.asList(roleId, roleName, userPo.getOpNo())) != null) {
			return setFailInfo(dc, "受托人名称已经存在，请重新命名！");
		}

		bean.set("OPETIME", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
		bean.set("DEPT_ID", userPo.getDeptId());
		
		
		// 保存用户信息
		String pid = dc.getRequestString("PID");
		String opno = dc.getRequestString("OPNO");
		String deptId = dc.getRequestString("DEPT_ID");

		//判断委托人是否可以委托
		List checkParam=new ArrayList();
		StringBuffer checkSql=new StringBuffer("SELECT OPEMAN FROM SYS_N_ROLES_ENTRUST A WHERE ( ");


			checkSql.append("(( ? BETWEEN BEGINDATE AND ENDDATE ) OR ( ? BETWEEN BEGINDATE AND ENDDATE ) OR (?<BEGINDATE and  ? >ENDDATE))");	
			checkParam.add(beginData);
			checkParam.add(endData);
			checkParam.add(beginData);
			checkParam.add(endData);
		checkSql.append(" ) AND  CREATOR=? AND EXISTS(SELECT 1 FROM SYS_N_ROLES_ENTRUST WHERE A.ROLEID=ROLEID) ");	
		checkParam.add(opno);
		
		Map selectMap = getSelectMap(hmLogSql, checkSql.toString(), checkParam);
		if(selectMap!=null) {
			return setFailInfo(dc, "委托失败！用户："+selectMap.get("OPEMAN")+"在该时段存在委托记录！");
		}
		
		// 保存委托信息
		int result;
		String saveSql = "select 1 from SYS_N_ROLES_ENTRUST where ROLEID=?";
		if (getSelectMap(hmLogSql, saveSql, Arrays.asList(roleId)) == null) {
			bean.set("CREATOR", userPo.getOpNo());
			bean.set("OPEMAN", userPo.getOpName());
			result = insert(hmLogSql, bean);
		} else { // 修改
			result = update(hmLogSql, bean);
		}

		if (result != 1) {
			return setFailInfo(dc, "保存委托信息失败！");
		}

	
		DataBean roleUserBean = new DataBean("SYS_N_ROLEUSER_ENTRUST", "PID");
		roleUserBean.set("OPNO", opno);
		roleUserBean.set("ROLEID", roleId);
		roleUserBean.set("DEPT_ID", deptId);

		if (StrUtil.isEmpty(pid)) {
			roleUserBean.set("PID", GenID.gen(32));
			roleUserBean.set("OPETIME", DateUtil.date());
			if (insert(hmLogSql, roleUserBean) != 1) {
				return setFailInfo(dc, "数据操作失败！");
			}
		} else {
			roleUserBean.set("PID", pid);
			if (update(hmLogSql, roleUserBean) != 1) {
				return setFailInfo(dc, "数据操作失败！");
			}
		}

		// 保存委托权限
		DataBean beanRolerights = new DataBean("SYS_N_ROLERIGHTS_ENTRUST", "ROLEID,RESOURCEID");
		beanRolerights.set("OPEMAN", userPo.getOpName());
		beanRolerights.set("OPETIME", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

		// 处理委托中权限资源的删除
		if (!StrUtil.isEmpty(rolerightsdellist)) {

			String[] arrRoleRightsDel = rolerightsdellist.split(";");
			for (String sResourceId : arrRoleRightsDel) {
				beanRolerights.set("ROLEID", roleId);
				beanRolerights.set("RESOURCEID", sResourceId);

				// 删除委托中的权限资源ID
				String rolerightsEntrustSql = "delete FROM SYS_N_ROLERIGHTS_ENTRUST where ROLEID=? and RESOURCEID=?";
				if (executeSql(hmLogSql, rolerightsEntrustSql, Arrays.asList(roleId, sResourceId)) != 1) {
					return setFailInfo(dc, "取消委托权限信息失败！");
				}
			}
		}

		// 增加的权限资源
		if (!StrUtil.isEmpty(rolerightsaddlist)) {
			String[] arrRoleRightsAdd = rolerightsaddlist.split(";");
			for (int i = 0; i < arrRoleRightsAdd.length; i++) {
				beanRolerights.set("ROLEID", roleId);
				beanRolerights.set("RESOURCEID", arrRoleRightsAdd[i]);
				String deleteRolerightsEntrustSql = "delete FROM SYS_N_ROLERIGHTS_ENTRUST where ROLEID=? and RESOURCEID=?";
				executeSql(hmLogSql, deleteRolerightsEntrustSql, Arrays.asList(roleId, arrRoleRightsAdd[i]));
				if (insert(hmLogSql, beanRolerights) != 1) {
					return setFailInfo(dc, "保存委托权限信息失败！");
				}
			}
		}

		// 保存结果提示
		if (result == 1) {
			return setSuccessInfo(dc, "保存成功！");
		} else {
			return setFailInfo(dc, "保存失败！");
		}
	}

	@Override
	public String pageAddress(DataContext dc) {

		return null;
	}

	@Override
	public String verifyParameter(DataContext dc) {

		return null;
	}

}
