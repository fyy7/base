/*
 * 获取当前项目路径
 */
function getProjectPath(){
	var pathName=window.document.location.pathname;
	return pathName.substring(0,pathName.substr(1).indexOf('/')+2);
}

/*
 * 处理表格按钮中重复的坚线
 */
function dataGridButtonFilter(arr_old){
	var arr_new=[];
	var i_start=0;
  	for(var i=0;i<arr_old.length;i++){
  		if(arr_old[i]!='-' ){
  			if(i_start!=0){
  				arr_new[i_start++]='-'
  			}
  			arr_new[i_start++]=arr_old[i];
  		}
  	}
  	return arr_new;
}

/*
 * // 对Date的扩展，将 Date 转化为指定格式的String //月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符， //年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字) //例子： //(new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423 //(new Date()).Format("yyyy-M-d h:m:s.S") ==> 2006-7-2 8:9:4.18
 */
Date.prototype.Format = function (fmt) { // author: meizz
 var o = {
     "M+": this.getMonth() + 1, // 月份
     "d+": this.getDate(), // 日
     "h+": this.getHours(), // 小时
     "m+": this.getMinutes(), // 分
     "s+": this.getSeconds(), // 秒
     "q+": Math.floor((this.getMonth() + 3) / 3), // 季度
     "S": this.getMilliseconds() // 毫秒
 };
 if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
 for (var k in o)
 if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
 return fmt;
}
/*
 * 获取父窗口的窗口
 */
function getParentWindow2() {
	var g_win_top = window.top;
	var currTab = g_win_top.$('#tabs').tabs('getSelected');
	var child_iframe_id = "";

	if (g_win_top.g_openModelWinNum == 1) {
		child_iframe_id = g_win_top.$(currTab.panel('options').content).attr('id');
	} else if (g_win_top.g_openModelWinNum > 1) {
		child_iframe_id = "g_win_iframe" + (g_win_top.g_openModelWinNum - 1);
	}

	// 例：var
	// orgids=$("input[name='SCOPE_ORGIDS']",getParentWindow().document).val();

	// 如果用jquery选择器获得iframe，需要加一个【0】；
	return window.top.$("#" + child_iframe_id)[0].contentWindow;
	// getJsonData();
}

/**
 * 获取上一窗口的parent,可执行父窗口中的相关函数， 例getParentWindow().父窗口函数()
 * 
 * 如果要取得iframe中对象，可以用 getParentWindow().$("#dd").val()
 * 
 * @returns
 */
function getParentWindow() {
	var g_c_win_top = window.top;
	var currTab = g_c_win_top.$('#tabs').tabs('getSelected');
	var child_iframe_id = g_c_win_top.$(currTab.panel('options').content).attr('id');

	if (window.top.g_openModelWinNum > 1) {
		return window.top.$("#g_win_iframe" + (window.top.g_openModelWinNum - 1))[0].contentWindow;
	}

	var w_c_iframe = window.top.$("#" + child_iframe_id)[0].contentWindow;
	if (w_c_iframe.g_c_openModelWinNum > 1) {
		return window.top.$("#g_c_win_iframe" + (w_c_iframe.g_c_openModelWinNum - 1))[0].contentWindow;
	}

	return w_c_iframe;
}

/**
 * 打开遮罩层窗口(最顶层)
 * 
 * @param stitle 标题
 * @param surl 地址
 * @param sheight 高度
 * @param swidth 宽度
 * @param execReturnFunc 回调函数
 */
