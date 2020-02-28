/**
 * @Title: DataBeanTool.java 
 * @Package com.kind.user.Interface 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author HUANGLEI 
 * @date 2018年8月28日 下午5:00:44 
 * @version V1.0   
 */
package com.kind.base.user.user.Interface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.kind.framework.core.bean.DataBean;
import com.kind.framework.service.BaseActionService;

/**
 * 一些特殊的操作方法
 * 
 * @author huanglei
 *
 */
public class SaveBeanTool {

	/**
	 * bean保存（在无法判定是新增还是修改时使用）
	 * 
	 * @param service
	 * @param hmLogSql
	 * @param bean
	 * @return
	 */
	public static int saveRowForBean(BaseActionService service, HashMap<String, String> hmLogSql, DataBean bean) {
		/* 获取当前记录数 */
		StringBuffer countSql = new StringBuffer("select count(1) from ");
		List<Object> countParam = new ArrayList<>();
		countSql.append(bean.getTableName()).append(" where 1=1");
		String keyfile = bean.getKeyField();
		String[] keyfiles = new String[1];
		if (keyfile.indexOf(",") > -1) {
			keyfiles = keyfile.split(",");
		} else {
			keyfiles[0] = keyfile;
		}

		for (int i = 0; i < keyfiles.length; i++) {
			countSql.append(" and ").append(keyfiles[i]).append("=?");
			countParam.add(bean.get(keyfiles[i].toUpperCase()));
		}
		String count = service.getOneFiledValue(hmLogSql, countSql.toString(), countParam).toString();
		int result = 0;
		if (Integer.valueOf(count) > 0) {
			result = service.update(hmLogSql, bean);
		} else {
			result = service.insert(hmLogSql, bean);
		}
		return result;
	}

	/**
	 * bean保存（在无法判定是新增还是修改时使用）
	 * 
	 * @param service
	 * @param hmLogSql
	 * @param beans
	 *            数组名称
	 * @param tablename
	 *            表名
	 * @param relationName
	 *            关联字段名
	 * @param relationKey
	 *            关联键值
	 * @return
	 */
	public static int saveRowForBeans(BaseActionService service, HashMap<String, String> hmLogSql, DataBean[] beans, String tablename, String relationName, String relationKey) {
		/* 获取当前记录数 */

		StringBuffer deletetSql = new StringBuffer("delete from " + tablename + " where " + relationName + "=? ");
		List<Object> countParam = new ArrayList<>();
		countParam.add(relationKey);

		int result = 0;
		result = service.executeSql(hmLogSql, deletetSql.toString(), countParam);
		if (result > 0) {
			result = service.insert(hmLogSql, beans);
		}
		return result;
	}
}
