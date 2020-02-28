/**
 * @Title: GotoResourceMain.java 
 * @Package com.kind.portal.service.resource 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年3月5日 下午4:39:00 
 * @version V1.0   
 */
package com.kind.base.auth.service.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.base.auth.cloudservice.auth.ResourcesResetRedis;
import com.kind.common.constant.ConCommon;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.GenID;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author HUANGLEI
 *
 */
@Service
@Action(requireLogin = true, action = "#sys_resoure_save1", description = "资源保存", powerCode = "resource.module", requireTransaction = true)
public class ResourceSave2 extends BaseActionService {
	private String rid = null;
	private String superId = null;
	private String appid = null;
	private String channelRtype = null;
	private boolean istrue;

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		DataBean bean = getBean(dc, "SYS_RESOURCES", "RID");
		rid = dc.getRequestString("RID");
		superId = dc.getRequestString("PARENTID");
		appid = dc.getRequestString("APP_ID");
		channelRtype = dc.getRequestString("CHANNEL_RTYPE");
		if (StrUtil.isEmpty(appid)) {
			return setFailInfo(dc, "系统标识不能为空！");
		}
		if (StrUtil.isEmpty(superId)) {
			return setFailInfo(dc, "父类资源组不能为空！");
		}
		if (StrUtil.isEmpty(channelRtype)) {
			return setFailInfo(dc, "类型标识不能为空！");
		}

		StringBuffer checksql;
		// 判断是否添加
		boolean isAdd = false;
		List<Object> paramList = new ArrayList<>();
		// 解决注入的资源默写字段为空导致的序号生成异常
		this.executeSql(hmLogSql, "update SYS_RESOURCES set RLEVEL='1',RTYPE='1',ORDIDX='1',ALLORDIDX='0.0001' where CHANNEL_RTYPE='00000' and ALLORDIDX is null", Arrays.asList());

		if (StrUtil.isEmpty(rid)) {

			// String haveTopSql = "select 1 from SYS_RESOURCES where APP_ID=? and PARENTID='ResourceTop' and CHANNEL_RTYPE=?";
			if (StrUtil.isEmpty(superId)) {

				// if (StrUtil.isEmpty(superId) && getSelectMap(hmLogSql, haveTopSql, Arrays.asList(appid, channelRtype)) == null) {
				// DataBean topBean = new DataBean("SYS_RESOURCES", "RID");
				//
				// String key = "R" + GenID.gen(19);
				// topBean.set("RID", key);
				// topBean.set("PARENTID", "ResourceTop");
				// topBean.set("RLOGO", "sysmodule");
				// topBean.set("ALLORDIDX", "0.0001");
				// topBean.set("TITLE", "系统顶级模块");
				// topBean.set("RLEVEL", "1");
				// topBean.set("RTYPE", "1");
				// topBean.set("ORDIDX", "1");
				// topBean.set("APP_ID", appid);
				// topBean.set("ISGROUP", "1");
				// topBean.set("SUBSYSTEM", "all");
				// topBean.set("VISIBLE", "1");
				// topBean.set("CHANNEL_RTYPE", channelRtype);
				//
				// if (insert(hmLogSql, topBean) == 0) {
				// return this.setFailInfo(dc);
				// }

				superId = appid;
				bean.set("PARENTID", appid);
			}
			// 添加
			isAdd = true;
			checksql = new StringBuffer("select count(1) NUM  from SYS_RESOURCES where app_id=? and  (TITLE = ? and PARENTID = ?)");
			checksql.append(" or (RLOGO=?)");
			paramList.add(appid);
			paramList.add(bean.getString("TITLE"));
			paramList.add(bean.getString("PARENTID"));
			paramList.add(bean.getString("RLOGO"));
			bean.set("ISRIGHT", 1);
		} else {
			// 修改

			checksql = new StringBuffer("select count(1) NUM  from SYS_RESOURCES where  app_id=? and  (TITLE = ? and RID != ? and PARENTID = ?)");
			checksql.append(" or (RLOGO=? and RID != ?)");
			paramList.add(appid);
			paramList.add(bean.getString("TITLE"));
			paramList.add(bean.getString("RID"));
			paramList.add(bean.getString("PARENTID"));
			paramList.add(bean.getString("RLOGO"));
			paramList.add(bean.getString("RID"));
		}

		// 判断模块标识和title是否已经存在
		// 同级名称重或者标识重
		Map<String, Object> checkTitle = getSelectMap(hmLogSql, checksql.toString(), paramList);
		istrue = (checkTitle != null) && checkTitle.get("NUM") != null;
		if (istrue) {
			int count = ((Number) checkTitle.get("NUM")).intValue();
			if (count > 0) {
				return this.setFailInfo(dc, "模块标识和标题已经存在，保存失败！");
			}
		}

		String parentallordidx = null;
		String allordidx, oldallordidx = null;
		String oldsuper = null;
		int ordidx = 1;