function openWindow(stitle, surl, sheight, swidth, execReturnFunc) {
	var w_c_iframe = getTabsCurrentWindow();
	if (w_c_iframe.g_c_openModelWinNum > 0) {
		openWindow2(stitle, surl, sheight, swidth, execReturnFunc);
		return;
	}
	
	surl=getProjectPath()+surl;
	
	var g_win_top = window.top;
	try{
		var currTab = g_win_top.$('#tabs').tabs('getSelected');
		var child_iframe_id = "";
	
		g_win_top.g_jsonData = {};
		g_win_top.g_openModelWinNum++;
	
		if (g_win_top.g_openModelWinNum == 1) {
			child_iframe_id = g_win_top.$(currTab.panel('options').content).attr('id');
		} else if (g_win_top.g_openModelWinNum > 1) {
			child_iframe_id = "g_win_iframe" + (g_win_top.g_openModelWinNum - 1);
		}
	}catch(e){
		
	}

	// 动态增加
	var t_win_str = "<div id='g_win@num' class='easyui-window' data-options='closed:true' ><iframe src='about:blank' style='height:100%;width:100%;display:block;' frameborder='0' id='g_win_iframe@num' name='g_win_iframe@num' scrolling='no'></iframe></div>";
	var re = /@num/g;
	t_win_str = t_win_str.replace(re, g_win_top.g_openModelWinNum);
	g_win_top.$('body').append(t_win_str);
	// $.parser.parse($("#id_div_g_windows"));//渲染

	// 标题
	stitle = "<span>" + stitle + "</span>";

	if ((swidth + "").indexOf('%') > -1) {
		swidth = (parseInt($(g_win_top.window).width()) - 20) * parseInt(swidth.replace('%', '')) / 100;
	}
	if ((sheight + "").indexOf('%') > -1) {
		sheight = (parseInt($(g_win_top.window).height()) - 20) * parseInt(sheight.replace('%', '')) / 100;
	}

	// 超过窗口最大值的，取窗口值
	swidth = parseInt(swidth) > (parseInt($(g_win_top.window).width()) - 20) ? ($(g_win_top.window).width() - 20) : swidth;
	sheight = parseInt(sheight) > (parseInt($(g_win_top.window).height()) - 20) ? ($(g_win_top.window).height() - 20) : sheight;

	var win = g_win_top.$("#g_win" + g_win_top.g_openModelWinNum).window({
		title : stitle,
		width : swidth,
		height : sheight,
		onClose : function() {
			// 赋空白页，避免出现ie浏览器下出现iframe中input失去焦点无法输入的情况
			g_win_top.$("#g_win_iframe" + g_win_top.g_openModelWinNum).attr("src", "about:blank");

			g_win_top.$("#g_win_iframe" + g_win_top.g_openModelWinNum).remove();
			// 窗口数减去1
			g_win_top.g_openModelWinNum--;

			// 摧毁窗口
			g_win_top.$("#g_win" + (g_win_top.g_openModelWinNum + 1)).window('destroy');

			if (execReturnFunc && execReturnFunc != "") {
				// 执行上一个窗口中的回调函数
				// 2017年6月13日 首页用到openwindow是没有子iframe所以child_iframe_id为null，导致获取不到正确的iframe页面，改为为空时返回顶层
				// 以下一行是原有代码
				// var str = "window.top.$(\"#" + child_iframe_id + "\")[0].contentWindow." + execReturnFunc + "(g_win_top.g_jsonData)";
				var str = "";
				if(child_iframe_id){
					str = "window.top.$(\"#" + child_iframe_id + "\")[0].contentWindow." + execReturnFunc + "(g_win_top.g_jsonData)";
				}else{
					str = "window.top." + execReturnFunc + "(g_win_top.g_jsonData)";
				}
				
				try {
					eval(str);
				} catch (err) {
					alert("回调函数【" + execReturnFunc + "】执行失败！\r\n错误信息：" + err.message);
				}
			}
		}
	});
	win.window('center');
	win.window('open');

	// 拆解url，改为form的post提交
	var jsonParams = getUrlParams(surl);
	surl = surl.split("?")[0];
	
	var method="post";
	if(surl.indexOf(".html")>-1){
		method="get";
	}
	
	var formHtml = "<form id='g_win_iframe_form" + g_win_top.g_openModelWinNum + "' action='" + surl + "' method='"+method+"' target='g_win_iframe" + g_win_top.g_openModelWinNum + "'>";
	$.each(jsonParams, function(item, value) {
		formHtml += "<input name='" + item + "' type='hidden' value='" + value + "'/>"
	});
	formHtml += "</form>";
	// alert(formHtml);

	g_win_top.$('body').append(formHtml);
	g_win_top.$("#g_win_iframe_form" + g_win_top.g_openModelWinNum).submit();
	g_win_top.$("#g_win_iframe_form" + g_win_top.g_openModelWinNum).remove();

	// 赋url地址
	// g_win_top.$("#g_win_iframe" + g_win_top.g_openModelWinNum).attr("src", surl);
}

/**
 * 关闭遮罩层窗口(最顶层)
 */
function closeWindow() {
	var w_c_iframe = getTabsCurrentWindow();
	if (w_c_iframe.g_c_openModelWinNum > 0) {
		// alert("closeWindow");
		closeWindow2();
		return;
	}
	window.top.$("#g_win" + window.top.g_openModelWinNum).window('close');
}

/**
 * 设置全局的json对象值
 * 
 * @param key 键值
 * @param val 值
 */
function setGlobalParam(key, val) {
	var w_c_iframe = getTabsCurrentWindow();
	var str = "";
	if (w_c_iframe.g_c_openModelWinNum > 0) {
		str = "w_c_iframe.g_c_jsonData." + key + "=val";
	} else {
		str = "window.top.g_jsonData." + key + "=val";
	}
	eval(str);
}

/**
 * 获取全局json对象key值
 * 
 * @param key 键值
 * @returns {String}
 */
function getGlobalParam(key) {
	var w_c_iframe = getTabsCurrentWindow();
	var str = "", k_value = "";
	if (w_c_iframe.g_c_openModelWinNum > 0) {
		str = "k_value=w_c_iframe.g_c_jsonData." + key;
		eval(str);
	} else {
		str = "k_value=window.top.g_jsonData." + key;
		eval(str);
	}
	if (w_c_iframe.g_c_openModelWinNum == 0) {
		if (k_value == null || k_value == "") {
			str = "k_value=w_c_iframe.g_c_jsonData." + key;
			eval(str);
		}
	}

	return k_value;
}

/**
 * 获取当前tabs中的window对象
 * 
 * @returns
 */
