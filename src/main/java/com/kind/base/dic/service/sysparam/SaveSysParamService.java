package com.kind.base.dic.service.sysparam;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.utils.CodeSwitching;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.GenID;

/**
 * @author majiantao
 *
 *         2018年2月27日
 */
@Service
@Action(requireLogin = true, action = "#save_sysparam", description = "参数管理数据保存", powerCode = "dictionary.parameter_2", requireTransaction = true)
public class SaveSysParamService extends BaseActionService {

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String cmd = dc.getRequestString("CMD");
		DataBean bean = this.getBean(dc, "SYS_SYSPARM", "UUID");

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		int result = 0;
		if (ConCommon.CMD_A.equals(cmd)) {

			// 判断参数号是否重复
			List<Object> param = new ArrayList<Object>();
			param.add(bean.get("PARAID"));
			String count = getOneFiledValue(hmLogSql, "select count(1) from sys_sysparm where paraid=? ", param).toString();
			if (!ConCommon.NUM_0.equals(count)) {
				return setFailInfo(dc, "参数号重复！");
			}

			bean.set("UUID", GenID.gen(36));
			bean.set("INITDATE", sdf.format(date));
			bean.set("UPDATEDATE", sdf.format(date));
			result = this.insert(hmLogSql, bean);
		} else if (ConCommon.CMD_U.equals(cmd)) {
			bean.set("UPDATEDATE", sdf.format(date));
			result = this.update(hmLogSql, bean);
		}

		if (result != 1) {
			return setFailInfo(dc, "保存失败！");
		}

		// 重置系统参数radis缓存
		CodeSwitching.putDataByFlag("sys");

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

	public static void main(String[] args) {
		// Date date = new Date();
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// System.out.println(sdf.format(date));
	}
}
