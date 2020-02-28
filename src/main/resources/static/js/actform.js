var act_order = 0;
var act_run_count = 0; // 当前循环次数
var act_run_time = 200; // 循环间隔
var act_finish_max_count = 15; // 循环总数
var act_selectInput = {};// 待填充页面列表
var act_queryList = {};// 后台请求列表
var act_queryJsons = {}; // ajax数据库
var act_fun_flag = false;// 执行状态

var act_targer_input;
var act_tree_config={};
/*
 * ztree 内容整合
 */
//ztree 默认配置
var _act_tree_setting = {
		view: {
			selectedMulti: false
		},
		data: {
			key: {
				title:"",
				name:"NAME"
			},
			simpleData: {
				enable: true,
				idKey: "ID",
				pIdKey: "PID"
			}
		},
		callback: {
			onClick:onClickActTree
		}
	};

var _actTreeDiv='<div><div id="_act_mask" class="_act_mask" style="display: none" ></div><div id="_act_tree_div" class="_act_tree_div"  style="display: none" ><div><input id="act_tree_search" type="text" autocomplete="off"   placeholder="请在这里输入关键字" /><a id="act_expandAllBtn" href="#"  class="btn_edit_small">展开</a><a id="act_collapseAllBtn" href="#"  class="btn_edit_small">折叠</a><a id="act_treeDivSubmit" href="#"  class="btn_edit_small">确定</a><a id="act_treeDivclose" href="#"  class="btn_edit_small">取消</a><a id="act_treeDivclear" href="#"  class="btn_edit_small">清空</a>	</div><div style="width:360px; height: 280px;overflow:auto;" ><ul id="_act_tree_data" class="ztree"></ul></div></div></div>'
$(function(){
		if(!$("#_act_mask")[0]){
			$(document.body).append(_actTreeDiv);
			$('#_act_mask').click(function(){
				act_TreeDiv_hide(false);
			});
		}
		$(document).on( "keyup", "#act_tree_search", function( e ) {
			var treeObj = $.fn.zTree.getZTreeObj("_act_tree_data");
			var allNodes = treeObj.transformToArray(treeObj.getNodes());
			treeObj.hideNodes(allNodes);    //当开始搜索时，先将所有节点隐藏
			var nodeList = treeObj.getNodesByParamFuzzy("NAME", $(this).val(), null);
			     var arr = new Array();
			     for (var i = 0; i < nodeList.length; i++) {
			         arr = $.merge(arr, nodeList[i].getPath());    //找出所有符合要求的叶子节点的路径
			     }
			     treeObj.showNodes($.unique(arr));    //显示所有要求的节点及其路径节点
			     treeObj.expandAll(true);    //展开路径
		});  
});

	


//树形初始化，设置为触发加载数据
function act_setTreeSelect(action, match, flagid, params, allordidx, flagadd,drawConfig){
		var querykey = $.md5(flagid + JSON.stringify(params));
		var config = {};

		config["flagid"] = flagid;
		// 避免因为参数重复导致取错数据
		config["flagadd"] = "tree:"+flagadd;

		if (allordidx) {
			config["allordidx"] = allordidx;
		}
		config["querykey"] = querykey;
		//树形配置整合
		if(drawConfig){
			 for(var key in _act_tree_setting){
		
				if(!drawConfig[key]){
					drawConfig[key]=_act_tree_setting[key];
				}
			   }
			
		}else{
			drawConfig=_act_tree_setting;
		}
		
		config["drawConfig"] = drawConfig;
		if (!act_queryList[querykey]) {
			act_queryList[querykey] = true;
			postAjax("restful?action=" + action + "&flagId=" + flagid + "&queryId=" + querykey + "&V=" + Math.random(), params, "act_getTreeSelectJson");

		}
		//禁止体现历史记录，避免叠加
		$(match).attr("autocomplete","off");
		$( document ).on( "click", match, function( e ) {
			e.stopPropagation();  //阻止冒泡
			if (!$(this).attr("id")) {
				$(this).attr("id", "act_order_" + act_order);
			
			}
			if(!act_tree_config[$(this).attr("id")]){
				act_tree_config[$(this).attr("id")] = config;
			}
			if($(this).attr("_act_search")){
				$("#act_tree_search").show()
			}else{
				$("#act_tree_search").hide()
			}
			if($(this).attr("_act_clear")==false){
				$("#act_treeDivclear").hide()
			}else{
				$("#act_treeDivclear").show()
			}
			act_TreeDiv_show($(this))}); 
	}