function getTabsCurrentWindow() {
	var g_c_win_top = window.top;
	var currTab ;
	try{
		currTab =g_c_win_top.$('#tabs').tabs('getSelected')
	}catch(e){
		return g_c_win_top;
	}

	
	var child_iframe_id = g_c_win_top.$(currTab.panel('options').content).attr('id');
	
	// 2017年6月13日 首页用到openwindow是没有子iframe所以child_iframe_id为null，导致获取不到正确的iframe页面，改为为空时返回顶层
	// 以下两行是原有代码
	// var w_c_iframe = window.top.$("#" + child_iframe_id)[0].contentWindow;
	// return w_c_iframe;
	
	if(window.top.$("#" + child_iframe_id)[0]){
		var w_c_iframe = window.top.$("#" + child_iframe_id)[0].contentWindow;
		return w_c_iframe;
	}else{
		return g_c_win_top;
	}
}

/**
 * 当前窗口模态窗口层数
 */
var g_c_openModelWinNum = 0;

/**
 * 当前窗口模态窗口的全局数据容器
 */
var g_c_jsonData = {};

/**
 * 打开遮罩层窗口(当前窗口的)
 * 
 * @param stitle 标题
 * @param surl 地址
 * @param sheight 高度
 * @param swidth 宽度
 * @param execReturnFunc 回调函数
 */
function openWindow2(stitle, surl, sheight, swidth, execReturnFunc) {
	if (window.top.g_openModelWinNum > 0) {
		openWindow(stitle, surl, sheight, swidth, execReturnFunc);
		return;
	}
	var w_c_iframe = getTabsCurrentWindow();
	
	surl=getProjectPath()+surl;

	w_c_iframe.g_c_jsonData = {};
	w_c_iframe.g_c_openModelWinNum++;

	// 动态增加
	var t_win_str = "<div id='g_c_win@num' class='easyui-window' data-options='closed:true' ><iframe src='' style='height:100%;width:100%;display:block;' frameborder='0' id='g_c_win_iframe@num' name='g_c_win_iframe@num' scrolling='no'></iframe></div>";
	var re = /@num/g;
	t_win_str = t_win_str.replace(re, w_c_iframe.g_c_openModelWinNum);

	w_c_iframe.$('body').append(t_win_str);
	// $.parser.parse(w_c_iframe.document.body);//渲染

	// 标题
	stitle = "<span title='" + stitle + "[" + w_c_iframe.g_c_openModelWinNum + "]'>" + stitle + "</span>";

	if ((swidth + "").indexOf('%') > -1) {
		swidth = (parseInt($(w_c_iframe).width()) - 20) * parseInt(swidth.replace('%', '')) / 100;
	}
	if ((sheight + "").indexOf('%') > -1) {
		sheight = (parseInt($(w_c_iframe).height()) - 20) * parseInt(sheight.replace('%', '')) / 100;
	}

	// 超过窗口最大值的，取窗口值
	swidth = parseInt(swidth) > (parseInt($(w_c_iframe).width()) - 20) ? ($(w_c_iframe).width() - 20) : swidth;
	sheight = parseInt(sheight) > (parseInt($(w_c_iframe).height()) - 20) ? ($(w_c_iframe).height() - 20) : sheight;

	var win = w_c_iframe.$("#g_c_win" + w_c_iframe.g_c_openModelWinNum).window({
		title : stitle,
		width : swidth,
		height : sheight,
		onClose : function() {// onBeforeClose
			// 赋空白页，避免出现ie浏览器下出现iframe中input失去焦点无法输入的情况
			w_c_iframe.$("#g_c_win_iframe" + w_c_iframe.g_c_openModelWinNum).attr("src", "about:blank");

			w_c_iframe.$("#g_c_win_iframe" + w_c_iframe.g_c_openModelWinNum).remove();

			// 窗口数减去1
			w_c_iframe.g_c_openModelWinNum--;

			// 摧毁窗口
			w_c_iframe.$("#g_c_win" + (w_c_iframe.g_c_openModelWinNum + 1)).window('destroy');

			if (execReturnFunc && execReturnFunc != "") {
				// 执行上一个窗口中的回调函数
				var str = "w_c_iframe.";
				if (w_c_iframe.g_c_openModelWinNum > 0) {
					str += "$(\"#g_c_win_iframe" + w_c_iframe.g_c_openModelWinNum + "\")[0].contentWindow.";
				}
				str += execReturnFunc + "(w_c_iframe.g_c_jsonData)";
				try {
					eval(str);
				} catch (err) {
					alert("回调函数【" + execReturnFunc + "】执行失败！\r\n错误信息：" + err.message);
				}
			}
		}
	});
	win.window('center');
	win.window('open');

	// 拆解url，改为form的post提交
	var jsonParams = getUrlParams(surl);
	surl = surl.split("?")[0];
	
	var method="post";
	if(surl.indexOf(".html")>-1){
		method="get";
	}
	
	var formHtml = "<form id='g_win_iframe_form" + w_c_iframe.g_c_openModelWinNum + "' action='" + surl + "' method='"+method+"' target='g_win_iframe" + w_c_iframe.g_c_openModelWinNum + "'>";
	$.each(jsonParams, function(item, value) {
		formHtml += "<input name='" + item + "' type='hidden' value='" + value + "'/>"
	});
	formHtml += "</form>";
	// alert(formHtml);

	w_c_iframe.$('body').append(formHtml);
	w_c_iframe.$("#g_win_iframe_form" + w_c_iframe.g_c_openModelWinNum).submit();
	w_c_iframe.$("#g_win_iframe_form" + w_c_iframe.g_c_openModelWinNum).remove();

	// 赋url地址
	// w_c_iframe.$("#g_c_win_iframe" + w_c_iframe.g_c_openModelWinNum).attr("src", surl);
}

