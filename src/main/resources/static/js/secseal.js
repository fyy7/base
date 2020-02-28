/**
 * 1、打开电子印章控件窗口
 * @param  Param 参数集合
 */
function openSecSeal(param){
	var url = "HttpChannel?action=html__common_open_secseal&PARAM=" + param;
	openWindow("公文盖章", url, "100%", "100%", "");
}

/**
 * 2、保存回填处理
 * @param date 保存返回的Json串。
 */
function saveStampDocReturn(date){
	var obj = new Array();
	var fileArr = $("#FILE_ONE_EDC").text();
	if(fileArr != null && fileArr != ""){
		var datas = eval('(' + fileArr + ')')
		for(var v in datas){  
			datas[v].FLAG = "D";  //所有的FLAG都更改为D
	        obj.push(datas[v]);
	    } 
	}
	var json = eval('(' + date + ')');
	
	if(json != null ){
		var str = "<div class='file_list'><a href='#1'  onclick=\"downfile('" + json.Id + "','" + json.file_path + "','" + json.filename_new + "','N')\">【盖章文件】：" + json.filename_old + "</a></div>";	
		$("#btn_edit_edc").hide();
		$("#docfile_edc").html(str);
		
		json.FLAG = "N"; // N：新增，O：原有，D：删除
		obj.push(json);
	}else{
		$("#btn_edit_edc").show();
		$("#docfile_edc").html("");
	}
	
	$("#FILE_ONE_EDC").text(JSON.stringify(obj));
}