function act_tree_callback(obj,data){
	if(data){
		var dataname="";
		var dataid="";
		for(i=0;i<data.length;i++){
			if(dataname){
				dataname+=",";
				dataid+=",";
			}
			dataname+=data[i]["NAME"];
			dataid+=data[i]["ID"];
		}

		obj.val(dataname)
		if(obj.attr("_actTargerID")){
			$("#"+obj.attr("_actTargerID")).val(dataid)
		}
	}else{
		obj.val("");
		if(obj.attr("_actTargerID")){
			$("#"+obj.attr("_actTargerID")).val("");
		}
	}

}

//加载树形
function act_TreeDiv_show(event){
	act_targer_input=event;
	var tree_div_height=event.offset().top+30;
	var comple_tree_div_heigth=$(window).height()-tree_div_height;
	if(comple_tree_div_heigth<$("#_act_tree_div").height()){
		if($("#_act_tree_div").height()<tree_div_height){
			tree_div_height=tree_div_height-$("#_act_tree_div").height();
		}else{
			tree_div_height=$(window).height()-$("#_act_tree_div").height();
		}
		
	}

	
	$("#_act_tree_div").css("left",event.offset().left).css("top",tree_div_height);
		$("#_act_tree_div").show();
		act_initTreeData(event.attr("id"));
		$("#_act_mask").show();
}
//隐藏树形
function act_TreeDiv_hide(){

	$("#_act_tree_div").hide();
	$("#_act_mask").hide();
	act_targer_input=null;
}
	function act_getTreeSelectJson(json){
		if (json.g_result == '1') {
			act_queryJsons[json.queryId] = json.rows;
		}
	}
	function act_initTreeData(key){

		var config = act_tree_config[key];
		if (!config || !config.querykey) {
			return;
		}

		if(!config.flagadd&&config.flagadd.split(":")[0]!="tree"){
			return ;
		}
		


		var job = act_queryJsons[config.querykey];
		if (!job) {
			return;
		}
		//避免重复加载导致性能损耗
		if(config.querykey!=$("#_act_tree_data").attr("dataId")){
			$.fn.zTree.init($("#_act_tree_data"), config.drawConfig, job);
		}
		if(config["drawConfig"]["check"]["enable"]){
			var act_zTree = $.fn.zTree.getZTreeObj("_act_tree_data");
			act_zTree.checkAllNodes(false);
			if(act_targer_input.attr("_actTargerID")){
			var dataid=	$("#"+act_targer_input.attr("_actTargerID")).val();
			if(dataid!=null){
			
				var datas=dataid.split(",");
				for(var key in datas){
					node = act_zTree.getNodeByParam("ID",datas[key],null);
					if(node!=null){
						act_zTree.checkNode(node,datas[key], false, true);
						node.oldchecked="Y";
					}
				}
			}
			
			
			//		for(var key in rolesData){
//			node = zTree.getNodeByParam("RID",key,null);
//			if(node!=null){
//				zTree.checkNode(node,rolesData[key], false, true);
//				node.oldchecked="Y";
//			}
//		}
			}
		}
		
		$("#act_expandAllBtn").bind("click", {type:"expandAll"}, act_expandNode);
		$("#act_collapseAllBtn").bind("click", {type:"collapseAll"}, act_expandNode);

		$( document ).on( "click", "#act_treeDivclose", function( e ) {act_TreeDiv_hide(false)} ); 	
		$( document ).on( "click", "#act_treeDivSubmit", function( e ) {
			act_submitData();
			act_TreeDiv_hide(false)
			}); 	
		$( document ).on( "click", "#act_treeDivclear", function( e ) {
			act_tree_callback(act_targer_input,null);
			act_TreeDiv_hide(false);
			} ); 	
		$("#_act_tree_data").attr("dataId",config.querykey);
	}
	

	function act_expandNode(e) {
		var zTree = $.fn.zTree.getZTreeObj("_act_tree_data");
		var type = e.data.type;
		var nodes = zTree.getSelectedNodes();
		if (type.indexOf("All")<0 && nodes.length == 0) {
			alert("请先选择一个父节点");
		}

		if (type == "expandAll") {
			zTree.expandAll(true);
		} else if (type == "collapseAll") {
			zTree.expandAll(false);
		}
	}
	//确认选择
	function act_submitData(){
		var zTree = $.fn.zTree.getZTreeObj("_act_tree_data");
		if(zTree){
			//获取勾选的
			var nodes = zTree.getCheckedNodes(true);	
			if(nodes.length>0){
				if(act_targer_input){
					act_tree_callback(act_targer_input,nodes);
				}
			}
		}
	
	}
	//默认点击事件
	function onClickActTree(event, treeId, treeNode, clickFlag){
		if(treeNode.PARENTID=='ResourceTop'){
			return true;
		}
		var nodes=[];
		nodes.push(treeNode)
		if(act_targer_input){
		act_tree_callback(act_targer_input,nodes);
		var 	callback=act_targer_input.attr("_act_callback");
			if (callback != undefined) {
				if(callback.indexOf("(")>0){
					eval(callback);
				}else{

					eval(callback+"(nodes,act_targer_input);");
				}
        	
        	}
			act_TreeDiv_hide(true);
		}
	
	}
	
	
