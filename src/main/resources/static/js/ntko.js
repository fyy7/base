/**
 * 1、打开模板
 * @param param  参数集合：
 * 	1、type：模板类型;
 * 	2、activityId：当前活动节点；
 * 	3、isEdit：是否可编辑[0,1];
 *  4、path： 正文路径
 *  5、name:  文件名称
 * 	6、model：模板编号；
 *  7、flag： 打开类型：1正文,2红头,3版记
 *  8、btns： 控制按钮；
 *  9、datas：书签数据;
 * 	10、ifCompose：是否已排版[0,1]；
 */
function openWord(param){
	var url = "HttpChannel?action=html__common_open_word&PARAM=" + param;
	openWindow("文档在线编辑", url, "100%", "100%", "");
}

/**
 * 2、保存回填处理
 * @param date 保存返回的Json串。
 */
function saveDocReturnData(date){
	var obj = new Array();
	var fileArr = $("#FILE_ONE").text();
	if(fileArr != null && fileArr != ""){
		var datas = eval('(' + fileArr + ')')
		for(var v in datas){  
			datas[v].FLAG = "D";  //所有的FLAG都更改为D
	        obj.push(datas[v]);
	    } 
	}
	var json = eval('(' + date + ')');
	if(json != null ){
		var str = "<div class='file_list'><a href='#1' onclick='openDocByNtko(\"" + json.file_path + "\",\"" + json.filename_old + "\");'>【发文正文】：" + json.filename_old + "</a>";
		str = str + "<a href='#1' class='del_button' onclick='delWord();' title='删除'></a></div>";	
		$("#btn_edit").hide();
		$("#docfile").html(str);
		
		json.FLAG = "N"; // N：新增，O：原有，D：删除
		obj.push(json);
	}else{
		$("#btn_edit").show();
		$("#docfile").html("");
	}
	
	$("#FILE_ONE").text(JSON.stringify(obj));
}


/**
 * 3、删除操作
 */
function delWord(){
	var obj = new Array();
	var fileArr = $("#FILE_ONE").text();
	if(fileArr != null && fileArr != ""){
		var datas = eval('(' + fileArr + ')')
		for(var v in datas){  
			datas[v].FLAG = "D";  //所有的FLAG都更改为D
	        obj.push(datas[v]);
	    } 
	}
	$("#btn_edit").show();
	$("#docfile").html("");
	$("#FILE_ONE").text(JSON.stringify(obj));
}