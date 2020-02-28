package com.kind.base.user.service.dept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * @author LIUHAORAN
 *
 *         2018年2月22日
 */
@Service
@Action(requireLogin = true, action = "#dept_tree_data", description = "获取部门树数据", powerCode = "", requireTransaction = false)
public class GetDeptTreeData extends BaseActionService {
	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String organid = dc.getRequestString("ORGANID");
		String deptid = dc.getRequestString("DEPTID");
		String ischild = dc.getRequestString("ISCHILD");

		List<Object> paramList = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("SELECT A.DEPTID,A.PARENTID,A.DEPTNAME,A.ALLORDIDX,B.ACCOUNT_PERFIX FROM SYS_DEPARTMENT_INFO A,SYS_ORGANIZATION_INFO B  WHERE A.ORGANID=B.ORGANID AND A.ISDEL=0 AND B.ISDEL=0 ");
		if (StrUtil.isEmpty(deptid)) {
			sql.append("AND A.ORGANID=?");
			paramList.add(organid);

		} else if ("1".equals(ischild) && !StrUtil.isEmpty(deptid)) {
			sql.append("AND A.ALLORDIDX like (select ALLORDIDX||'%' from SYS_DEPARTMENT_INFO where DEPTID=?  )");
			paramList.add(deptid);
		} else {
			sql.append("AND A.DEPTID=?");
			paramList.add(deptid);
		}

		sql.append(" ORDER BY A.ALLORDIDX");

		List<Map<String, Object>> list = this.getSelectList(hmLogSql, sql.toString(), paramList);
		JSONArray jsonArr = new JSONArray();
		if (list != null) {
			for (Map<String, Object> map : list) {
				map.put("id", map.get("DEPTID"));
				map.put("pId", map.get("PARENTID"));
				map.put("name", map.get("DEPTNAME"));
				;
				jsonArr.add(JSON.parseObject(JSON.toJSONString(map)));
			}
		}

		dc.setBusiness("TREE_DATAS", jsonArr.toJSONString());
		return this.setSuccessInfo(dc, "获取成功！");
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