/**
 * 关闭遮罩层窗口(当前窗口的)
 */
function closeWindow2() {
	if (window.top.g_openModelWinNum > 0) {
		closeWindow();
		return;
	}
	var w_c_iframe = getTabsCurrentWindow();
	w_c_iframe.$("#g_c_win" + w_c_iframe.g_c_openModelWinNum).window('close');
}

/**
 * 设置窗口居中
 */
function setWindowCenter() {
	var w_c_iframe = getTabsCurrentWindow();
	for (var i = 1; i <= w_c_iframe.g_c_openModelWinNum; i++) {
		if (!(w_c_iframe.$("#g_c_win" + i).window('options').closed)) {
			// 有显示的才居中
			w_c_iframe.$("#g_c_win" + i).window('center');
		}
	}
}

// 重置按钮（仅支持页面ID为fieldset_data的DIV内的查询条件）
function reset() {
	$("#fieldset_data input:visible").val("");
	$("#fieldset_data select:visible option[value='']").attr("selected", true);
}

/**
 * 执行查询(在初始化时执行查询请直接在初始化中设置url的值)
 * 
 * @param dgId 页面dg的id
 * @param url 要执行的url
 * @param formId 查询条件所在的formId
 */
function queryDataFromDg(dgId, url, formId) {
	var opts = $("#" + dgId).datagrid("options");
	if (null == opts.url || "" == opts.url) {
		opts.url = url;
	}
	$("#" + dgId).datagrid("load", $("#" + formId).serializeObject());
}

/**
 * 获取表单id=formid的所有input select textarea值组成json串，用于查询时用
 * 
 * @param formid
 * @returns {String}
 */
function getFormFieldJsonData(formid) {
	var url = "{";
	$("#" + formid + " input").each(function() {
				if ($(this).attr("type") == "button" || $(this).attr("type") == "submit") {
					// 不需操作
				} else if ($(this).attr("type") == 'radio' || $(this).attr("type") == 'checkbox') {
					if ($(this).attr("checked") == "checked") {
						if ($(this).attr("name")) {
							url = url +  $(this).attr("name") + ":\""+ $(this).val()+"\",";
						}
					}
				} else {
					if ($(this).attr("name")) {
						url = url +  $(this).attr("name") + ":\""+ $(this).val()+"\",";
					}
				}
			});
	$("#" + formid + " select").each(function() {
		if ($(this).attr("name")) {
			url = url +  $(this).attr("name") + ":\""+ $(this).val()+"\",";
		}
	});

	$("#" + formid + " textarea").each(function() {
		if ($(this).attr("name")) {
			url = url +  $(this).attr("name") + ":\""+ $(this).val()+"\",";
		}
	});
	return url.substr(0,url.length-1)+"}";
}

/**
 * 设置ID内元素可读
 * 
 * @param id
 */
function setOnlyRead(id,exceptionname){
	if(!exceptionname){
		exceptionname="";
	}
	$("#" + id + " input").each(function() {
		if(exceptionname.indexOf(","+$(this).attr("name")+",")>-1){
			return true;
		}
		if ($(this).attr("type") == "button" || $(this).attr("type") == "submit" || $(this).attr("type") == "file") { // 按钮
			$(this).attr("disabled","disabled");
		} else if ($(this).attr("type") == 'radio' || $(this).attr("type") == 'checkbox') { // 单选，复选
			$(this).attr("disabled","disabled");
		}else if($(this).attr("class") == "Wdate"){ // 时间
			$(this).attr("disabled","disabled");
		} else {
			$(this).attr("readonly","readonly");
		}
		
	});
	$("#" + id + " select").each(function() {
		if(exceptionname.indexOf(","+$(this).attr("name")+",")>-1){
			return true;
		}
		$(this).attr("disabled","disabled");
	});
	$("#" + id + " textarea").each(function() {
		if(exceptionname.indexOf(","+$(this).attr("name")+",")>-1){
			return true;
		}
		$(this).attr("readonly","readonly");
	});
}


/**
 * select下拉框初始化（根据标签属性'codesrcsql'配置的SQL）
 */
