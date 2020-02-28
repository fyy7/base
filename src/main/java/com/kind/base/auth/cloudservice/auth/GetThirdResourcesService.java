package com.kind.base.auth.cloudservice.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kind.base.core.auth.ResourceService;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 获取第三方资源数据
 * 
 * @author yanhang
 */
@Service
// @Action(requireLogin = false, action = "#get_third_resources_service", description = "获取第三方资源数据", powerCode = "", requireTransaction = false)
public class GetThirdResourcesService extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(GetThirdResourcesService.class);

	/**
	 * http://my.kind.com/auth/restful?action=get_third_resources_service&TOKEN=&APP_ID=APP90001&CRTYPE=&RID=TSTRID001&EXTITEM1=&EXTITEM2=
	 */
	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		ResourceService resourceService = SpringUtil.getBean(ResourceService.class);

		String msg = "";
		try {
			String token = dc.getRequestObject("TOKEN", "");
			String channelId = dc.getRequestObject("APP_ID", "");
			String crtype = dc.getRequestObject("CRTYPE", ""); // 资源类型
			String rid = dc.getRequestObject("RID", ""); // 资源ID
			String extitem1 = dc.getRequestObject("EXTITEM1", "");
			String extitem2 = dc.getRequestObject("EXTITEM2", "");

			List<Object> params = new ArrayList<Object>();
			StringBuffer sb = new StringBuffer(" SELECT R.RID, R.TITLE, R.PARENTID, R.CHANNEL_RTYPE, R.VISIBLE, R.ISGROUP, R.ORDIDX, R.ALLORDIDX, R.APP_ID, R.EXTITEM1, R.EXTITEM2 FROM SYS_RESOURCES R WHERE APP_ID = ? ");
			params.add(channelId);

			if (StrUtil.isNotBlank(crtype)) {
				sb.append(" AND CHANNEL_RTYPE = ? ");
				params.add(crtype);
			}

			if (StrUtil.isNotBlank(extitem1)) {
				sb.append(" AND EXTITEM1 LIKE ? ");
				// params.add("%" + extitem1 + "%");
				ConvertSqlDefault.addLikeEscapeStr(sb, extitem1, "%%", params);
			}

			if (StrUtil.isNotBlank(extitem2)) {
				sb.append(" AND EXTITEM2 LIKE ? ");
				// params.add("%" + extitem2 + "%");
				ConvertSqlDefault.addLikeEscapeStr(sb, extitem2, "%%", params);
			}

			String sql = sb.toString();
			List<Map<String, Object>> result = this.getSelectList(hmLogSql, sql, params);

			List<Map<String, Object>> _result = new ArrayList<Map<String, Object>>();

			// 查找根节点
			for (Map<String, Object> item : result) {
				if (rid.equals(MapUtil.getStr(item, "RID"))) {
					_result.add(item);

					// 查找子节点
					List<Map<String, Object>> children = getListByPid(rid, result);
					if (children.size() > 0) {
						_result.addAll(children);
					}

					break;
				}
			}

			dc.setBusiness("value", _result);
		} catch (Exception e) {
			msg = "获取第三方资源数据异常！";
			log.error(msg);

			return setFailInfo(dc, msg);
		}

		return setSuccessInfo(dc);
	}

	private List<Map<String, Object>> getListByPid(String pid, List<Map<String, Object>> src) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		if (StrUtil.isBlank(pid) || src == null) {
			return result;
		}

		for (Map<String, Object> item : src) {
			if (pid.equals(MapUtil.getStr(item, "PARENTID"))) {
				result.add(item);

				List<Map<String, Object>> children = getListByPid(MapUtil.getStr(item, "RID"), src);
				if (children.size() > 0) {
					result.addAll(children);
				}
			}
		}

		return result;
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
