/*// 对Date的扩展，将 Date 转化为指定格式的String
// 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符， 
// 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字) 
// 例子： 
// (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423 
// (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18 
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
 * 算字符的字节长度
 */
String.prototype.getBytesLength = function() {   
    var totalLength = 0;     
    var charCode;  
    for (var i = 0; i < this.length; i++) {  
        charCode = this.charCodeAt(i);  
        if (charCode < 0x007f)  {     
            totalLength++;     
        } else if ((0x0080 <= charCode) && (charCode <= 0x07ff))  {     
            totalLength += 2;     
        } else if ((0x0800 <= charCode) && (charCode <= 0xffff))  {     
            totalLength += 3;   
        } else{  
            totalLength += 4;   
        }          
    }  
    return totalLength;   
}  

/**
 * 自定义编码转换函数，不同环境执行相应的编码转换
 * 
 * @param url
 *            地址
 * @returns
 */
function myEncodeURI(url) {
	// 正式环境：WAS
	// return url;
	// 开发环境：Tomcat
	return encodeURI(url);
}

try {
	/*
	 * 消息框按钮汉化
	 */
	$.extend($.messager.defaults, {
		ok : '确定',
		cancel : '取消'
	});

	/*
	 * pagination分页缺省值设置
	 */
	$.extend($.fn.pagination.defaults, {
		showRefresh : false,
		displayMsg : '当前[{from}-{to}],共{total}条',
		beforePageText : '第',
		afterPageText : '页,共{pages}页',
		loadMsg : '处理中...',
		pageList : [ 10, 20, 30, 40 ],
		pageSize : 20
	});

	/*
	 * tabs默认值设置
	 */
	$.extend($.fn.tabs.defaults, {
		width : 'auto',
		height : 'auto',
		border : false
	});

	/*
	 * window遮罩层窗口缺省值
	 */
	$.extend($.fn.window.defaults, {
		modal : true,
		resizable : false,
		maximizable : false,
		minimizable : false,
		collapsible : false,
		fitColumns : true
	});

	/*
	 * datagrid数据表格缺省值
	 */
	$.extend($.fn.datagrid.defaults, {
		loadMsg : '处理中...',
		nowrap : false,
		rownumbers : false,
		singleSelect : true,// 单行选择
		pagination : true,
		pageList : [ 10, 20, 30, 40 ],
		pageSize : 20,
		// 客户端排序
		remoteSort : false,
		onLoadError:function(){
			showMsg("数据加载失败！");
		}
	});
} catch (e) {
	// alert(e);
}

/**
 * 显示、隐藏tabs中的tab页
 * 
 * @param id
 *            tabs的id值
 * @param title
 *            tab的标题
 * @param flag
 *            1为显示、其它为隐藏
 */
function setTabsToggle(id, title, flag) {
	var tab_option = $('#' + id).tabs('getTab', title).panel('options').tab;
	if (flag == 1) {
		// 显示
		tab_option.show();
	} else {
		// 隐藏
		tab_option.hide();
	}
}

/**
 * 获取当前Tabs的选择tab值
 * 
 * @param id
 *            tabs的id值
 * @returns
 */
function getTabsIndexNumber(id) {
	var tab = $('#' + id).tabs('getSelected');
	var index = $('#' + id).tabs('getTabIndex', tab);
	return index;
}

/**
 * 查询条件外围框事件，显示、隐藏效果
 * @resourceUrl 资源（如图片）地址前缀，以"/"结尾
 */
function fieldsetToggle(id, resourceUrl) {
	if (resourceUrl == undefined) {
		resourceUrl = "";
	}
	
	var fieldset_data = $('#' + id).closest("fieldset").children("#fieldset_data");
	fieldset_data.toggle();
	if (fieldset_data.is(":hidden")) {
		$('#' + id).children("img").attr("src", resourceUrl + 'images/DHTMLSuite_plus.gif');
	} else {
		$('#' + id).children("img").attr("src", resourceUrl + 'images/DHTMLSuite_minus.gif');
	}
	$(window).resize();
	return;
}

/**
 * 提交表单数据，处理后进行closeWindow操作，该方法针对有openWindow的窗口进行处理
 * 
 * @param formId
 *            表单Id
 * @param successCallbackFn
 *            执行成功后回调函数
 */
function submitFormData_old(formId, successCallbackFn) {
	showWaitMsg();
	// 表单必填验证
	if (!CheckForm(formId)) {
		hideWaitMsg();
		return;
	}
	// alert(JSON.stringify($("#"+formId).serialize()));

	$.ajax({
		type : "POST",
		url : $("#" + formId).attr('action'),
		data : $("#" + formId).serialize(),
		success : function(data) {
			var obj = jQuery.parseJSON(data);
			obj.showMsgFlag = "1";
			hideWaitMsg();
			window.top.g_jsonData = obj;

			if (obj.g_result == '1') {
				if (null != successCallbackFn && '' != successCallbackFn) {
					eval(successCallbackFn + "()");
				}
				window.top.showMsgAndExecFn(obj.g_message, "closeWindow");
			} else {
				showErrorMsg(obj.g_message);
			}
		},
		error : function(XMLHttpRequest, textStatus, errorThrown) {
			hideWaitMsg();
			showErrorMsg("失败了: " + textStatus);
		}
	});
	return;
}

/**
 * 提交表单数据，处理后进行closeWindow操作，该方法针对有openWindow的窗口进行处理
 * 
 * @param formId
 *            表单Id
 * @param successCallbackFn
 *            执行成功后回调函数
 */