function initSelect(){
	if($("select[option-type]").length>0){
		var arr = new Array();
		var action;
		$("select[option-type]").each(function(index){
			if(!action&&$(this).attr("option-action")){
				action=	$(this).attr("option-action")
			}
			var attr = {};
			attr.name=$(this).attr("name");
			attr.optiontype=$(this).attr("option-type");
			if(attr.name && attr.optiontype){
				arr.push(attr);
			}
		});
		if(action){
			postAjax("restful?action="+action, "param="+JSON.stringify(arr), "initSelectCallback");
		}
	
	}else{
		$("select[select-value]").each(function(){
			if($(this).attr("select-value")){
				$(this).find("option[value='"+$(this).attr("select-value")+"']").attr("selected",true);
			}
			if(!($(this).attr("need-exec-change")=="false")){
				$(this).attr("need-exec-change","true");
			}
		});
		$("select[need-exec-change='true']").change().attr("need-exec-change","false");
	}
	
}
function initSelectCallback(jsondata){
	if(jsondata.g_result=='1'){
        $.each(jsondata.selects, function(index1, value){
        	var _optionsHtml = "";
        	var _optionsJson = value.options;
        	$.each(_optionsJson, function(index2, option){
        		var _optionValue, _optionText;
        		var _valueIndex = 0;
        		$.each(option, function(key, value){
        			if(_valueIndex==0){
        				_optionValue = value;
        			}
        			if(_valueIndex==1){
        				_optionText = value;
        			}
        			_valueIndex++;
        		});
        		_optionsHtml += "<option value='"+_optionValue+"'>"+_optionText+"</option>";
        	});
        	$("select[name='"+value.name+"']").append(_optionsHtml);
        });
    }
	$("select[select-value]").each(function(){
		$(this).val($(this).attr("select-value"));
		if(!($(this).attr("need-exec-change")=="false")){
			$(this).attr("need-exec-change","true");
		}
	});
	$("select[need-exec-change='true']").change().attr("need-exec-change","false");
}
$(function(){
	initSelect();
});

// 初始化select下拉框。id：待初始化的select控件ID，data格式：[{key: "", value: ""}, ...]
function initSelectOption(id, data) {
	for (var idx in data) {
		var item = data[idx];
		$("#" + id).append("<option value='" + item.key + "'>" + item.value + "</option>");
	}
}

// 获取字典值，data格式：[{key: "", value: ""}, ...]
function getDicValue(data, key) {
	for (var idx in data) {
		var item = data[idx];
		if (item.key == key) {
			return item.value;
		}
	}
}

function getFileSuffix(fileElementId) {
	if (fileElementId == undefined) {
		return "";
	}
	
	var value = $("#" + fileElementId).val();
	if (value.indexOf(".") > -1) {
	    var suffix = value.substring(value.lastIndexOf(".") + 1, value.length).toLowerCase();
	    return suffix;
	}
	
	return "";
}

function getFileName(fileElementId) {
	if (fileElementId == undefined) {
		return "";
	}
	
	var value = $("#" + fileElementId).val();
	if (value.indexOf(".") > -1) {
	    var name = value.substring(value.lastIndexOf("\\") + 1, value.lastIndexOf("."));
	    return name;
	}
	
	return "";
}

/**
 * 文件上传到附件服务器[文件控件name属性约定为"filesToUpload"]
 * 
 * @param targetUrl 目标地址
 * @param fileElementId 文件上传控件ID
 * @param fileSuffix 文件后缀，可为多个（以半角逗号隔开，不可包含空格，如："jpeg,jpg,png,gif"）
 * @param sizeLimit 大小限制，单位：B，为0则不限制
 * @param callback 上传成功回调函数，入参：data(json对象)，格式：{"g_result":1,"g_message":"上传文件完成","files":[{"uid":"add03cda312940d5a50efb3c5fc345d8"}]}
 */
function uploadFile(targetUrl, fileElementId, fileSuffix, sizeLimit, callback) {
	var value = $("#" + fileElementId).val();
	if (value == undefined || value == "") {
		showMsg("请选择文件");
		return false;
	}
	
	showWaitMsg();
	if (value.indexOf(".") > -1) {
	    var suffix = value.substring(value.lastIndexOf(".") + 1, value.length).toLowerCase();
	    if(fileSuffix != "" && fileSuffix.indexOf(suffix) < 0){
	    	hideWaitMsg();
	    	showMsg("文件格式不正确");
	        return false;
	    }
	}
    
    if (sizeLimit > 0) {
    	// var fileSize = $("#" + fileElementId)[0].files[0].size;  // 单位：B
        var fileSize = $("#" + fileElementId)[0].files ? $("#" + fileElementId)[0].files[0].size : $("#" + fileElementId)[0].size;
        if(fileSize > sizeLimit){
        	showMsg("文件大小[" + fileSize + "B, " + fileSize / 1024 + "KB]不能超过[" + sizeLimit + "B, " + sizeLimit / 1024 + "KB]");  
            hideWaitMsg();
            return false;
        }
    }

    $.ajaxFileUpload({
        url: targetUrl,
        secureuri: false,
        fileElementId: fileElementId,  // 文件上传的id属性
        dataType: "json",
        success: function(data, status) {
        	hideWaitMsg();
        	if (callback != undefined) {
        		callback(data);
        	}
        },
        error: function (data, status, e) {
        	showMsg("服务器繁忙，请稍后再试！");
        }
    });
}