function act_select2Checkbox(obj, swidth) {
	var id = obj.attr("id");
	var name = obj.attr("name");
	var width = obj.width();
	var onfun = obj.attr("onchange");

	var val = obj.val();
	var title = obj.attr("title");

	var html = "<span style='width:" + width + ";padding-top: 3px;'>";
	obj.find("option").each(function(ind) {
		if ($(this).val() && $(this).text()) {
			if (!swidth) {
				var new_swidth = (($(this).text().length) * 14 + 25);
			} else {
				new_swidth = swidth;
			}
			html += "<span style='cursor:pointer;width:" + new_swidth + ";' class='selectToCheckbox' ><input type='checkbox' name='" + name + "' text='" + $(this).text() + "' title='" + title + "'  value='" + $(this).val() + "'  " + (val == $(this).val() ? "checked" : "") + "  " + (obj.attr("required") ? " required='" + obj.attr("required") + "' warning='" + obj.attr("warning") + "' " : "") + " style='width:15px;height:15px;border:0px;'/>&nbsp;" + $(this).text() + "</span>";
		}
	});
	html += "</span>";
	obj.after(html);

	obj.parent().find("span.selectToCheckbox").click(function(event) {

		if (!$(this).find('input').prop('checked')) {

			$(this).find('input').prop('checked', false);
		} else {

			$(this).find('input').prop('checked', true);
		}
		event.stopPropagation();
	});

	if (onfun != null) {
		obj.parent().find("input[type='checkbox']").on("click", function(event) {
			eval(onfun);
		});
		obj.parent().find("span.selectToCheckbox").on("click", function(event) {
			eval(onfun);
		});
	}

	obj.remove();
}

// select 转 radio
function act_selToRadio(obj, swidth) {
	var id = obj.attr("id");
	var name = obj.attr("name");
	var width = obj.width();
	var onfun = obj.attr("onchange");
	var cancelfun = obj.attr("cancel");
	var val = obj.val();
	var title = obj.attr("title");

	var html = "<span style='width:" + width + ";padding-top: 3px;'>";
	obj.find("option").each(function(ind) {
		if ($(this).val() && $(this).text()) {
			if (!swidth) {
				var new_swidth = (($(this).text().length) * 14 + 25);
			} else {
				new_swidth = swidth;
			}

			html += "<span style='cursor:pointer;width:" + new_swidth + ";' class='selectToRadio' ><input type='radio' name='" + name + "' text='" + $(this).text() + "' title='" + title + "'  value='" + $(this).val() + "'  " + (val == $(this).val() ? "checked" : "") + "  " + (obj.attr("required") ? " required='" + obj.attr("required") + "' warning='" + obj.attr("warning") + "' " : "") + " style='width:15px;height:15px;border:0px;'/>&nbsp;" + $(this).text() + "</span>";
		}
	});
	html += "</span>";
	obj.after(html);

	obj.parent().find("span.selectToRadio").click(function(event) {
		// $(this).find('input').attr('checked',!$(this).find('input').attr('checked'));
		$(this).find('input').prop('checked', true);
		event.stopPropagation();
	});

	obj.parent().find("input[type='radio']").on("mousedown", function(event) {
		// $(this).attr('checked',!$(this).attr('checked'));
		$(this).prop('checked', true);
		return false;
	});
	obj.parent().find("input[type='radio']").on("keydown", function(event) {
		// alert(event.which)
		if (event.which == "32") {
			// $(this).attr('checked',!$(this).attr('checked'));
			$(this).prop('checked', true);
		}
	});

	obj.parent().find("input[type='radio']").click(function(event) {
		return false;
	});
	if (onfun != null) {
		obj.parent().find("input[type='radio']").on("click", function(event) {
			eval(onfun);
		});
		obj.parent().find("span.selectToRadio").on("click", function(event) {
			eval(onfun);
		});
	}
	if (cancelfun == '1') {
		obj.parent().find("input[type='radio']").on("dblclick", function(event) {
			$(this).prop('checked', false);
		});
		obj.parent().find("span.selectToRadio").on("dblclick", function(event) {
			$(this).prop("input[type='radio']").attr('checked', false);
		});

	}

	obj.remove();
}