function submitFormData(formId, successCallbackFn) {
	showWaitMsg();

	// 不知道这里为什么了，提交后表单数据会变成乱码了。后查出(过滤器有需要设置字符集，utf-8)
	$('#' + formId).form('submit', {
		// url: ...,
		onSubmit : function() {
			// var isValid = $(this).form('validate');
			var isValid = CheckForm(formId);
			if(isValid && $("#before").length>0){
				isValid = beforeSubmit();
			}
			if (!isValid) {
				hideWaitMsg(); // hide progress bar while the form is invalid
			}
			return isValid; // return false will stop the form submission
		},
		success : function(data) {
			var obj = jQuery.parseJSON(data);
			obj.showMsgFlag = "1";
			hideWaitMsg();
			window.top.g_jsonData = obj;

			if (obj.g_result == '1') {
				if (null != successCallbackFn && '' != successCallbackFn) {
					eval(successCallbackFn + "(obj)");
				}
				window.top.showMsgAndExecFn(obj.g_message, "closeWindow");
			} else {
				showErrorMsg(obj.g_message);
			}
		}
	});
}

/**
 * 提交表单数据，处理后进行closeWindow操作，并且不进行数据必填验证
 * 
 * @param formId
 *            表单Id
 * @param successCallbackFn
 *            执行成功后回调函数
 */
function submitFormDataNoRequired(formId, successCallbackFn) {
	showWaitMsg();

	// 不知道这里为什么了，提交后表单数据会变成乱码了。后查出(过滤器有需要设置字符集，utf-8)
	$('#' + formId).form('submit', {
		// url: ...,
		onSubmit : function() {
			// var isValid = $(this).form('validate');
			var isValid = CheckFormNoRequired(formId);
			if (!isValid) {
				hideWaitMsg(); // hide progress bar while the form is invalid
			}
			return isValid; // return false will stop the form submission
		},
		success : function(data) {
			var obj = jQuery.parseJSON(data);
			obj.showMsgFlag = "1";
			hideWaitMsg();
			window.top.g_jsonData = obj;

			if (obj.g_result == '1') {
				if (null != successCallbackFn && '' != successCallbackFn) {
					eval(successCallbackFn + "(obj)");
				}
				window.top.showMsgAndExecFn(obj.g_message, "closeWindow");
			} else {
				showErrorMsg(obj.g_message);
			}
		}
	});
}

/**
 * 提交表单数据，处理后不进行closeWindow操作
 * 
 * @param formId
 *            表单Id
 * @param successCallbackFn
 *            执行成功后回调函数
 */
function submitFormData2_old(formId, successCallbackFn) {
	showWaitMsg();
	// 表单必填验证
	if (!CheckForm(formId)) {
		hideWaitMsg();
		return;
	}
	// alert(JSON.stringify($("#"+formId).serialize()));

	$.ajax({
		type : "POST",
		url : $("#" + formId).attr('action'),
		data : $("#" + formId).serialize(),
		success : function(data) {
			var obj = jQuery.parseJSON(data);
			obj.showMsgFlag = "1";
			hideWaitMsg();
			window.top.g_jsonData = obj;

			if (obj.g_result == '1') {
				if (null != successCallbackFn && '' != successCallbackFn) {
					eval(successCallbackFn + "()");
				}
				window.top.showMsgAndExecFn(obj.g_message, "");
			} else {
				showErrorMsg(obj.g_message);
			}
		},
		error : function(XMLHttpRequest, textStatus, errorThrown) {
			hideWaitMsg();
			showErrorMsg("失败了: " + textStatus);
		}
	});
	return;
}
/**
 * 提交表单数据，处理后不进行closeWindow操作
 * 
 * @param formId
 *            表单Id
 * @param successCallbackFn
 *            执行成功后回调函数
 */
function submitFormData2(formId, successCallbackFn) {
	showWaitMsg();
	$('#' + formId).form('submit', {
		// url: ...,
		onSubmit : function() {
			// var isValid = $(this).form('validate');
			var isValid = CheckForm(formId);
			if (!isValid) {
				hideWaitMsg(); // hide progress bar while the form is invalid
			}
			return isValid; // return false will stop the form submission
		},
		success : function(data) {
			// alert(data);
			var obj = jQuery.parseJSON(data);
			obj.showMsgFlag = "1";
			hideWaitMsg();
			window.top.g_jsonData = obj;

			if (obj.g_result == '1') {
				if (null != successCallbackFn && '' != successCallbackFn) {
					eval(successCallbackFn + "(obj)");
				}
				window.top.showMsgAndExecFn(obj.g_message, "");
			} else {
				showErrorMsg(obj.g_message);
			}
		}
	});
}

/**
 * 单元格溢出文本省略，easyui数据表格中使用
 * 
 * @param val
 *            溢出文本字符串
 * @param row
 *            表格的行数
 * @returns {String} 返回的字符串
 */
function formatterhide(val, row, index){  
	if(val != "" && val != null && val != "undefined"){
		return '<div title="'+val+'" style="overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">'+val+'</div>'; 
	}
} 

/**
 * 提供英文字母或数字换行，easyui数据表格中使用
 * 
 * @param val
 *            日期字符串，如2014-02-02 12:23:22
 * @param row
 *            表格的行数
 * @returns {String} 返回的字符串
 */
function formatterwrap (val,row,index) {
	if(val != "" && val != null && val != "undefined"){
		return '<div style="word-break:break-all;word-wrap:break-word;white-space:pre-wrap;">'+val+'</div>';
	}
}

/**
 * 格式化日期，easyui数据表格中使用
 * 
 * @param val
 *            日期字符串，如2014-02-02 12:23:22
 * @param row
 *            表格的行数
 * @returns {String} 返回的字符串
 */

function formatterdate(val, row) {
	if(val){
		if(typeof(val) == "number"){
			var newDate = new Date();
		   	newDate.setTime(val);
			return "<span title='" + val + "'>" + newDate.Format("yyyy-MM-dd") + "</span>";
		}else{
			return "<span title='" + val + "'>" + val.substr(0, val.indexOf(" ")) + "</span>";
		}
		
	}
	return ''; 
}

/**
 * 格式化日期，easyui数据表格中使用
 * 
 * @param val
 *            日期字符串，如2014.02.02
 * @param row
 *            表格的行数
 * @returns {String} 返回的字符串
 */