// 绑定鼠标滑过悬浮放大显示图片事件，className为加载在需要绑定事件的img元素的class名称
function bindEnlargeEvent(className) {
	if ($("#enlargeImage").length == 0) {
		$("<div id='enlargeImage' style='position: absolute; display: none; z-index: 999; border: 0px solid #f4f4f4;'></div>").appendTo("body");;
	}
	
	$("." + className).mousemove(function(event) {
		event = event || window.event;
		var enlargeWidth = 200;
		var enlargeHeight = 200;
		var enlargeImage = $("#enlargeImage");
		enlargeImage.css("display", "block");
		enlargeImage.html("<img src='" + event.target.src + "' style='width: 200px; height: 200px;'/>");
		
		enlargeImage.css("top", document.body.scrollTop + event.clientY + 10 + "px");
		var left = (document.body.clientWidth - (document.body.scrollLeft + event.clientX)) > enlargeWidth ? (document.body.scrollLeft + event.clientX) : (document.body.scrollLeft + event.clientX - enlargeWidth - 10);
		enlargeImage.css("left", left + "px");
	});
	
	$("." + className).mouseout(function(event) {
		var enlargeImage = $("#enlargeImage");
		enlargeImage.html("");
		enlargeImage.css("display", "none");
	});
}

function getCookie(cookieName) {
	if (document.cookie.length > 0) {
		var c_start = document.cookie.indexOf(cookieName + "=");
		if (c_start != -1) {
			c_start = c_start + cookieName.length + 1;
			var c_end = document.cookie.indexOf(";", c_start);
			if (c_end == -1)
				c_end = document.cookie.length;

			return unescape(document.cookie.substring(c_start, c_end));
		}
	}

	return "";
}

/**
 * 检测cookie值变化事件
 * 
 * @param checkCookieName 待检测的cookie名称
 * @param callback 值变化时的回调函数，入参：旧值、新值
 * @param checkInterval 检测间隔，单位[毫秒]，不传默认10秒检测一次
 * @returns
 */
function onCookieChange(checkCookieName, callback, checkInterval) {
	var oldCookieValue = getCookie(checkCookieName);
		
	var varName = "_cookie_" + checkCookieName + "_" + new Date().getTime();
	window[varName] = oldCookieValue;
	
	if (checkInterval == undefined || checkInterval <= 0) {
		checkInterval = 10000;
	}

	var isRunning = false;
	setInterval(function () {
		if (isRunning) {
			return;
		}
		isRunning = true;
		
		var newCookieValue = getCookie(checkCookieName);
		if (window[varName] != newCookieValue) {
			if (callback) {
				callback(oldCookieValue, newCookieValue);
			}
		}
		
		isRunning = false;
	}, checkInterval);
}

// 检测登录状态。使用一个cookie值记录登录状态，通过判断该cookie是否发生变化来指示登录状态是否变化
function checkLoginState(loginStateCookieName, checkInterval) {
	var isRunning = false;
	onCookieChange(loginStateCookieName, function (oldCookieValue, newCookieValue) {
		if (isRunning) {
			return;
		}
		isRunning = true;
		
		// 强制刷新处理，避免取消用户不对的问题
		window.top.$.messager.alert("信息","登录用户状态已改变，页面需要刷新!","warning",
           function(){
			location.reload();
          }
		 );
		
		// window.top.$.messager.confirm('确认', "登录状态已改变，是否刷新页面？", function(r) {
		// if (r) {
		// //isRunning = false;
		// location.reload();
		// }
		// });
	}, checkInterval);
}

/**
 * 转换对象属性名称的大小写
 * 
 * @param obj 对象
 * @param keyCase 0:小写，1:大写
 */
function convertObjectFieldKeyCase(obj, keyCase) {
	var _obj = {};
	
	jQuery.each(obj, function(key, val) {
		var _key = key;
		if (keyCase == "0") {
			_key = key.toLowerCase();
		} else if (keyCase == "1") {
			_key = key.toUpperCase();
		}
		
		_obj[_key] = val;
	});
	
	return _obj;
}

/**
 * 转换对象属性名称的大小写
 * 
 * @param objArray 对象数组
 * @param keyCase 0:小写，1:大写
 */
function convertArrayObjectFieldKeyCase(objArray, keyCase) {
	var result = new Array();
	
	for (idx in objArray) {
		result.push(convertObjectFieldKeyCase(objArray[idx], keyCase));
	}
	
	return result;
}


/**
 * 限制输入n位小数
* @param obj	限制对象
 * @param num 	小数点位数，0表示只能输入正整数
*/
function checkfloatnum(obj,num){
	if(num>0){
		obj.value = obj.value.replace(/[^\d.]/g,""); //清除"数字"和"."以外的字符
		obj.value = obj.value.replace(/^\./g,""); //验证第一个字符是数字
		obj.value = obj.value.replace(/\.{2,}/g,"."); //只保留第一个, 清除多余的
		obj.value = obj.value.replace(".","$#$").replace(/\./g,"").replace("$#$",".");
		var sReg="^(\\-)*(\\d+)\\.(";
		for(i=1;i<=num;i++){
		   sReg=sReg+"\\d";
		}
		sReg=sReg+").*$";
		var reg = new RegExp(sReg, "");
		obj.value = obj.value.replace(reg,'$1$2.$3'); //只能输入N个小数
	}else{
		obj.value=obj.value.replace(/[^\d]/g,'')
	}
}

/**
 * 层级化数据（根据allordidx处理），在value上添加层级缩进，实现效果如：
 * 
 * 一级节点
 *   |-二级节点
 *   |-二级节点
 *   ...
 *   
 * @param dicData		含allordidx层级机构字段的对象数组，格式可为[{"value": "", "key": "", "allordidx": ""}, ...]
 * @param prefixToDel	allordidx字段需删除的前缀
 * 返回格式：[{"value": "", "key": "", "allordidx": ""}, ...]
 */
