package com.kind.base.demonstration;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.io.FileUtil;

/**
 * 演示页
 * 
 * @author yanhang0610
 */
@Service
@Action(requireLogin = true, action = "#demonstration_goto_main_page", description = "[演示]进入子系统菜单列表页", powerCode = "", requireTransaction = false)
public class DemonstrationGotoMainPageAction extends BaseActionService {

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String pageId = dc.getRequestString("pageId");
		dc.setBusiness("PAGEID", pageId);

		String pageCfgJsonStr = FileUtil.readUtf8String("/quick_demonstration_config/" + pageId + ".txt");
		JSONObject pageCfgJson = JSONObject.parseObject(pageCfgJsonStr);

		// 搜索栏
		JSONArray searchBarCfg = pageCfgJson.getJSONArray("searchBar");
		StringBuffer searchBarHtml = new StringBuffer();
		searchBarHtml.append("<table style='width:100%;' id='fieldset_data'>");
		for (int i = 0; i < searchBarCfg.size(); i++) {
			searchBarHtml.append("<tr><td>");

			JSONArray searchBarRowCfg = searchBarCfg.getJSONArray(i);
			for (int j = 0; j < searchBarRowCfg.size(); j++) {
				searchBarHtml.append(getSearchBarItemHtml(searchBarRowCfg.getJSONObject(j)));
			}

			searchBarHtml.append("</td></tr>");
		}
		searchBarHtml.append("</table>");
		dc.setBusiness("SEARCH_BAR_HTML", searchBarHtml.toString());

		// 表格配置
		JSONArray datagridColumnsCfg = pageCfgJson.getJSONArray("datagridColumns");
		StringBuffer datagridColumnsHtml = new StringBuffer();
		datagridColumnsHtml.append("["); // {field: "ATTACHMENT_NAME", width: 250, align: "left", halign: "center", title: "名称"}
		for (int i = 0; i < datagridColumnsCfg.size(); i++) {
			JSONObject item = datagridColumnsCfg.getJSONObject(i);
			datagridColumnsHtml.append("{field: '" + item.getString("field") + "', width: " + item.getString("width") + ", align: '" + item.getString("align") + "', halign: 'center', title: '" + item.getString("title") + "'}");

			if (i != datagridColumnsCfg.size() - 1) {
				datagridColumnsHtml.append(", ");
			}
		}
		datagridColumnsHtml.append("]");
		dc.setBusiness("DATAGRID_COLUMNS_HTML", datagridColumnsHtml.toString());

		// 表格数据
		JSONArray datagridDataCfg = pageCfgJson.getJSONArray("datagridData");
		StringBuffer datagridDataHtml = new StringBuffer();
		datagridDataHtml.append("[");
		for (int i = 0; i < datagridDataCfg.size(); i++) {
			StringBuffer itemJsonStr = new StringBuffer();
			itemJsonStr.append("{");
			JSONArray attrs = datagridDataCfg.getJSONArray(i);
			for (int j = 0; j < attrs.size(); j++) {
				JSONObject attr = attrs.getJSONObject(j);

				itemJsonStr.append(attr.getString("field")).append(": '").append(attr.getString("value")).append("'");

				if (j != attrs.size() - 1) {
					itemJsonStr.append(", ");
				}
			}
			itemJsonStr.append("}");

			datagridDataHtml.append(itemJsonStr.toString());

			if (i != datagridDataCfg.size() - 1) {
				datagridDataHtml.append(", ");
			}
		}
		datagridDataHtml.append("]");
		dc.setBusiness("DATAGRID_DATA_HTML", datagridDataHtml.toString());

		// 表格数据

		return setSuccessInfo(dc);
	}

	private String getSearchBarItemHtml(JSONObject itemCfg) {
		StringBuffer sb = new StringBuffer();

		String label = itemCfg.getString("label");
		String type = itemCfg.getString("type");
		String widthStyle = itemCfg.containsKey("width") ? (" style='width: " + itemCfg.getString("width") + ";' ") : " ";

		switch (type) {
		case "input":
			sb.append(label);
			sb.append("<input type='text' " + widthStyle + "/>");

			break;
		case "select":
			sb.append(label);
			sb.append("<select " + widthStyle + ">");

			JSONArray options = itemCfg.getJSONArray("options");
			sb.append("<option>--</option>");
			for (int i = 0; i < options.size(); i++) {
				sb.append("<option>" + options.getString(i) + "</option>");
			}
			sb.append("<select />");

			break;
		case "radio":
			JSONArray items = itemCfg.getJSONArray("items");
			for (int i = 0; i < items.size(); i++) {
				sb.append("<input type='radio' name='search_bar_radio'/><span style='color: red;font-weight: bolder;'>" + items.getString(i) + "</span>");
			}

			break;
		case "datetimepicker":
			sb.append(label);
			sb.append("<input type='text' " + widthStyle + " class='Wdate' onfocus='WdatePicker({dateFmt:\"yyyy-MM-dd\",isShowClear:true,readOnly:true})' />");

			break;
		case "text":
			sb.append(itemCfg.getString("value"));

			break;
		case "clearBtn":
			sb.append("<a href='#' class='easyui-linkbutton btn_query' data-options=\"iconCls:'icon-cancel'\" style='background-color: #E84B6E; border-color: #E84B6E'>清空</a>");

			break;
		case "searchBtn":
			sb.append("<a href='#' class='easyui-linkbutton btn_query' data-options=\"iconCls:'icon-search'\">查询</a>");

			break;
		default:

			break;
		}

		return sb.toString();
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "demonstration/demonstration_main";
	}
}