function formatterdate2(val, row) {
	if(val){
		if(typeof(val) == "number"){
			var newDate = new Date();
		   	newDate.setTime(val);
			return "<span title='" + val + "'>" + newDate.Format("yyyy.MM.dd") + "</span>";
		}else{
			return "<span title='" + val + "'>" + val.substr(0, val.indexOf(" ")) + "</span>";
		}
		
	}
	return ''; 
}

/**
 * 格式化日期，easyui数据表格中使用
 * 
 * @param val
 *            日期字符串，如2014-02-02 12:23:22
 * @param row
 *            表格的行数
 * @returns {String} 返回的字符串
 */
function formatterdatetime(val, row) {
	if(val){
		var newDate = new Date();
	   	newDate.setTime(val);
		return newDate.Format("yyyy-MM-dd hh:mm:ss");
	}
	return '';   
}

/**
 * 格式化日期，easyui数据表格中使用
 * 
 * @param val
 *            日期字符串，如2014-02-02 12:23:22
 * @param row
 *            表格的行数
 * @returns {String} 返回的字符串
 */
function formatterdatetime2(val, row) {
	if(val){
		var newDate = new Date();
	   	newDate.setTime(val);
		return newDate.Format("yyyy.MM.dd hh:mm:ss");
	}
	return '';   
}

/**
 * 格式化日期，easyui数据表格中使用
 * 
 * @param val
 *            日期字符串，如2014-02-02 12:23:22
 * @param row
 *            表格的行数
 * @returns {String} 返回的字符串
 */
function formatterdatemerge(val, row) {
	var beginDate = new Date();
   	beginDate.setTime(row.BEGINDATE);
   	var endDate = new Date();
   	endDate.setTime(row.ENDDATE);
	return beginDate.Format("yyyy-MM-dd hh:mm:ss") + ' 至 ' + endDate.Format("yyyy-MM-dd hh:mm:ss");   
}
/*
 * form表单序列化
 */
(function($) {
	$.fn.serializeObject = function() {
		if (!this.length) {
			return false;
		}

		var $el = this, data = {}, lookup = data; // current reference of data

		$el.find(':input[type!="checkbox"][type!="radio"], input:checked').each(function() {
			// data[a][b] becomes [ data, a, b ]
			var named = this.name.replace(/\[([^\]]+)?\]/g, ',$1').split(','), cap = named.length - 1, i = 0;

			for (; i < cap; i++) {
				// move down the tree - create objects or array if necessary
				lookup = lookup[named[i]] = lookup[named[i]] || (named[i + 1] == "" ? [] : {});
			}
			// at the end, psuh or assign the value
			if (lookup.length != undefined) {
				lookup.push($(this).val());
			} else {
				lookup[named[cap]] = $(this).val();
			}
			// assign the reference back to root
			lookup = data;
		});

		return data;
	};
})(jQuery);

/**
 * JQuery扩展方法，用户对JQuery EasyUI的DataGrid控件进行操作。
 */
$.fn.extend({
	/**
	 * 修改DataGrid对象的默认大小，以适应页面宽度。
	 * 
	 * @param heightMargin
	 *            高度对页内边距的距离。
	 * @param widthMargin
	 *            宽度对页内边距的距离。
	 * @param minHeight
	 *            最小高度。
	 * @param minWidth
	 *            最小宽度。
	 * 
	 */
	resizeDataGrid : function(heightMargin, widthMargin, minHeight, minWidth) {
		// alert($(window).height()+"==="+heightMargin);
		var height = $(window).height() - heightMargin;
		var width = $(window).width() - widthMargin;
		// alert(heightMargin);
		height = height < minHeight ? minHeight : height;
		width = width < minWidth ? minWidth : width;
		// alert(width);

		// height-=4;
		// width -=4;

		$(this).datagrid('resize', {
			height : height,
			width : width
		});
	}
});

/**
 * 等待窗口效果
 * 
 * @param info
 *            提示信息
 */
function showWaitMsg(info) {
	$("<div id='id_datagrid-mask' class=\"datagrid-mask\"></div>").css({
		display : "block",
		width : "100%",
		"z-index":999,
		height : $(window).height()
	}).appendTo("body");

	if (!info || info == null || info == "") {
		info = "正在运行，请稍候。。。";
	}

	$("<div id='id_datagrid-mask' class=\"datagrid-mask-msg\"></div>").html(info + "<a href='javascript:void(0)' onclick='hideWaitMsg()'>x</a>").appendTo("body").css({
		display : "block",
		left : ($(document.body).outerWidth(true) - 190) / 2,
		top : ($(window).height() - 45) / 2
	});
}

/**
 * 关闭等待效果
 */
function hideWaitMsg() {
	$.each($("div[id='id_datagrid-mask']"), function() {
		$(this).remove();
	});
}

/**
 * 在右下角显示alert效果内容
 * 
 * @param info
 *            提示内容
 */
function showMsgRight(info) {
	var i_height = 30;// 算高度，动态加高
	window.top.$.messager.show({
		title : '提示',
		msg : info,
		showType : 'show'
	});
}

/**
 * 显示alert效果内容
 * 
 * @param info
 *            提示内容
 */
function showAlert(info) {
	window.top.$.messager.alert('提示', info);
}

/**
 * 在顶中间显示alert效果内容
 * 
 * @param info
 *            提示内容
 */
function showMsg(info) {
	var i_width=300;
	var i_height = (info.getBytesLength() /15) *10;// 算高度，动态加高

	window.top.$.messager.show({
		title : '提示',
		msg : '<div>' + info + '</div>',
		showType : 'slide',// slide,fade,show
		timeout : 2000,// 2秒
		// showSpeed : 500,
		height : (i_height < 150 ? 'auto' : 150),
		width : i_width,
		style : {
			right : '',
			top : document.body.scrollTop + document.documentElement.scrollTop,
			bottom : ''
		}
	});
}

/**
 * 提示内容宽度计算
 * 
 * @param info
 *            提示内容
 */