function doLevelDicByAllordidx(dicData, prefixToDel) {
	var resultDic = [];
	
	$.each(dicData, function (idx, item) {
		if (item) {
			if (!item.key && item.KEY) {
				item.key = item.KEY;
			}
			
			if (!item.value && item.VALUE) {
				item.value = item.VALUE;
			}
			
			if (!item.allordidx && item.ALLORDIDX) {
				item.allordidx = item.ALLORDIDX;
			}
		}
	});
	
	$.each(dicData, function (idx, item) {
		if (item && item.allordidx) {
			item.allordidx = item.allordidx.replace(prefixToDel, "");
		}
	});
	
	_doLevelDicByAllordidx(dicData, null, 1, resultDic);
	
	return resultDic;
}

/*
 * 层级化数据（根据allordidx处理）
 * @param dicData			含allordidx层级机构字段的对象数组，格式可为[{"value": "", "key": "", "allordidx": ""}, ...]
 * @param parentAllordidx	父级allordidx，若为第一级节点则该字段需设置为null
 * @param level				层级,从1开始,与allordidx.split('.')的个数映射
 * @param resultDic			结果存放处，请传入一个array对象
 * @return	无，结果存放在resultDic里
 */
function _doLevelDicByAllordidx(dicData, parentAllordidx, level, resultDic) {
	$.each(dicData, function (idx, item) {
		if (item && item.allordidx) {
			var allordidxArray = item.allordidx.split(".");
			if (allordidxArray.length == level) {  // 过滤层级
				if (parentAllordidx == null) {
					resultDic.push({"value": item.value, "key": item.key, "allordidx": item.allordidx});
					
					dicData[idx] = null;  // 标记为已处理
					_doLevelDicByAllordidx(dicData, item.allordidx, level + 1, resultDic);
				} else if (new RegExp("^"+parentAllordidx).test(item.allordidx)) {  // 找到parentAllordidx的下级节点
					var indent = "";  // 缩进
					for (var i = 1; i < level; i++) {
						indent += "&nbsp;&nbsp;&nbsp;";
					}
					indent += "|-";
					
					resultDic.push({"value": indent + item.value, "key": item.key, "allordidx": item.allordidx});
					
					dicData[idx] = null;  // 标记为已处理
					_doLevelDicByAllordidx(dicData, item.allordidx, level + 1, resultDic);
				}
			
			}
		}
	});
}

/**
 * 校验JSON对象或JS对象的字段合法性（只校验字符串类型和数值类型）
 * @param obj		JSON或JS对象。如：{"name": "zhangsan", "age": 20}
 * @param rule		obj对象各字段对应的正则校验规则，若obj的字段在rule里找不到对应的规则字段，则该字段返回校验成功，可传字符串或对象。如：{"name": "^\\w{1,8}$", "age": {reg: "^[1-9]{1}[0-9]{0,2}$", successMsg: "年龄字段校验成功！", failedMsg: "年龄字段校验失败！"}}
 * 					字符串：直接为校验规则。如："^\\w{1,8}$"
 *					对象：校验规则对象，格式：{reg: "", successMsg: "", failedMsg: ""}。如：{reg: "^[1-9]{1}[0-9]{0,2}$", successMsg: "", failedMsg: ""}
 * 返回格式：{result: [{fieldName: "字段名称", fieldValue: "字段值", fieldRule: "字段校验规则", success: true, msg: "结果消息"}, ...], validateFailedMsg: "", success: true(整体的验证结果，只要有字段验证失败则返回false)}
 */
function validateObjFields(obj, rule) {
	var resultObjs = [];
	var validateFailedMsg = "";
	var success = true;
	
	$.each(obj, function (fieldName, fieldValue) {
		if (isObject(fieldValue)) {
			var _validateResult = validateObjFields(fieldValue, rule[fieldName]);
			resultObjs = resultObjs.concat(_validateResult.result);
			validateFailedMsg += _validateResult.validateFailedMsg;
			if (!_validateResult.success) success = false;
			
			return;
		}
		
		// 只校验字符串类型和数值类型
		if (!fieldValue || ((typeof fieldValue != "string") && (typeof fieldValue != "number"))) {
			resultObjs.push({
				fieldName: fieldName,
				fieldValue: "",
				fieldRule: "",
				success: true,
				msg: "--非字符串类型和数值类型，跳过验证！--"
			});
			
			return;
		}
		
		var _rule = rule[fieldName];
		
		var resultObj = {};
		resultObj.fieldName = fieldName;
		resultObj.fieldValue = fieldValue;
		resultObj.fieldRule = rule[fieldName];
		resultObj.success = true;
		resultObj.msg = (isObject(_rule) && _rule.successMsg) ? _rule.successMsg : "值格式校验成功！";
		
		if (!_rule || (isObject(_rule) && !_rule.reg)) {
			resultObj.msg = (isObject(_rule) && _rule.successMsg) ? _rule.successMsg : "未找到字段值对应的匹配规则，默认校验成功！";
		} else if (! new RegExp(isObject(_rule) ? _rule.reg : _rule).test(fieldValue)) {
			resultObj.success = false;
			resultObj.msg = (isObject(_rule) &&  _rule.failedMsg) ? _rule.failedMsg : "字段[" + fieldName + "=" + fieldValue + "]值格式校验失败！";
			
			validateFailedMsg += resultObj.msg + "__SEPERATOR__";
			success = false;
		}
		
		resultObjs.push(resultObj);
	});
	
	function isObject(obj) { 
		return obj && (typeof obj == "object") && obj.constructor == Object; 
	}
	
	return {
		result: resultObjs,
		validateFailedMsg: validateFailedMsg,
		success: success
	};
}