// 下拉框初始化
function act_setSelectInit(action, match, flagid, params, allordidx) {
	act_fun_flag = false;
	$(match).each(function(ind) {
		if (!$(this).attr("id")) {
			$(this).attr("id", "act_order_" + act_order);
		}

		act_setSelectValue(action, $(this).attr("id"), flagid, params, allordidx);
	});
}

function act_execfunstr(funstr) {
	if (funstr) {
		if (funstr.indexOf("(") > 0) {
			eval(funstr);
		} else {
			eval(funstr + "()");
		}
	}
}

// 判断全局是否初始化完成
function act_isfinish() {
	for ( var obj in act_selectInput) {
		return false;
	}
	return true;
}

// 初始化结束执行方法
function act_do_finish(funstr, maxcount) {
	if (act_isfinish()) {
		act_fun_flag = true;
	}
	if (funstr) {
		if (maxcount) {
			act_finish_max_count = maxcount;
		}

		if (act_fun_flag == true) {

			act_execfunstr(funstr);
		} else {

			if (act_run_count <= act_finish_max_count) {
				act_run_count++;
				setTimeout(function() {
					act_do_finish(funstr);
				}, act_run_time);
			}
		}
	}
}

/*
 * act下拉框数据填值
 */
function act_setSelectValue(action, id, flagid, params, allordidx, flagadd) {
	// 使用MD5参数作为后台数据存储验证
	var querykey = $.md5(flagid + JSON.stringify(params));

	var config = {};

	config["flagid"] = flagid;
	// 避免因为参数重复导致取错数据
	if (flagadd) {
		config["flagadd"] = flagadd;
	}
	if (allordidx) {
		config["allordidx"] = allordidx;
	}

	config["querykey"] = querykey;
	act_selectInput[id] = config;

	if (act_queryJsons[querykey]) {
		act_setSelect(id);

	} else if (!act_queryList[querykey]) {
		act_queryList[querykey] = true;
		postAjax("restful?action=" + action + "&flagId=" + flagid + "&queryId=" + querykey + "&V=" + Math.random(), params, "act_getSelectJson");

	}
}

function act_getSelectJson(json) {
	if (json.g_result == '1') {
		act_queryJsons[json.queryId] = json.rows;
		act_setSelectAll();
	}
}

function act_setSelectAll() {
	for ( var key in act_selectInput) {
		act_setSelect(key);
	}
}

function act_setSelect(key) {
	var config = act_selectInput[key];
	if (!config || !config.querykey) {
		return;
	}

	var job = act_queryJsons[config.querykey];
	if (!job) {
		return;
	}

	var allordidx = config.allordidx;
	var flagid = config.flagid

	for (i = 0; i < job.length; i++) {
		var addStr = "";
		if (allordidx) {
			var tmp_size = job[i][allordidx].split(".");
			if (tmp_size.length > 2) {
				for (var j = 2; j < tmp_size.length; j++) {
					addStr += "　";
				}
				addStr += "|-";
			}
		}
		var datastr = "";
		for ( var keyname in job[i]) {
			datastr += " " + keyname + "='" + job[i][keyname] + "'";
		}
		$("#" + key).append("<option value='" + job[i].DM + "'" + datastr + ">" + addStr + job[i].DMNR + "</option>");
	}

	// 清楚id避免重复执行
	delete act_selectInput[key];

	if ($("#" + key).attr("select-value")) {

		$("#" + key).find("option[value='" + $("#" + key).attr("select-value") + "']").attr("selected", true);
	} else {
		$("#" + key).prop('selectedIndex', 0);
	}
	if ($("#" + key).attr("sucFunc")) {
		var kk=$("#" + key).attr("sucFunc");
		if ($("#" + key).attr("sucFunc").indexOf("(") > 1) {
			eval($("#" + key).attr("sucFunc"));
		} else {
			eval($("#" + key).attr("sucFunc") + "()");
		}
	}
	//一次性临时方法
	if ($("#" + key).attr("tmpFunc")) {
		if ($("#" + key).attr("tmpFunc").indexOf("(") > 1) {
			eval($("#" + key).attr("tmpFunc"));
		} else {
			eval($("#" + key).attr("tmpFunc") + "()");
		}
		$("#" + key).removeAttr("tmpFunc");
	}
}
