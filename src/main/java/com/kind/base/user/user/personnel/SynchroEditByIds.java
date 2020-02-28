package com.kind.base.user.user.personnel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.base.user.service.user.IMCommonOperate;
import com.kind.common.stream.MqBean;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * @author
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = true, action = "#personnel_synchro_deptbase_by_ids", description = "根据部门id数组同步部门数据到本地", powerCode = "userpower.personnel.synchro_2", requireTransaction = true)
public class SynchroEditByIds extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String keys = dc.getRequestString("keys");
		String[] deptids = keys.split(",");
		// String type = dc.getRequestString("flag");
		int result = 0;
		// 消息List
		List<MqBean> mqList = new ArrayList<>();

		StringBuffer validateAddsql = new StringBuffer();
		List<Object> validatesqlParam = new ArrayList<>();

		for (int i = 0; i < deptids.length; i++) {
			if (!StrUtil.isEmpty(deptids[i])) {
				if (validateAddsql.length() > 0) {
					validateAddsql.append(",");
				}
				validateAddsql.append("?");
				validatesqlParam.add(deptids[i]);
			}
		}
		if (validatesqlParam.size() == 0) {
			return setFailInfo(dc, "参数传递不符合要求！");
		}
		// 判读父节点是否存在
		if (getSelectMap(hmLogSql, "SELECT 1 FROM SYS_DEPARTMENT_INFO_IMAGE A WHERE  A.DEPTID IN(" + validateAddsql + ") AND NOT EXISTS( SELECT 1 FROM SYS_DEPARTMENT_PARENT WHERE A.PARENTID=DEPTID ) ", validatesqlParam) != null) {
			return setFailInfo(dc, "存在未同步的上级部门，请检查！");
		}
		// 判读是否重名
		if (getSelectMap(hmLogSql, "SELECT 1 FROM SYS_DEPARTMENT_INFO_IMAGE A WHERE  A.DEPTID IN(" + validateAddsql + ") AND EXISTS(SELECT 1 FROM SYS_DEPARTMENT_INFO WHERE  ISDEL !=1  AND   A.DEPTNAME=DEPTNAME  AND A.PARENTID=PARENTID AND A.DEPTID!=DEPTID ) ", validatesqlParam) != null) {
			return setFailInfo(dc, "存在同级下的重复部门，请检查！");
		}

		for (int i = 0; i < validatesqlParam.size(); i++) {

			result = synchroData(this, hmLogSql, "SYS_DEPARTMENT_INFO_IMAGE", "DEPTID", validatesqlParam.get(i).toString(), new DataBean("SYS_DEPARTMENT_INFO", "DEPTID"), mqList);
			if (result == 0) {
				return setFailInfo(dc, "部门基本信息更新异常！");
			}
			result = synchroChildData(this, hmLogSql, "SYS_DEPARTMENT_TYPE_IMAGE", "DEPTID", validatesqlParam.get(i).toString(), new DataBean("SYS_DEPARTMENT_TYPE", "DEPTID,BVALUE"), mqList);
			if (result == 0) {
				return setFailInfo(dc, "部门基本信息更新异常！");
			}
		}

		if (mqList.size() > 0) {
			for (int i = 0; i < mqList.size(); i++) {
				MqBean mqBean = mqList.get(i);
				DataBean tmpbean = mqBean.getDataBean();

				if ("1".equals(tmpbean.getString("ISDEL"))) {
					result = IMCommonOperate.delDept(this, dc, tmpbean.getString("DEPTID"));
				} else {
					result = IMCommonOperate.updateDeptByBean(this, dc, tmpbean, mqBean.getBeanCmd());
				}
				if (result == 0) {
					return setFailInfo(dc, "部门基本信息更新异常！");
				}
			}

			// MqSendService sendService = SpringUtil.getBean(MqSendService.class);
			// if (!sendService.sendMessage(mqList)) {
			// return this.setFailInfo(dc, "消息发送失败！");
			// }
		}

		return this.setSuccessInfo(dc);
	}

	/**
	 * 同步主表单条数据
	 * 
	 * @param service
	 * @param hmLogSql
	 * @param tableName
	 * @param keyname
	 * @param key
	 * @param bean
	 * @return
	 */
	public int synchroData(BaseActionService service, HashMap<String, String> hmLogSql, String tableName, String keyname, String key, DataBean bean, List<MqBean> mqList) {
		int result = 0;
		List<Object> queryParams = new ArrayList<>();
		StringBuffer whereSql = new StringBuffer(" where 1=1");
		String keyNames[] = keyname.split(",");
		String keys[] = key.split(",");
		if (keyNames.length != keys.length) {
			return 0;
		}
		for (int i = 0; i < keyNames.length; i++) {
			whereSql.append(" and ").append(keyNames[i]).append("=?");
			queryParams.add(keys[i]);
		}

		Map<String, Object> imageMap = service.getSelectMap(hmLogSql, "select * from " + tableName + " " + whereSql + " and data_exchange_flag='1' ", queryParams);
		if (imageMap != null) {
			bean.loadMapData(imageMap);

			if (getOneFiledValue(hmLogSql, "select count(1) from " + bean.getTableName() + " " + whereSql, queryParams).toString().equals("0")) {
				result = service.insert(hmLogSql, bean);
				MqBean mqBean = new MqBean(bean, MqBean.CMD_A, null);
				mqList.add(mqBean);
			} else {
				result = service.update(hmLogSql, bean);
				MqBean mqBean = new MqBean(bean, MqBean.CMD_U, null);
				mqList.add(mqBean);
			}
			if (result > 0) {
				result = service.executeSql(hmLogSql, "update " + tableName + "  set data_exchange_flag='0' " + " " + whereSql, queryParams);
			}

		}
		return result;
	}

	/**
	 * 子表多条数据更新
	 * 
	 * @param service
	 * @param hmLogSql
	 * @param tableName
	 * @param linkName
	 * @param key
	 * @param bean
	 * @return
	 */
	public int synchroChildData(BaseActionService service, HashMap<String, String> hmLogSql, String tableName, String linkName, String key, DataBean bean, List<MqBean> mqlist) {
		int result = 1;
		List<Object> queryParams = new ArrayList<>();
		StringBuffer whereSql = new StringBuffer(" where 1=1 ");
		String keyNames[] = linkName.split(",");
		String keys[] = key.split(",");
		if (keyNames.length != keys.length) {
			return 0;
		}
		for (int i = 0; i < keyNames.length; i++) {
			whereSql.append(" and ").append(keyNames[i]).append("=?");
			queryParams.add(keys[i]);
		}
		DataBean[] imageBean = service.getSelectArrayDataBean(hmLogSql, "select * from " + tableName + " " + whereSql, queryParams);
		DataBean[] delBean = service.getSelectArrayDataBean(hmLogSql, "select * from " + bean.getTableName() + " " + whereSql, queryParams);
		if (delBean != null) {
			result = service.executeSql(hmLogSql, "delete from " + bean.getTableName() + " " + whereSql, queryParams);
			if (result == 0) {
				return result;
			}

			for (int i = 0; i < delBean.length; i++) {
				delBean[i].setTableName(bean.getTableName());
				delBean[i].setKeyField(bean.getKeyField());
				MqBean mqBean = new MqBean(delBean[i], MqBean.CMD_D, null);
				mqlist.add(mqBean);
			}
		}

		if (imageBean != null) {
			for (int i = 0; i < imageBean.length; i++) {
				imageBean[i].setTableName(bean.getTableName());
				imageBean[i].setKeyField(bean.getKeyField());
				// MqBean mqBean = new MqBean(imageBean[i], MqBean.CMD_A, null);
			}
			result = service.insert(hmLogSql, imageBean);
		}

		return result;
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