/*解决js小数位加法精度确实问题，只支持小于9007199254740992的数*/

    /*
     * 判断obj是否为一个整数
     */
    function isInteger(obj) {
        return Math.floor(obj) === obj
    }
    /*
     * 将一个浮点数转成整数和小数两部分。如 3.14 >> 3，0.14
     * @param floatNum {number} 小数
     * @return {object}
     *   {tint:3, tfloat: 0.14}
     */
    function toDepart(floatNum) {
        var ret = {tint: 0, tfloat: 0.0}
        if (isInteger(floatNum)) {
            ret.tint = floatNum
            return ret
        }
        var strfi  = floatNum + ''
        ret.tint  = Number(strfi.split(".")[0])
        ret.tfloat   = Number('0.'+strfi.split(".")[1])
        return ret
    }
    /*
     * 将一个浮点数转成整数，返回整数和倍数。如 3.14 >> 314，倍数是 100
     * @param floatNum {number} 小数
     * @return {object}
     *   {times:100, num: 314}
     */
    function toInteger(floatNum) {
        var ret = {times: 1, num: 0}       
        var strfi  = floatNum + ''
        var dotPos = strfi.indexOf('.')
        var len    = strfi.substr(dotPos+1).length
        var times  = Math.pow(10, len)
        var intNum = parseInt(floatNum * times + 0.5, 10)
        ret.times  = times
        ret.num    = intNum
        return ret
    }

    /*
     * 核心方法，实现加减乘除运算，确保不丢失精度
     * 思路：把小数放大为整数（乘），进行算术运算，再缩小为小数（除）
     *
     * @param a {number} 运算数1
     * @param b {number} 运算数2
     * @param digits {number} 精度，保留的小数点数，比如 2, 即保留为两位小数
     * @param op {string} 运算类型，有加减乘除（add/subtract/multiply/divide）
     *
     */
    function add(a, b) {
    	 var depart1 = toDepart(a);
         var depart2 = toDepart(b); 
        var o1 = toInteger(depart1.tfloat)
        var o2 = toInteger(depart2.tfloat)
        var n1 = o1.num
        var n2 = o2.num
        var t1 = o1.times
        var t2 = o2.times;
        var max = t1 > t2 ? t1 : t2
        var result = null
       
        if (t1 === t2) { // 两个小数位数相同
            result = n1 + n2
        } else if (t1 > t2) { // o1 小数位 大于 o2
            result = n1 + n2 * (t1 / t2);
        } else { // o1 小数位 小于 o2
            result = n1 * (t2 / t1) + n2
        }
        var addflaot = result / max
    	var addint = addflaot>1 ? depart1.tint+depart2.tint+1 : depart1.tint+depart2.tint
    	if(isInteger(addflaot)){
    	   return addint
    	}else {
    	  return addint.toString()+"."+addflaot.toString().split(".")[1]
    	}
           
    }

/**
 * webuploader文件上送后返回的数据格式转换
 * @param successUploadInfos	webuploader调用upload后回调函数参数值，或者低啊用getUploadInfos返回的数据值
 * @param callback				上送后回调函数，参数：转换后的数据格式data(格式: [{"name": "文件名", "g_result":1,"g_message":"上传文件完成","uid":"add03cda312940d5a50efb3c5fc345d8"}, ...])
 */
function webUploaderDataConvert(successUploadInfos) {
	var files = [];
	
	for (var idx in successUploadInfos) {
		var item = successUploadInfos[idx];
		var _result = {
			"name": item.name,
			"g_result": 0,
			"g_message": "服务器未响应数据"
		};
		
		if (item.ret && item.ret.length > 0) {
			for (var idx2 in item.ret) {
				var item2 = item.ret[idx2];
				if (item2.files && item2.files.length > 0) {
					_result["g_result"] = item2.g_result;
					_result["g_message"] = item2.g_message;
					_result["uid"] = item2.files[0].uid;
					
					break;
				}
			}
		}
		
		files.push(_result);
	}
	
	return files;
}

/**
 * webuploader文件上送并对返回数据处理
 * @param webuploaderObj	webuploader实例对象
 * @param callback			上送后回调函数，参数：data(格式: [{"name": "文件名", "g_result":1,"g_message":"上传文件完成","uid":"add03cda312940d5a50efb3c5fc345d8"}, ...])
 */
function doWebUpload(webuploaderObj, callback) {
	webuploaderObj.upload(function (successUploadInfos) {
		var files = webUploaderDataConvert(successUploadInfos);
		
		if (callback) {
			callback(files);
		}
	});
}