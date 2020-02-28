package com.kind.base.auth.cloudservice.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kind.framework.core.Action;
import com.kind.framework.core.Constant;
import com.kind.framework.core.bean.DataBean;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 注入渠道系统资源，http的话需要post请求
 * 
 * @author yanhang
 */
@Service
@Action(requireLogin = false, action = "#inject_channel_resource", description = "注入渠道系统资源", powerCode = "", requireTransaction = true)
public class InjectChannelResourcesService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(InjectChannelResourcesService.class);

	private Set<String> rids = new HashSet<String>();
	private Set<String> rlogos = new HashSet<String>();

	private Map<String, Object> all_allordidx = new HashMap<String, Object>();

	// private CommonService commonService = new CommonService();

	/**
	 * HTTP需post请求<br>
	 * http://my.kind.com/portal/restful?action=inject_channel_resource&TOKEN=&APP_ID=APP90001&INTERNAL=1&RESOURCES=JSON_STR([{}, ...]) <br>
	 * 
	 * RESOURCES：json数组（树结构）的字符串格式，字段定义如下：<br>
	 * RID TITLE PID CRTP(channel_rtype) INTERNAL不填或为0时，该字段无效 VIS(visible) GRUP(isgroup) ORD(ordidx) EXT1(extitem1) EXT2(extitem2) CHLD(childs) 子节点列表 RLOGO 内部调用时有效
	 * 
	 * <br>
	 * 示例数据：<br>
	 * [ { "RID": "TSTRID001", "TITLE": "TSTTIL001", "PID": "TSTPID001", "CRTP": "90001", "VIS": "1", "GRUP": "1", "ORD": "33", "EXT1": "EXT1001", "EXT2": "EXT2001", "CHLD": [ { "RID": "TSTRID001001", "TITLE": "TSTTIL001001", "PID": "TSTRID001", "CRTP": "90001", "VIS": "0", "GRUP": "0", "ORD": "199", "EXT1": "EXT1001001", "EXT2": "EXT2001001", "CHLD": [ ] }, { "RID": "TSTRID001002", "TITLE": "TSTTIL001002", "PID": "TSTRID001", "CRTP": "90002", "VIS": "1", "GRUP": "1", "ORD": "2", "EXT1": "EXT1001002", "EXT2": "EXT2001002", "CHLD": [ { "RID": "TSTRID001002001", "TITLE": "TSTTIL001002001", "PID": "TSTRID001002", "CRTP": "90003", "VIS": "1", "GRUP": "0", "ORD": "1", "EXT1": "EXT1001002001", "EXT2": "EXT2001002001", "CHLD": [ ] }, { "RID": "TSTRID001002002", "TITLE": "TSTTIL001002002", "PID": "TSTRID001002", "CRTP": "90003", "VIS": "1", "GRUP": "0", "ORD": "2", "EXT1": "EXT1001002002", "EXT2": "EXT2001002002", "CHLD": [ ] } ] } ] }, { "RID": "TSTRID002", "TITLE": "TSTTIL002", "PID": "TSTPID001", "CRTP": "90001", "VIS": "1", "GRUP": "0", "ORD": "5", "EXT1": "EXT1002", "EXT2": "EXT2002", "CHLD": [ ] } ]
	 */
	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {

		String msg = "";
		try {
			// String token = dc.getRequestObject("TOKEN", "");
			String channelId = dc.getRequestObject("APP_ID", "");
			String internal = dc.getRequestObject("INTERNAL", "0"); // 指示是否内部调用，1是，0否

			// String ret = commonService.doAccessValid(dc, token, channelId);
			// if (StrUtil.isNotBlank(ret)) {
			// return setFailInfo(dc, ret + "，资源注入失败！");
			// }

			String resourcesData = dc.getRequestObject("RESOURCES", "");
			JSONArray array = JSONObject.parseArray(resourcesData);
			if (array == null || array.size() == 0) {
				return setFailInfo(dc, "资源数据为空，资源注入失败！");
			}

			if (StrUtil.isBlank(channelId)) {
				return setFailInfo(dc, "APP_ID为空，资源注入失败！");
			}

			all_allordidx = this.getAppAllOrdidxs(hmLogSql, channelId);

			// 根据ordidx排序
			sortByOrdidx(array);

			Set<DataBean> beans = getBeans(dc, channelId, array, "0." + getAppNo(channelId), "1".equals(internal));
			if (beans == null) {
				return dc.getBusinessInt(Constant.FRAMEWORK_G_RESULT);
			}

			List<Object> param = new ArrayList<Object>();
			param.add(channelId);

			StringBuffer _rids = new StringBuffer("");

			boolean f_start = false;
			for (String rid : rids) {
				if (f_start) {
					_rids.append(",");
				}
				_rids.append("?");
				param.add(rid);
				f_start = true;
			}

			if (StrUtil.isNotBlank(_rids.toString())) {
				// 删除原有数据
				int result = this.executeSql(hmLogSql, " DELETE FROM SYS_RESOURCES WHERE APP_ID = ? AND RID IN (" + _rids.toString() + ") ", param);
				if (result != 1) {
					return setFailInfo(dc, "覆盖旧数据失败，资源注入失败！");
				}

				// Object _num = this.getOneFiledValue(hmLogSql, " SELECT count(1) FROM SYS_RESOURCES WHERE APP_ID = ? and RID IN (" + _rids.toString() + ") ", param);
				// if (!"0".equals(String.valueOf(_num))) {
				// return setFailInfo(dc, "资源ID已存在，资源注入失败！");
				// }
			}

			param = new ArrayList<Object>();
			f_start = false;
			// 判断RLOGO唯一性
			StringBuffer _rlogos = new StringBuffer("");
			for (String rlogo : rlogos) {
				if (f_start) {
					_rlogos.append(",");
				}
				_rlogos.append("?");
				param.add(rlogo);
				f_start = true;
			}

			if (StrUtil.isNotBlank(_rlogos.toString())) {
				// 不为空时，才去判断，不然会出错
				Object _num = this.getOneFiledValue(hmLogSql, " SELECT count(1) FROM SYS_RESOURCES WHERE RLOGO IN (" + _rlogos.toString() + ") ", param);
				if (!"0".equals(String.valueOf(_num))) {
					return setFailInfo(dc, "资源RLOGO与已有数据重复，资源注入失败！");
				}
			}

			// 入库前二次验证新生成的allordidx是否存在
			if (this.insert(hmLogSql, beans.toArray(new DataBean[beans.size()])) != 1) {
				return setFailInfo(dc, "资源注入失败！");
			}
		} catch (Exception e) {
			msg = "资源注入异常！" + e.getMessage();
			log.error(msg, e);
			return setFailInfo(dc, msg);
		}

		return setSuccessInfo(dc);
	}

	/**
	 * @param dc
	 * @param appId
	 * @param array
	 * @param parentAllordidx
	 * @param isInternalCall
	 * @return
	 */
	private Set<DataBean> getBeans(DataContext dc, String appId, JSONArray array, String parentAllordidx, boolean isInternalCall) {
		Set<DataBean> beans = new HashSet<DataBean>();

		for (Object obj : array) {
			JSONObject item = (JSONObject) obj;

			String msg = "";
			String rlogo = item.containsKey("RLOGO") ? item.getString("RLOGO") : "";
			String rid = item.getString("RID");
			if (StrUtil.isBlank(rid)) {
				msg = String.format("检测到空资源ID，注入失败！");
				log.info(msg + " 资源数据：" + item.toJSONString());
				setFailInfo(dc, msg);

				return null;
			}

			if (rids.contains(rid)) {
				msg = String.format("检测到重复资源ID[%s]，注入失败！", rid);
				log.info(msg + " 资源数据：" + item.toJSONString());
				setFailInfo(dc, msg);

				return null;
			}
			rids.add(rid);

			// 判断RLOGO是否重复(RLOGO全局唯一)
			if (StrUtil.isNotBlank(rlogo) && rlogos.contains(rlogo)) {
				msg = String.format("检测到重复资源RLOGO[%s]，注入失败！", rlogo);
				log.info(msg + " 资源数据：" + item.toJSONString());
				setFailInfo(dc, msg);

				return null;
			}
			if (StrUtil.isNotBlank(rlogo)) {
				rlogos.add(rlogo);
			}

			boolean fieldsCheckSuccess = itemFieldsCheck(dc, item);
			if (!fieldsCheckSuccess) {
				return null;
			}

			DataBean bean = new DataBean("SYS_RESOURCES", "RID");
			bean.set("RID", rid);
			bean.set("TITLE", MapUtil.getStr(item, "TITLE"));
			bean.set("PARENTID", MapUtil.getStr(item, "PID"));
			bean.set("CHANNEL_RTYPE", isInternalCall ? MapUtil.getStr(item, "CRTP") : "00003");
			bean.set("VISIBLE", MapUtil.getStr(item, "VIS"));
			bean.set("ISGROUP", MapUtil.getStr(item, "GRUP"));

			String ordidx = MapUtil.getStr(item, "ORD");
			bean.set("ORDIDX", ordidx);
			String allordidx = generateAllordidx(parentAllordidx, rid);
			bean.set("ALLORDIDX", allordidx);

			if (StrUtil.isNotBlank(allordidx)) {
				int splitLength = allordidx.split("\\.").length;
				if (splitLength > 1) {
					bean.set("RLEVEL", splitLength - 1);
				}
			}

			bean.set("APP_ID", appId);
			bean.set("EXTITEM1", MapUtil.getStr(item, "EXT1"));
			bean.set("EXTITEM2", MapUtil.getStr(item, "EXT2"));

			if (isInternalCall) {
				bean.set("RLOGO", MapUtil.getStr(item, "RLOGO"));
			}

			beans.add(bean);

			JSONArray childs = item.getJSONArray("CHLD");
			if (childs != null && childs.size() > 0) {
				Set<DataBean> _beans = getBeans(dc, appId, childs, allordidx, isInternalCall);
				if (_beans == null) {
					return null;
				}
				beans.addAll(_beans);
			}
		}

		return beans;
	}

	/**
	 * 字段空值校验
	 * 
	 * @param dc
	 * @param item
	 * @return
	 */
	private boolean itemFieldsCheck(DataContext dc, JSONObject item) {
		String msg = "";
		Object value = null;
		for (String key : item.keySet()) {
			if ("EXTITEM1".equalsIgnoreCase(key) || "EXTITEM2".equalsIgnoreCase(key) || "CHANNEL_RTYPE".equalsIgnoreCase(key)) {
				continue;
			}

			value = item.get(key);
			if (value == null || ((value instanceof String) && StrUtil.isBlank((String) value))) {
				msg = String.format("检测到空字段[%s]，注入失败！", key);
				log.info(msg + " 资源数据：" + item.toJSONString());
				setFailInfo(dc, msg);

				return false;
			}
		}

		return true;
	}

	/**
	 * @param appId
	 * @return
	 */
	private String getAppNo(String appId) {
		try {
			String result = appId.replaceAll("APP", "").replaceAll("app", "");

			return Integer.parseInt(result) + "";
		} catch (Exception e) {
			return "99999";
		}
	}

	/**
	 * 获取APP下的所有allordidx列的集合
	 * 
	 * @param hmLogSql
	 * @param appId
	 * @return
	 */
	private Map<String, Object> getAppAllOrdidxs(HashMap<String, String> hmLogSql, String appId) {
		String sql = " SELECT RID, ALLORDIDX FROM SYS_RESOURCES WHERE APP_ID = ? ";

		List<Object> params = new ArrayList<Object>();
		params.add(appId);

		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> resultMap = this.getSelectList(hmLogSql, sql, params);
		for (Map<String, Object> map : resultMap) {
			String rid = MapUtil.getStr(map, "RID");
			String allordidx = MapUtil.getStr(map, "ALLORDIDX");
			if (StrUtil.isNotBlank(rid) && StrUtil.isNotBlank(allordidx)) {
				result.put(rid, allordidx);
			}
		}

		return result;
	}

	/**
	 * @param array
	 */
	private void sortByOrdidx(JSONArray array) {
		JSONObject[] _array = new JSONObject[array.size()];
		array.toArray(_array);

		Arrays.sort(_array, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				int ordidx1 = o1.get("ORD") == null ? 0 : MapUtil.getInt(o1, "ORD");
				int ordidx2 = o2.get("ORD") == null ? 0 : MapUtil.getInt(o2, "ORD");

				if (ordidx1 != ordidx2) {
					return ordidx1 - ordidx2;
				} else {
					// 根据ID排序
					return 0;
				}
			}
		});

		array.clear();

		List<JSONObject> list = new ArrayList<JSONObject>();
		for (JSONObject obj : _array) {
			list.add(obj);
		}
		array.addAll(list);

		for (JSONObject item : list) {
			JSONArray subArray = item.getJSONArray("CHLD");
			if (subArray != null && subArray.size() > 0) {
				sortByOrdidx(subArray);
			}
		}
	}

	/**
	 * 生成资源的allordidx
	 * 
	 * @param parentAllordidx
	 * @param rid
	 * @return
	 */
	private String generateAllordidx(String parentAllordidx, String rid) {
		// 尝试根据资源ID查询已有的allordidx
		String allordidx = MapUtil.getStr(all_allordidx, rid);
		if (StrUtil.isBlank(allordidx)) {
			// 生成allordidx
			int count = 1;
			while (count <= 9999) {
				allordidx = String.format("%s.%s", parentAllordidx, StrUtil.padPre(count++ + "", 4, '0'));
				if (!all_allordidx.values().contains(allordidx)) {
					break;
				}
			}

			all_allordidx.put(rid, allordidx);
		}

		return allordidx;
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "common/json";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

}
