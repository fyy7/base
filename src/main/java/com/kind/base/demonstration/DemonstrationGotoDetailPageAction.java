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
@Action(requireLogin = true, action = "#demonstration_goto_detail_page", description = "[演示]进入子系统菜单详情页", powerCode = "", requireTransaction = false)
public class DemonstrationGotoDetailPageAction extends BaseActionService {

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String pageId = dc.getRequestString("pageId");

		String pageCfgJsonStr = FileUtil.readUtf8String("/quick_demonstration_config/" + pageId + ".txt");
		JSONObject pageCfgJson = JSONObject.parseObject(pageCfgJsonStr);

		JSONArray detailFormCfg = pageCfgJson.getJSONArray("detailForm");
		StringBuffer detailFormHtml = new StringBuffer();
		detailFormHtml.append("<table class='dataintable form_bod'>");
		for (int i = 0; i < detailFormCfg.size(); i++) {
			detailFormHtml.append("<tr>");

			JSONArray formItemsCfg = detailFormCfg.getJSONArray(i);
			for (int j = 0; j < formItemsCfg.size(); j++) {
				JSONObject formItem = formItemsCfg.getJSONObject(j);
				detailFormHtml.append(getFormItemHtml(formItem));
			}

			detailFormHtml.append("</tr>");
		}
		detailFormHtml.append("</table>");
		dc.setBusiness("FORM_ITEMS_HTML", detailFormHtml.toString());

		return setSuccessInfo(dc);
	}

	private String getFormItemHtml(JSONObject itemCfg) {
		StringBuffer sb = new StringBuffer();

		String label = itemCfg.getString("label");
		String type = itemCfg.getString("type");
		String widthStyle = itemCfg.containsKey("width") ? (" style='width: " + itemCfg.getString("width") + ";' ") : " style='width: 92%' ";
		String colspan = itemCfg.containsKey("colspan") ? (" colspan='" + itemCfg.getString("colspan") + "' ") : " ";
		String required = itemCfg.containsKey("required") ? (" required='" + itemCfg.getString("required") + "' ") : " ";

		switch (type) {
		case "input":
			sb.append("<td class='infotitle'>" + label + "</td>");
			sb.append("<td " + colspan + ">");

			sb.append("<input type='text' " + widthStyle + required + " />");

			sb.append("</td>");

			break;
		case "select":
			sb.append("<td class='infotitle'>" + label + "</td>");
			sb.append("<td " + colspan + ">");

			sb.append("<select " + widthStyle + required + ">");
			JSONArray options = itemCfg.getJSONArray("options");
			sb.append("<option>--</option>");
			for (int i = 0; i < options.size(); i++) {
				sb.append("<option>" + options.getString(i) + "</option>");
			}
			sb.append("<select />");

			sb.append("</td>");

			break;
		case "radio":
			sb.append("<td class='infotitle'>" + label + "</td>");
			sb.append("<td " + colspan + ">");

			JSONArray items = itemCfg.getJSONArray("items");
			sb.append("<div " + widthStyle + ">");
			for (int i = 0; i < items.size(); i++) {
				sb.append("<input type='radio' name='form_radio_" + label + "'/><span style='font-weight: bolder;'>" + items.getString(i) + "</span>");
			}
			sb.append("</div>");

			sb.append("</td>");

			break;
		case "datetimepicker":
			sb.append("<td class='infotitle'>" + label + "</td>");
			sb.append("<td " + colspan + ">");

			sb.append("<input type='text' " + widthStyle + required + " class='Wdate' onfocus='WdatePicker({dateFmt:\"yyyy-MM-dd\",isShowClear:true,readOnly:true})' />");

			sb.append("</td>");

			break;
		case "text":
			sb.append("<td class='infotitle'>" + label + "</td>");
			sb.append("<td " + colspan + ">");

			sb.append(itemCfg.getString("value"));

			sb.append("</td>");

			break;
		case "textarea":
			sb.append("<td class='infotitle'>" + label + "</td>");
			sb.append("<td " + colspan + ">");

			String width = itemCfg.containsKey("width") ? (" width: " + itemCfg.getString("width") + "; ") : " width: 94%; ";
			String height = itemCfg.containsKey("height") ? (" height: " + itemCfg.getString("height") + "; ") : " height: 50px; ";

			sb.append("<textarea style='" + width + height + "'></textarea>");

			sb.append("</td>");

			break;
		default:

			break;
		}

		return sb.toString();
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "demonstration/demonstration_detail";
	}

}