		if (isAdd) {
			rid = "R" + GenID.gen(19);
			bean.set("RID", rid);
		} else {
			Map<String, Object> resources = getSelectMap(hmLogSql, "select PARENTID,RLEVEL,ALLORDIDX from SYS_RESOURCES where RID =?  ", Arrays.asList(rid));
			if (resources != null) {
				oldsuper = (String) (resources.get("PARENTID"));
				oldallordidx = (String) (resources.get("ALLORDIDX"));
			}
			Object o = getOneFiledValue(hmLogSql, "select count(*) from SYS_RESOURCES where PARENTID=? and channel_rtype='00001'", Arrays.asList(rid));
			if (Integer.valueOf(String.valueOf(o)) > 0) {
				// 是否资源组，看是否有下层资源
				bean.set("ISGROUP", 1);
			}
		}

		if (isAdd || !superId.equals(oldsuper)) {
			List<Map<String, Object>> resources = null;
			// 同级最大排序号

			resources = getSelectList(hmLogSql, "select max(ORDIDX) as MAXORDIDX from SYS_RESOURCES where PARENTID =?", Arrays.asList(superId));
			if (resources.size() > 0) {
				istrue = ObjectUtil.isNull(resources.get(0).get("MAXORDIDX"));
				if (!istrue) {
					ordidx = Integer.valueOf(String.valueOf(resources.get(0).get("MAXORDIDX"))) + 1;
				}
			}

			bean.set("ORDIDX", ordidx);
			bean.set("RLEVEL", 1);

			// 级别
			resources = getSelectList(hmLogSql, "select RLEVEL,ALLORDIDX from SYS_RESOURCES where RID =?", Arrays.asList(superId));
			if (resources.size() == 1) {
				int rlevel = Integer.valueOf(String.valueOf(resources.get(0).get("RLEVEL")));
				bean.set("RLEVEL", rlevel + 1);
				parentallordidx = (String) (resources.get(0).get("ALLORDIDX"));
			}

			if (parentallordidx == null) {
				parentallordidx = "0";
			}
			// 补0
			allordidx = String.valueOf(parentallordidx) + "." + String.valueOf(new java.text.DecimalFormat("0000").format(ordidx));
			bean.set("ALLORDIDX", allordidx);

			// sybase:substring,length
			// oracle:substr,length
			if (oldallordidx != null && !oldallordidx.equals("")) {
				// 原来具有上下文排序
				if (!oldallordidx.equals(allordidx)) {
					// 上下文排序号有修改，则子节点一起修改
					String replacesql = "update SYS_RESOURCES set ALLORDIDX=" + ConvertSqlDefault.getStrJoinSQL("'" + allordidx + "'", ConvertSqlDefault.substrSQL("ALLORDIDX", (allordidx.length() + 1) + "", "length(ALLORDIDX)")) + " where ALLORDIDX like '" + oldallordidx + "%'";
					if (executeSql(hmLogSql, replacesql, new ArrayList<Object>()) != 1) {
						return this.setFailInfo(dc, "替换上下文排序号失败！");
					}
				}
			}
		}
		if (isAdd) {
			if (insert(hmLogSql, bean) == 0) {
				return this.setFailInfo(dc);
			}
		} else {
			if (update(hmLogSql, bean) == 0) {
				return this.setFailInfo(dc);
			}
		}

		// 重新处理应用范围
		if (executeSql(hmLogSql, "delete from SYS_RESOURCES_ORGS where RID=?", Arrays.asList(rid)) == 0) {
			return this.setFailInfo(dc);
		}

		// 应用范围的处理
		istrue = ConCommon.NUM_1.equals(bean.getString("SCOPE"));
		if (!istrue) {
			if (handleOrgids(hmLogSql, dc, rid) == 0) {
				return this.setFailInfo(dc);
			}
		}
		// 重新处理工作流范围
		if (executeSql(hmLogSql, "delete from sys_resources_workflow where RID=?", Arrays.asList(rid)) == 0) {
			return this.setFailInfo(dc);
		}

		String workflows = dc.getRequestString("WORKFLOWS");
		String workflowsName = dc.getRequestString("WORKFLOWS_NAME");
		if (StrUtil.isNotEmpty(workflows)) {
			String[] workflowArr = workflows.split(",");
			String[] workflowNameArr = workflowsName.split(",");
			for (int i = 0; i < workflowArr.length; i++) {
				if (StrUtil.isNotEmpty(workflowArr[i])) {
					DataBean o = new DataBean();
					o.setTableName("sys_resources_workflow");
					o.set("RID", bean.getString("RID"));
					o.set("FLOW_ID", workflowArr[i]);
					o.set("FLOW_NAME", workflowNameArr[i]);
					o.set("FLOW_INDEX", i+1);
					if (insert(hmLogSql, o) == 0) {
						return this.setFailInfo(dc);
					}
				}
			}
		}
		List<Map<String, Object>> workflowlist = getSelectList(hmLogSql, "select "+ConvertSqlDefault.getStrJoinSQL("RID","'_'","FLOW_ID")+"  AS RID,RID AS  PARENTID ,FLOW_NAME  AS  TITLE," + ConvertSqlDefault.getStrJoinSQL("'"+bean.getString("RLOGO")+"'", "'_'","FLOW_ID")+" as RLOGO from SYS_RESOURCES_WORKFLOW where   rid=?", Arrays.asList(rid));
		if (updateResByPid(hmLogSql, workflowlist, appid, "00004", rid,bean.getString("TITLE")+"@工作流") == 0) {
			return this.setFailInfo(dc);
		}
		// 处理功能按钮资源
		if (executeSql(hmLogSql, "delete from SYS_RESOURCES_BUTTON where RID=?", Arrays.asList(rid)) == 0) {
			return this.setFailInfo(dc);
		}
		String[] arrBselect = (String[]) dc.getRequestObject("BSELECT");
		String[] arrBvalue = (String[]) dc.getRequestObject("BVALUE");
		String[] arrBtitle = (String[]) dc.getRequestObject("BTITLE");
		if (arrBselect != null) {
			for (int i = 0; i < arrBselect.length; i++) {
				if (arrBselect[i].equals("1")) {
					DataBean o = new DataBean();
					o.setTableName("SYS_RESOURCES_BUTTON");
					o.set("RID", bean.getString("RID"));
					o.set("BVALUE", arrBvalue[i]);
					o.set("BTITLE", arrBtitle[i]);

					if (insert(hmLogSql, o) == 0) {
						return this.setFailInfo(dc);
					}
				}
			}
		}

