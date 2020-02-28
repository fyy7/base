package com.kind.base.user.service.organdepttype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.stream.MqBean;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.util.StrUtil;

/**
 * 
 * @author chenzhiwei
 * 
 *         2018年4月24日
 */

@Service
@Action(requireLogin = true, action = "#organ_dept_type_save", description = "机构部门类型保存", powerCode = "", requireTransaction = true)
public class SaveOrganDeptTypeService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		// 消息List
		List<MqBean> mqList = new ArrayList<>();

		int result = 0;
		String cmd = dc.getRequestString("CMD");

		String organ_id = dc.getRequestString("ORGAN_ID");
		String dmlx = dc.getRequestString("DMLX");
		String dm = dc.getRequestString("DM");
		String oldOrganId = dc.getRequestString("OLD_ORGAN_ID");
		String oldDmlx = dc.getRequestString("OLD_DMLX");
		String oldDm = dc.getRequestString("OLD_DM");
		DataBean bean = getBean(dc, "ORGAN_DMB", "DMLX,DM,ORGAN_ID");
		boolean pkeyChange = ConCommon.CMD_U.equals(cmd) && !(StrUtil.equals(organ_id, oldOrganId) && StrUtil.equals(dmlx, oldDmlx) && StrUtil.equals(dm, oldDm));
		boolean add = ConCommon.CMD_A.equals(cmd);
		if (add || pkeyChange) {
			List<Object> param = new ArrayList<Object>();
			param.add(dmlx);
			param.add(dm);
			param.add(organ_id);
			String sql = "SELECT * FROM ORGAN_DMB WHERE DMLX=? AND DM=? AND ORGAN_ID=?";
			if (getSelectMap(hmLogSql, sql, param) != null) {
				return setFailInfo(dc, "该代码已经存在！");
			}
		}
		if (ConCommon.CMD_A.equals(cmd)) {
			result = this.insert(hmLogSql, bean);
			MqBean mqBean = new MqBean(bean, MqBean.CMD_A, null);
			mqList.add(mqBean);
		} else {
			DataBean db = getBean(dc, "ORGAN_DMB", "DMLX,DM,ORGAN_ID");
			db.set("DMLX_OLD", oldDmlx);
			db.set("DM_OLD", oldDm);
			db.set("ORGAN_ID_OLD", oldOrganId);
			result = update(hmLogSql, db);
			MqBean mqBean = new MqBean(bean, MqBean.CMD_U, null);
			mqList.add(mqBean);
		}
		if (result != 1) {
			return setFailInfo(dc, "保存失败！");
		}

		return setSuccessInfo(dc, "保存成功！");
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