function getCalculativeWidth(info){
	var arr = info.split("<br/>");
	var maxLength = 0;
	var str = "";
	for(var i=0;i<arr.length;i++){
		if(arr[i].getBytesLength() > maxLength){
			maxLength = arr[i].getBytesLength();
			str = arr[i];
		}
	}

	var div = document.createElement("div");
	div.innerHTML = "<span id='calculativeWidth'>"+str+"</span>";
	document.body.appendChild(div);
	return str.visualLength();
}

String.prototype.visualLength = function() { 
	var ruler = $("#calculativeWidth"); 
	ruler.text(this); 
	return ruler[0].offsetWidth; 
} 

/**
 * 显示error效果内容
 * 
 * @param info
 *            提示内容
 */
function showErrorMsg(info) {
	window.top.$.messager.alert('提示', info, 'error');
}

/**
 * 显示提示，并执行回调函数
 * 
 * @param info
 *            提示内容
 * @param fn
 *            回调的函数
 */
function showMsgAndExecFn(info, fn) {
	showMsg(info);
	if (fn != "") {
		eval(fn + "()");
	}
	// window.top.$.messager.alert('提示', info, 'info', function() {
	// if (fn != "") {
	// eval(fn + "()");
	// }
	// });
}

/**
 * 弹出确认框
 * 
 * @param title
 *            标题
 * @param execFn
 *            回调函数
 */
function showConfirm(title, execFn) {
	window.top.$.messager.confirm('确认', title, function(r) {
		if (r) {
			eval(execFn + "()");
		}
	});
}

/**
 * 解析url中参数转成json对象
 * @param url
 * @returns 
 */
function parseQueryString(url) {
	var obj = {};
	var paraString = url.substring(url.indexOf("?") + 1, url.length).split("&");
	for ( var i in paraString) {
		var keyvalue = paraString[i].split("=");
		var key = keyvalue[0];
		var value = keyvalue[1];
		obj[key] = value;
	}
	return obj;
}


/**
 * 提交表单数据，处理后进行closeWindow操作，该方法针对有openWindow的窗口进行处理
 * 
 * @param formId
 *            表单Id
 * @param fn_ok_callback
 *            执行成功后回调函数
 * @param fn_fail_callback
 *            执行失败后回调函数            
 */
function postForm(formId, fn_ok_callback,fn_fail_callback) {
	$.ajax({
		type : "POST",
		url : $("#" + formId).attr('action'),
		data : $("#" + formId).serialize(),
		success : function(data) {
			var obj =jQuery.parseJSON(data);
			obj.showMsgFlag = "1";
			if (obj.g_result == 1) {
				if (fn_ok_callback && null != fn_ok_callback) {
					fn_ok_callback(obj);
				}
			} else {
				showErrorMsg(obj.g_message);
				if(fn_fail_callback && null != fn_fail_callback){
					fn_fail_callback(obj);
				}
			}
		},
		error : function(XMLHttpRequest, textStatus, errorThrown) {
			showErrorMsg("失败了: " + textStatus);
		}
	});
	return;
}

/**
 * 异步提交ajax，返回回调的json对象
 * 
 * @param surl
 *            网页url
 * @param sdata
 *            提交数据 key-value键对
 * @param sucFunc
 *            成功时回调函数
 * @param errType
 *            错误时是否提示，为0时为不提示，其它的提示
 * @param flag
 *            是否关闭等待标签，空和false开启，其他为关闭
 */
function postAjax(surl, sdata, sucFunc, errType,flag) {
	if(!flag){
		showWaitMsg();
	}
	
	if(!sdata || sdata==null || sdata==''){
		sdata={};
	}
	
	if (sdata instanceof String || (typeof sdata)=='string') {
		// 字符串
		sdata=parseQueryString(sdata);
	}
	
	// 拆解url，改为post提交
	var jsonParams = getUrlParams(surl);
	surl = surl.split("?")[0];
	
	//合并 sdata 和 jsonParams，修改并返回 sdata。
	jQuery.extend(sdata, jsonParams);
	

	$.ajax({
		type : "POST",
		url : surl,
		data : sdata,
		success : function(info) {
			hideWaitMsg();
			var obj_info;
			if(info){
				obj_info =  jQuery.parseJSON(info);
				if (obj_info.g_result) {
					if (obj_info.g_result == '0' && "0" != errType) {
						showMsg("出错了：" + obj_info.g_message);
						return;
					}
				}
			}
			if (sucFunc!=null&&sucFunc != "") {
				if (sucFunc instanceof String || (typeof sucFunc)=='string') {
					eval(sucFunc+"(obj_info)");
				}else{
					sucFunc(obj_info)
				}
			}
		},
		error : function(XMLHttpRequest, textStatus, errorThrown) {
			hideWaitMsg();
			showErrorMsg("失败了: " + textStatus);
		}
	});
}

/**
 * 异步提交ajax，返回回调的json对象
 * 
 * @param surl
 *            网页url
 * @param sdata
 *            提交数据 key-value键对
 * @param sucFunc
 *            成功时回调函数
 * @param errType
 *            错误时是否提示，为0时为不提示，其它的提示
 */
function postAjaxNoMsg(surl, sdata, sucFunc, errType) {
	if(!sdata || sdata==null || sdata==''){
		sdata={};
	}
	
	if (sdata instanceof String || (typeof sdata)=='string') {
		// 字符串
		sdata=parseQueryString(sdata);
	}
	// 拆解url，改为post提交
	var jsonParams = getUrlParams(surl);
	surl = surl.split("?")[0];
	
	//合并 sdata 和 jsonParams，修改并返回 sdata。
	jQuery.extend(sdata, jsonParams);

	$.ajax({
		type : "POST",
		url : surl,
		data : sdata,
		success : function(info) {
			hideWaitMsg();
			if (sucFunc != "") {
				var obj_info = jQuery.parseJSON(info);
				if (obj_info.g_result) {
					if (obj_info.g_result == '0' && "0" != errType) {
						showMsg("出错了：" + obj_info.g_message);
						return;
					}
				}
				eval(sucFunc + "(obj_info)");
			}
		},
		error : function(XMLHttpRequest, textStatus, errorThrown) {
			hideWaitMsg();
			showErrorMsg("失败了: " + textStatus);
		}
	});
}
/**
 * 异步提交ajax，直接返回获取的网页字符串
 * 
 * @param surl
 *            网页url
 * @param sdata
 *            提交数据 key-value键对
 * @param sucFunc
 *            成功时回调函数
 */