		List<Map<String, Object>> list = getSelectList(hmLogSql, "select " + ConvertSqlDefault.getStrJoinSQL("RID", "'_'", "BVALUE") + "  AS RID,RID AS  PARENTID ,BTITLE  AS  TITLE," + ConvertSqlDefault.getStrJoinSQL("'" + bean.getString("RLOGO") + "'", "'_'", "BVALUE") + " as RLOGO from SYS_RESOURCES_BUTTON where   rid=?", Arrays.asList(rid));
		if (updateResByPid(hmLogSql, list, appid, "00002", rid,bean.getString("TITLE")+"@按钮") == 0) {
			return this.setFailInfo(dc);
		}
		dc.setBusiness("RID", rid);

		// 重置中的资源缓存
		ResourcesResetRedis rrr = new ResourcesResetRedis();
		rrr.resetRedis(hmLogSql, appid);

		return setSuccessInfo(dc);
	}

	private int handleOrgids(HashMap<String, String> hmLogSql, DataContext dc, String rid) {
		istrue = StrUtil.isEmpty(dc.getRequestString("SCOPE_ORGIDS"));
		if (!istrue) {
			String[] arrIds = dc.getRequestString("SCOPE_ORGIDS").split(",");
			for (String orgid : arrIds) {
				DataBean bean = new DataBean("SYS_RESOURCES_ORGS", "RID");
				bean.set("RID", rid);
				bean.set("ORGANID", orgid);
				if (insert(hmLogSql, bean) == 0) {
					return 0;
				}
			}
		}
		return 1;
	}

	@Override
	public String pageAddress(DataContext arg0) {

		return null;
	}

	@Override
	public String verifyParameter(DataContext arg0) {

		return null;
	}

	/**
	 * 根据父类id初始化所有子类资源
	 */
	public int updateResByPid(HashMap<String, String> hmLogSql, List<Map<String, Object>> list, String appId, String channelRtype, String parentid) {
		if (!StrUtil.isEmpty(parentid)) {
			executeSql(hmLogSql, "delete FROM SYS_RESOURCES where app_id=? and channel_rtype=? and parentid=?", Arrays.asList(appId, channelRtype, parentid));
			for (Map<String, Object> map : list) {
				DataBean o = new DataBean();
				o.setTableName("SYS_RESOURCES");
				o.set("RID", map.get("RID"));
				o.set("APP_ID", appId);
				o.set("TITLE", map.get("TITLE"));
				o.set("CHANNEL_RTYPE", channelRtype);
				o.set("PARENTID", parentid);
				o.set("ALLORDIDX", map.get("ALLORDIDX"));
				o.set("RLOGO", map.get("RLOGO"));
				if (insert(hmLogSql, o) == 0) {
					return 0;
				}
			}
		}
		return 1;
	}
	public int updateResByPid(HashMap<String, String> hmLogSql, List<Map<String, Object>> list, String appId, String channelRtype, String parentid,String titleAddStr) {
		if (!StrUtil.isEmpty(parentid)) {
			executeSql(hmLogSql, "delete FROM SYS_RESOURCES where app_id=? and channel_rtype=? and parentid=?", Arrays.asList(appId, channelRtype, parentid));
			for (Map<String, Object> map : list) {
				DataBean o = new DataBean();
				o.setTableName("SYS_RESOURCES");
				o.set("RID", map.get("RID"));
				o.set("APP_ID", appId);
				o.set("TITLE", map.get("TITLE")+(StrUtil.isNotEmpty(titleAddStr)?"("+titleAddStr+")":""));
				o.set("CHANNEL_RTYPE", channelRtype);
				o.set("PARENTID", parentid);
				o.set("ALLORDIDX", map.get("ALLORDIDX"));
				o.set("RLOGO", map.get("RLOGO"));
				if (insert(hmLogSql, o) == 0) {
					return 0;
				}
			}
		}
		return 1;
	}
}
