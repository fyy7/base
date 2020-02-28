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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * 权限委托管理
 * 
 * @author WangXiaoyi
 *
 */
@Service
@Action(requireLogin = true, action = "#sys_entrust_get_entrust_list", description = "权限委托管理之权限委托查询", powerCode = "resource.entrust", requireTransaction = false)

public class EntrustUserQuery extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		UserPO userpo = (UserPO) dc.getSessionObject(SpringUtil.getEnvProperty(Constant.APP_USER_SESSION_ID_PARAM));
		String organId = dc.getRequestString("ORGANID");
		String deptId = dc.getRequestString("DEPT_ID");
		if(StrUtil.isEmptyOrUndefined(organId)||StrUtil.isEmptyOrUndefined(deptId)) {
			dc.setBusiness(ConCommon.PAGINATION_ROWS, new ArrayList());
			return setSuccessInfo(dc);
		}
		

		StringBuffer sbSql = new StringBuffer("");
		// 人员列表
		sbSql.append("select A.*,B.ORGAN_ID,B.DEPT_ID,C.DEPTNAME,D.NAME ORGANNAME from SYS_N_USERS A,SYS_N_USER_DEPT_INFO B,SYS_DEPARTMENT_INFO C,SYS_ORGANIZATION_INFO D where B.ORGAN_ID=D.ORGANID AND C.DEPTID=B.DEPT_ID AND A.OPNO=B.OPNO AND B.ORGAN_ID='").append(organId).append("'");

		if (StrUtil.isNotEmpty(deptId)) {
			sbSql.append(" and C.ALLORDIDX like (select ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" from SYS_DEPARTMENT_INFO where DEPTID='").append(deptId).append("')");
		} else {
			// 自个部门下面的
			if (!(userpo.getOpType() < Integer.parseInt(ConCommon.NUM_2))) {
				sbSql.append(" and C.ALLORDIDX like (select ").append(ConvertSqlDefault.getStrJoinSQL("ALLORDIDX", "'%'")).append(" from SYS_DEPARTMENT_INFO where DEPTID='").append(userpo.getDeptId()).append("')");
			}
		}

		if (!(userpo.getOpType() < Integer.parseInt(ConCommon.NUM_2))) {
			// 不能对自个进行赋值
			sbSql.append(" and A.OPNO!='").append(userpo.getOpNo()).append("'");

			// 不能对别人对自已有赋权限的，再对他进行赋权限，造成循环
			sbSql.append(" and not exists(select 1 from SYS_N_ROLEUSER_ENTRUST s where s.DEPT_ID=C.DEPTID and A.OPNO=s.OPNO and s.OPNO='").append(userpo.getOpNo()).append("')");
			sbSql.append(" and not exists(select 1 from SYS_N_USERRIGHTS s where s.DEPT_ID=C.DEPTID and A.OPNO=s.OPNO and s.OPNO='").append(userpo.getOpNo()).append("')");
		}

		List<Map<String, Object>> list = this.getSelectList(hmLogSql, sbSql.toString(), new ArrayList<Object>());
		dc.setBusiness(ConCommon.PAGINATION_ROWS, list);

		return setSuccessInfo(dc);
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