function postAjaxAndGetHtml(surl, sdata, sucFunc) {
	showWaitMsg();
	
	if(!sdata || sdata==null || sdata==''){
		sdata={};
	}
	
	if (sdata instanceof String || (typeof sdata)=='string') {
		// 字符串
		sdata=parseQueryString(sdata);
	}
	// 拆解url，改为post提交
	var jsonParams = getUrlParams(surl);
	surl = surl.split("?")[0];
	
	//合并 sdata 和 jsonParams，修改并返回 sdata。
	jQuery.extend(sdata, jsonParams);
	
	$.ajax({
		type : "POST",
		url : surl,
		data : sdata,
		success : function(info) {
			hideWaitMsg();
			if (sucFunc != "") {
				eval(sucFunc + "(info)");
			}

		},
		error : function(XMLHttpRequest, textStatus, errorThrown) {
			hideWaitMsg();
			showErrorMsg("失败了: " + textStatus);
		}
	});
}


/**
 * 同步提交ajax，直接返回获取的网页字符串
 * 
 * @param surl
 *            网页url
 * @param sdata
 *            提交数据 key-value键对
 * @param sucFunc
 *            成功时回调函数
 */
function postSyncAjaxAndGetHtml(surl, sdata, sucFunc) {
	showWaitMsg();
	
	if(!sdata || sdata==null || sdata==''){
		sdata={};
	}
	
	if (sdata instanceof String || (typeof sdata)=='string') {
		// 字符串
		sdata=parseQueryString(sdata);
	}
	// 拆解url，改为post提交
	var jsonParams = getUrlParams(surl);
	surl = surl.split("?")[0];
	
	//合并 sdata 和 jsonParams，修改并返回 sdata。
	jQuery.extend(sdata, jsonParams);
	
	$.ajax({
		type : "POST",
		url : surl,
		data : sdata,
		async : false,
		success : function(info) {
			hideWaitMsg();
			var info1=jQuery.parseJSON(info);
			if (sucFunc != "") {
				eval(sucFunc + "(info1)");
			}
		},
		error : function(XMLHttpRequest, textStatus, errorThrown) {
			hideWaitMsg();
			showErrorMsg("失败了: " + textStatus);
		}
	});
}

/**
 * 同步提交ajax，返回回调的json对象
 * 
 * @param surl
 *            网页url
 * @param sdata
 *            提交数据 key-value键对
 * @param sucFunc
 *            成功时回调函数
 * @param errType
 *            错误时是否提示，为0时为不提示，其它的提示
 * 
 */
function postSyncAjax(surl, sdata, sucFunc, errType) {
	showWaitMsg();
	
	if(!sdata || sdata==null || sdata==''){
		sdata={};
	}
	
	if (sdata instanceof String || (typeof sdata)=='string') {
		// 字符串
		sdata=parseQueryString(sdata);
	}
	// 拆解url，改为post提交
	var jsonParams = getUrlParams(surl);
	surl = surl.split("?")[0];
	
	//合并 sdata 和 jsonParams，修改并返回 sdata。
	jQuery.extend(sdata, jsonParams);
	
	$.ajax({
		type : "POST",
		url : surl,
		async : false,
		data : sdata,
		success : function(info) {
			hideWaitMsg();
			if (sucFunc != "") {
				var obj_info =jQuery.parseJSON(info);
				if (obj_info.g_result) {
					if (obj_info.g_result == '0' && "0" != errType) {
						showMsg("出错了：" + obj_info.g_message);
						return;
					}
				}
				eval(sucFunc + "(obj_info)");
			}
		},
		error : function(XMLHttpRequest, textStatus, errorThrown) {
			hideWaitMsg();
			showErrorMsg("失败了: " + textStatus);
		}
	});
}

/**
 * 设置cookies值
 * 
 * @param key
 *            键key
 * @param value
 *            值
 */
function setCookie(key, value) {
	// 两个参数，一个是cookie的名子，一个是值
	var Days = 30; // 此 cookie 将被保存 30 天
	var exp = new Date(); // new Date("December 31, 9998");
	exp.setTime(exp.getTime() + Days * 24 * 60 * 60 * 1000);
	document.cookie = key + "=" + escape(value) + ";expires=" + exp.toGMTString();
}

/**
 * 取cookies的key值函数
 * 
 * @param key
 *            值
 * @returns 返回值
 */
function getCookie(key) {
	var arr = document.cookie.match(new RegExp("(^| )" + key + "=([^;]*)(;|$)"));
	if (arr != null) {
		return unescape(arr[2]);
	}
	return null;
}

/**
 * 切换主题
 */
function changeThemes() {
	var skin = window.top.getCookie('cs-skin');
	if (skin) {
		$('#swicth-style').attr('href', window.top.themes[skin]);
	}
}

/*
 * 切换字体大小
 */
function changeFontSize(s_size) {
	$("body").css("font-size", s_size + "px");
}
/**
 * 默认回车切换焦点
 */
$(function() {
	// showWaitMsg("页面加载中。。。");
	$('input[class!=Wdate]:text:first').focus();
	var $inp = $('input:text');
	// $inp.css('font-size',"13px");
	$inp.bind('keydown', function(e) {
		var key = e.which;
		if (key == 13) {
			e.preventDefault();
			var nxtIdx = $inp.index(this) + 1;
			$(":input:text:eq(" + nxtIdx + ")").focus();
		}
	});
});

var g_init_close = true;
$(window).load(function() {
	if (g_init_close) {
		g_init_close = false;
		$(window).resize();
	}
	if (getCookie('cs-font_size')) {
		$("body").css("font-size", getCookie('cs-font_size') + "px");
	}
	// hideWaitMsg();
});

/*
 * 回退键处理
 */
$(document).keydown(function(e) {
	var target = e.target;
	var tag = e.target.tagName.toUpperCase();
	// 回退键
	if (e.keyCode == 8) {
		if ((tag == 'INPUT' && !$(target).attr("readonly")) || (tag == 'TEXTAREA' && !$(target).attr("readonly"))) {
			if ((target.type.toUpperCase() == "RADIO") || (target.type.toUpperCase() == "CHECKBOX")) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
});

var base64EncodeChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
var base64DecodeChars = new Array(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1);
/**
 * base64编码
 * 
 * @param {Object}
 *            str
 */
function base64encode(str) {
	str = utf16to8(str);
	var out = "", i = 0, len = str.length;
	var c1, c2, c3;
	while (i < len) {
		c1 = str.charCodeAt(i++) & 0xff;
		if (i == len) {
			out += base64EncodeChars.charAt(c1 >> 2);
			out += base64EncodeChars.charAt((c1 & 0x3) << 4);
			out += "==";
			break;
		}
		c2 = str.charCodeAt(i++);
		if (i == len) {
			out += base64EncodeChars.charAt(c1 >> 2);
			out += base64EncodeChars.charAt(((c1 & 0x3) << 4) | ((c2 & 0xF0) >> 4));
			out += base64EncodeChars.charAt((c2 & 0xF) << 2);
			out += "=";
			break;
		}
		c3 = str.charCodeAt(i++);
		out += base64EncodeChars.charAt(c1 >> 2);
		out += base64EncodeChars.charAt(((c1 & 0x3) << 4) | ((c2 & 0xF0) >> 4));
		out += base64EncodeChars.charAt(((c2 & 0xF) << 2) | ((c3 & 0xC0) >> 6));
		out += base64EncodeChars.charAt(c3 & 0x3F);
	}
	return out;
}
/**
 * base64解码
 * 
 * @param {Object}
 *            str
 */
function base64decode(str) {
	var c1, c2, c3, c4;
	var i = 0, len = str.length, out = "";
	while (i < len) {
		/* c1 */
		do {
			c1 = base64DecodeChars[str.charCodeAt(i++) & 0xff];
		} while (i < len && c1 == -1);
		if (c1 == -1)
			break;
		/* c2 */
		do {
			c2 = base64DecodeChars[str.charCodeAt(i++) & 0xff];
		} while (i < len && c2 == -1);
		if (c2 == -1)
			break;
		out += String.fromCharCode((c1 << 2) | ((c2 & 0x30) >> 4));
		/* c3 */
		do {
			c3 = str.charCodeAt(i++) & 0xff;
			if (c3 == 61)
				return out;
			c3 = base64DecodeChars[c3];
		} while (i < len && c3 == -1);
		if (c3 == -1)
			break;
		out += String.fromCharCode(((c2 & 0XF) << 4) | ((c3 & 0x3C) >> 2));
		/* c4 */
		do {
			c4 = str.charCodeAt(i++) & 0xff;
			if (c4 == 61)
				return out;
			c4 = base64DecodeChars[c4];
		} while (i < len && c4 == -1);
		if (c4 == -1)
			break;
		out += String.fromCharCode(((c3 & 0x03) << 6) | c4);
	}
	return utf8to16(out);
}
/**
 * utf16转utf8
 * 
 * @param {Object}
 *            str
 */
function utf16to8(str) {
	var out = "", i, len = str.length, c;
	for (i = 0; i < len; i++) {
		c = str.charCodeAt(i);
		if ((c >= 0x0001) && (c <= 0x007F)) {
			out += str.charAt(i);
		} else if (c > 0x07FF) {
			out += String.fromCharCode(0xE0 | ((c >> 12) & 0x0F));
			out += String.fromCharCode(0x80 | ((c >> 6) & 0x3F));
			out += String.fromCharCode(0x80 | ((c >> 0) & 0x3F));
		} else {
			out += String.fromCharCode(0xC0 | ((c >> 6) & 0x1F));
			out += String.fromCharCode(0x80 | ((c >> 0) & 0x3F));
		}
	}
	return out;
}
/**
 * utf8转utf16
 * 
 * @param {Object}
 *            str
 */
function utf8to16(str) {
	var out = "", i = 0, len = str.length, c;
	var char2, char3;
	while (i < len) {
		c = str.charCodeAt(i++);
		switch (c >> 4) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
			// 0xxxxxxx
			/**
			 * @returns {Array}
			 */
			out += str.charAt(i - 1);
			break;
		case 12:
		case 13:
			// 110x xxxx 10xx xxxx
			char2 = str.charCodeAt(i++);
			out += String.fromCharCode(((c & 0x1F) << 6) | (char2 & 0x3F));
			break;
		case 14:
			// 1110 xxxx10xx xxxx10xx xxxx
			char2 = str.charCodeAt(i++);
			char3 = str.charCodeAt(i++);
			out += String.fromCharCode(((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
			break;
		}
	}
	return out;
}
/*
 * 检测浏览器版本
 */
function checkBrowser() {
	ua = navigator.userAgent;
	ua = ua.toLocaleLowerCase();
	var browserVersion;
	if (ua.match(/msie/) != null || ua.match(/trident/) != null) {
		browserType = "IE";
		// 哈哈，现在可以检测ie11.0了！
		browserVersion = ua.match(/msie ([\d.]+)/) != null ? ua.match(/msie ([\d.]+)/)[1] : ua.match(/rv:([\d.]+)/)[1];
		browserType = browserType + browserVersion;
	} else if (ua.match(/firefox/) != null) {
		browserType = "火狐";
	} else if (ua.match(/opera/) != null) {
		browserType = "欧朋";
	} else if (ua.match(/chrome/) != null) {
		browserType = "谷歌";
	} else if (ua.match(/safari/) != null) {
		browserType = "Safari";
	}
	var arr = new Array(browserType);
	return arr;
}

/**
 * 图片加载
 * 
 * @param sid
 */
function scrollImageLoading(sid, funcStr) {
	var st = $(window).scrollTop(), sth = st + $(window).height();
	var $targetpics = $("#" + sid).find("img");

	var len = $targetpics.length;
	for (var i = 0; i < len; i++) {
		var $temppic = $targetpics.eq(i);
		var post = $temppic.position().top;
		var posb = post + $temppic.height();

		if ((post > st && post < sth) || (posb > st && posb < sth)) {
			var url = $temppic.attr("xsrc");
			if (url != "") {
				$temppic.attr("xsrc", "");
				$temppic.attr("src", url);
				// $temppic.css("padding","5px");
				$temppic.css("cursor", "pointer");
			}
		}
	}
}

/**
 * 窗口缩放
 */
function resizeEasyUiWindow() {
	$.each($("div .easyui-window"), function() {
		var _this = $(this);
		var i_resize = false;

		// 大于整个width的，开始缩放
		if ((_this.width() + 20) > $(window).width()) {
			if (!_this.attr("initWidth")) {
				_this.attr("initWidth", _this.width());
			}
			_this.window('resize', {
				width : ($(window).width() - 20)
			});
			i_resize = true;
		} else {
			// 返原初始值
			if (_this.attr("initWidth")) {
				_this.window('resize', {
					width : (parseInt(_this.attr("initWidth")) + 20)
				});
				_this.removeAttr("initWidth");
				i_resize = true;
			}
		}
		// 大于整个height的，开始缩放
		if ((_this.height() + 20) > $(window).height()) {
			if (!_this.attr("initHeight")) {
				_this.attr("initHeight", _this.height());
			}
			_this.window('resize', {
				height : ($(window).height() - 20)
			});
			i_resize = true;
		} else {
			// 返原初始值
			if (_this.attr("initHeight")) {
				_this.window('resize', {
					height : (parseInt(_this.attr("initHeight")) + 20)
				});
				_this.removeAttr("initHeight");
				i_resize = true;
			}
		}
		if (i_resize) {
			$.each(_this.find('iframe'), function() {
				$(window.top.$("#" + $(this).attr("id"))[0].contentWindow).resize();
			});
		}
		_this.window('center');
	});
}

/**
 * 获取url中的各项参数
 * 
 * @param _url
 */
function getUrlParams(_url) {
	var jsonParam = {};
	var i = _url.indexOf('?');
	if (i == -1) {
		return jsonParam;
	}

	var queryStr = _url.substr(i + 1);
	var arr_param = queryStr.split('&');

	for (i in arr_param) {
		var arr_tmp = arr_param[i].split('=');
		jsonParam[arr_tmp[0]] = arr_tmp[1];
	}
	return jsonParam;
}

/**
 * combox + ztree
 * 树形多选下拉框
 * 开始
 */
	
function DcodeonClickTree(event, treeId, treeNode, clickFlag){
    var cno= $('#'+treeId+"_select").combobox('getValue');
    var cvalue=$('#'+treeId+"_select").combobox('getText');
    var treeObj = $.fn.zTree.getZTreeObj(treeId);
    var nodes = treeObj.getCheckedNodes(true);
    if(treeNode.children){
        return;
    }
    if(treeNode.checked){
        treeObj.checkNode(treeNode, false, true);
        cno+=",";
        var replacestr=treeNode.CNO+",";
        cno=cno.replace(replacestr,"");
        cno=cno.substring(0,cno.length-1);
        
        cvalue+=",";
        replacestr=treeNode.CVALUE+",";
        cvalue=cvalue.replace(replacestr,"");
        cvalue=cvalue.substring(0,cvalue.length-1);
    }else{
        treeObj.checkNode(treeNode, true, true);
        if(cno){
            cno+=",";
        }
        cno+=treeNode.CNO;
        
        if(cvalue){
            cvalue+=",";
        }
        cvalue+=treeNode.CVALUE;
    }
    $('#'+treeId+"_select").combo('setValue', cno).combo('setText', cvalue);
}

function DcodezTreeOnCheck(event, treeId, treeNode) {
    var cno= $('#'+treeId+"_select").combobox('getValue');
    var cvalue=$('#'+treeId+"_select").combobox('getText');
    var treeObj = $.fn.zTree.getZTreeObj(treeId);
    var nodes = treeObj.getCheckedNodes(true);
    if(treeNode.children){
        return;
    }
    if(!treeNode.checked){
        treeObj.checkNode(treeNode, false, true);
        cno+=",";
        var replacestr=treeNode.CNO+",";
        cno=cno.replace(replacestr,"");
        cno=cno.substring(0,cno.length-1);
        
        cvalue+=",";
        replacestr=treeNode.CVALUE+",";
        cvalue=cvalue.replace(replacestr,"");
        cvalue=cvalue.substring(0,cvalue.length-1);
    }else{
        treeObj.checkNode(treeNode, true, true);
        if(cno){
            cno+=",";
        }
        cno+=treeNode.CNO;
        
        if(cvalue){
            cvalue+=",";
        }
        cvalue+=treeNode.CVALUE;
    }
    $('#'+treeId+"_select").combo('setValue', cno).combo('setText', cvalue);
};
	
	function DcodeonOneClickTree(event, treeId, treeNode, clickFlag){
    	var cno= treeNode.CNO;
        var cvalue=treeNode.CVALUE;
        $('#'+treeId+"_select").combo('setValue', cno).combo('setText', cvalue).combo('hidePanel');
    }
	
	//树及初始化
    function dcodeztreeInit(data){
    	var dcodesetting = {
                view: {
                    selectedMulti: false
                },
                check : {
                    enable : true
                },
                data: {
                    key: {
                        name:"CVALUE"
                    },
                    simpleData: {
                        enable: true,
                        idKey: "CNO",
                        pIdKey: "PARENTNO",
                    }
                },
                callback: {
                    onClick:DcodeonClickTree,
                    onCheck:DcodezTreeOnCheck
                }
            };
   		   

           var type =data.TYPE;
           if(type=="1"){
               dcodesetting={
                       view: {
                           selectedMulti: false
                       },
                       check : {
                           enable : false
                       },
                       data: {
                           key: {
                               name:"CVALUE"
                           },
                           simpleData: {
                               enable: true,
                               idKey: "CNO",
                               pIdKey: "PARENTNO",
                           }
                       },
                       callback: {
                           onClick:DcodeonOneClickTree
                       }
                   };
           }
           var zNodes = data.D_CODE_LIST;
           var intvalue = data.INTVALUE;
           var treeid=data.TREEID;
           $.fn.zTree.init($("#"+treeid), dcodesetting, zNodes);
           var zTree = $.fn.zTree.getZTreeObj(treeid);
           var nodes = zTree.transformToArray(zTree.getNodes());
           for(var i = 0;i < nodes.length;i++){//去除有子节点的复选框
               var id = nodes[i].id;
               var cno = nodes[i].CNO;
               if(type=="1"){//单选使用相等
                   if(intvalue==cno){
                       zTree.checkNode(nodes[i], true, true);
                   }   
               }else{//复选使用包含
                   if(intvalue.indexOf(cno) != -1){
                       zTree.checkNode(nodes[i], true, true);
                   }
                   for(var j = 0;j < nodes.length;j++){
                       if(id == nodes[j].pId){
                           node = zTree.getNodeByParam("id", id, null);
                           node.nocheck = true;
                           zTree.updateNode(node);
                       } 
                   }
               }
           } 
           zTree.expandAll(true);
    }
	
/**
 * combox + ztree
 * 树形多选下拉框
 * 结束
 */
    
    /**
     * eazyui 扩展表格流程已办专用
     * 表中表格
     * 开始
     */
    //已办详细信息列表前面的加号
    function getexpander(val,row,index){
        return '<span class="datagrid-row-expander datagrid-row-expand" style="display:inline-block;width:16px;height:16px;cursor:pointer;" />';
    }
    
	var currentChildRow = -1;//记录展开的节点号
    function intExpandRow(rowIndex,row,tableid,url){
    	var Childcolumns=[[
            {field:'USER_NAME',title:'办理人员',width:150,halign:'center'},
            {field:'ACTIVITY_ID_NAME',title:'办理环节',width:150,halign:'center'},
            {field:'OPERATER_ID_NAME',title:'办理方式',width:150,halign:'center'},
            {field:'OPERATER_AT',title:'办理时间',width:200,formatter:formatterdatetime,halign:'center',align:'center'},
            {field:'OPINION',title:'办理意见',width:400,formatter:formatterhide,halign:'center',align:'left'}
        ]];
		var oldrownum=currentChildRow;
        if(rowIndex != -1 && oldrownum != rowIndex){//折叠上一个展开的节点
            $('#'+tableid).datagrid('collapseRow',currentChildRow); 
        }
        currentChildRow=rowIndex;
        var ddv = $('#'+tableid).datagrid('getRowDetail',rowIndex).find('table.ddv');        
        ddv.datagrid({
            url:url,
            fitColumns:true,
            singleSelect:true,
            rownumbers:true,
            loadMsg:'',
            height:'auto',
            columns:Childcolumns,
            onResize:function(){
                $('#'+tableid).datagrid('fixDetailRowHeight',rowIndex);
            },
            onLoadSuccess:function(){
                setTimeout(function(){
                    $('#'+tableid).datagrid('fixDetailRowHeight',rowIndex);
                },0);
            }
        });
        $('#'+tableid).datagrid('fixDetailRowHeight',rowIndex);
	}   
    
  //扩展已办信息根据页面自适应
    function resetExpandRow(field,width,tableid){
  	  if($('#'+tableid).datagrid('getRowDetail',currentChildRow).find('table.ddv').length>0){
  		  var _maxfbwidth = 0;
  		  var optcol = $('#'+tableid).datagrid('getColumnFields');
  	      for(var i=0;i<optcol.length;i++){
  	    	  var col=$('#'+tableid).datagrid('getColumnOption',optcol[i]);
  	          if(col.width && !col.hidden){
  	        	  _maxfbwidth = _maxfbwidth+col.width;
  	          }
  	      }

      	  var ddv = $('#'+tableid).datagrid('getRowDetail',currentChildRow).find('table.ddv');
      	  ddv.datagrid({
                width: _maxfbwidth
          });
  	  }
    }
    
    /**
     * 富文本图片上传路径重定义
     * @param appId
     * @param saveDirect 为空则上传持久文件，为1则上传临时文件
     * @returns
     */
    function redirectImageActionUrl(appId,saveDirect){
    	UE.Editor.prototype._bkGetActionUrl = UE.Editor.prototype.getActionUrl;
    	UE.Editor.prototype.getActionUrl = function(action) {
    	    if (action == 'uploadimage') { 
    	        return 'upload?APP_ID=' + appId + '&CHECK_TYPE=00&SAVE_DIRECT=' + saveDirect;
    	    } else {
    	        return this._bkGetActionUrl.call(this, action);
    	    }
    	}
    }
    
    /**
     * 富文本图片上传回调
     */
	var imageId = "";
	function uploadImageCallBack(json){
		imageId += json.imageId + ",";
    } 
	
	/**
	 * 保存富文本图片ID
	 * @param myEditor 富文本编辑器初始化定义值
	 * @param uid 上传ID
	 * @param appId 
	 * @param sid 数据库ID
	 * @returns
	 */
	function saveUeditorImageId(myEditor,uid,appId,sid){
		var picture = "";
		if(imageId != ""){	  
			$("#"+uid).val(imageId.substring(0,imageId.length-1));
			var root = UE.htmlparser(UE.getEditor(myEditor).getContent(), true);
			var imgs = root.getNodesByTagName('img' );
			for (i=0;i<imgs.length;i++){
				var url = imgs[i].getAttr('src');
				var content = url.replace("&IS_TMP_FILE=1","");
				var str = "download?APP_ID="+appId+"&UID=";
				content = content.replace(str,"");
				picture += content + ",";
			}
			$("#"+sid).val(picture.substring(0,picture.length-1));
		}
	}