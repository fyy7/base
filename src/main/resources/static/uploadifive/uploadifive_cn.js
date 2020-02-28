/**
 * 初始化上传控件
 * 
 * @param fileId
 *            需要变成上传控件的id,以要fileId开头的都将被初始化
 * @param uploadUrl
 *   		  上传至服务器的地址
 * @param b_func
 *          上传完成时调用的方法
 */
function init_uploadFile(fileId,uploadUrl,fileType) {
	$("#"+fileId).before("<div id='"+fileId+"_list'></div>"); 
	
	var uploadLimit = 1;
	if ($("#"+fileId).attr("uploadLimit") != undefined) {
		uploadLimit = parseInt($("#"+fileId).attr("uploadLimit"));
	}
	
	var fileReturnContentId = "";
	if ($("#"+fileId).attr("uploadFileReturnContentId") != undefined) {
		fileReturnContentId = $("#"+fileId).attr("uploadFileReturnContentId");
	}
	
	var fileRelationId = "";
	if ($("#"+fileId).attr("uploadFileReturnRelationId") != undefined) {
		fileRelationId = $("#"+fileId).attr("uploadFileReturnRelationId");
	}
	
	var buttonClass = "";
	if ($("#"+fileId).attr("buttonClass") != undefined) {
		buttonClass = $("#"+fileId).attr("buttonClass");
	}
	
	var buttonWidth = 110;
	if ($("#"+fileId).attr("buttonWidth") != undefined) {
		buttonWidth = parseInt($("#"+fileId).attr("buttonWidth"));
	}
	
	var buttonHeight = 36;
	if ($("#"+fileId).attr("buttonHeight") != undefined) {
		buttonHeight = parseInt($("#"+fileId).attr("buttonHeight"));
	}
	
	var buttonWord = "";
	if ($("#"+fileId).attr("buttonWord") != undefined) {
		buttonWord = $("#"+fileId).attr("buttonWord");
	}else if(fileType=="img"){
		buttonWord = "选择图片";
	}else if(fileType=="video"){
		buttonWord = "选择视频";
	}else{
		buttonWord = "选择文件";
	}
	
	var fileTypeExts = "*/*";
	if ($("#"+fileId).attr("fileTypeExts") != undefined) {
		fileTypeExts = $("#"+fileId).attr("fileTypeExts");
	}else if(fileType=="img"){
		fileTypeExts = "image/*";
	}
	
	var fileSizeLimit = "2MB";
	if ($("#"+fileId).attr("fileSizeLimit") != undefined) {
		fileSizeLimit = $("#"+fileId).attr("fileSizeLimit");
	}
	
	var formData = {};
	if ($("#"+fileId).attr("formData") != undefined) {
		formData = JSON.parse($("#"+fileId).attr("formData"));
	}
	
	$('#'+fileId).uploadifive({
		'auto': true,
		'multi': false,
		'width': buttonWidth,
		'height': buttonHeight,
		'formData': formData,
		'removeCompleted': true,
		'removeTimeout': 500,
		'fileType': fileTypeExts,
		'buttonText': buttonWord,
		'uploadScript': uploadUrl,
		'buttonClass': buttonClass,
		'fileSizeLimit': fileSizeLimit,
		'onInit': function() {
			if(!fileType){
				initShowList(fileId,fileReturnContentId,fileRelationId,uploadLimit);
			}else if(fileType == "img"){
				initShowImgList(fileId,fileReturnContentId,fileRelationId,uploadLimit);
			}
			if($('#'+fileRelationId).attr("required")=='required'){
				$('.uploadifive-button').after('<div style="vertical-align:middle;line-height:36px;"><span style="color:red;padding-left:2px;" required-sign="true">*</span></div>');
			}
	    },
	    'onAddQueueItem':function(file) {//为添加到队列中的每个文件触发
	    	if(file.size == 0){
	    		showMsg("空文件不能上传！");
				return false;
	    	}
	    },
       	'onUploadComplete':function(file, data) {//每一个文件上传完毕时执行
			var jsondata = jQuery.parseJSON(data);
			if(jsondata.err_msg){
				showMsg(jsondata.err_msg);
				$('#uploadifive-'+fileId).addClass("disabled");
				$('#uploadifive-'+fileId).find(".hot-area").hide();
				return false;
			}
			if (fileReturnContentId != '') {
				if(!fileType){
					handleReturnData(fileId,fileReturnContentId,fileRelationId,jsondata);
				}else if(fileType == "img"){
					handleReturnDataImage(fileId,fileReturnContentId,fileRelationId,jsondata);
				}
			}
			
			if (uploadLimit <= $('#'+fileId+'_list').children('.file_list').length) {
				$('#uploadifive-'+fileId).addClass("disabled");
				$('#uploadifive-'+fileId).find(".hot-area").hide();
			}
        },
		'onError': function(errorType,file) {
	    	if(errorType == "FILE_SIZE_LIMIT_EXCEEDED"){
	    		var fileSize = file.size/1024/1024;
	    		showMsg('文件["' + file.name + '"]（'+fileSize.toFixed(2)+'MB）超出大小限制（' + fileSizeLimit + '）。');
	    	}else if(errorType == "FORBIDDEN_FILE_TYPE"){
	    		showMsg("禁止上传该类型文件！");
	    	}else if(errorType == "404_FILE_NOT_FOUND"){
	    		showMsg("文件未上传成功或服务器存放文件的文件夹不存在！");
	    	}else if(errorType == "403_FORBIDDEN"){
	    		showMsg("无上传权限！");
	    	}
	    },
	});
}

/**
 * 页面初始化时展示已传数据
 * @param fileId
 * @param contentId
 * @param relationId
 * @param num
 * @param fileType
 * @returns
 */
function initShowList(fileId,contentId,relationId,uploadLimit){
	if(contentId){
		var str = $("#"+contentId).val();
		var tmp = $("#"+fileId+"_list").html(); //避免切换页面导致重复调用
		if(str && !tmp){
			var jsons = jQuery.parseJSON(str);
			if(uploadLimit <= jsons.length){
				$('#uploadifive-'+fileId).addClass("disabled");
				$('#uploadifive-'+fileId).find(".hot-area").hide();
			}
			var relationData = "";
			for(var i=0;i<jsons.length;i++){
				relationData += jsons[i].uid+",";
				addfilelist(fileId, contentId, relationId, jsons[i]);
			}
			saveRelationId(relationId,relationData);
		}
	}
}

/**
 * 页面初始化时展示已传数据
 * @param fileId
 * @param contentId
 * @param relationId
 * @param uploadLimit
 * @param fileType
 * @returns
 */
function initShowImgList(fileId,contentId,relationId,uploadLimit){
	if(contentId){
		var str = $("#"+contentId).val();
		var tmp = $("#"+fileId+"_list").html(); //避免切换页面导致重复调用
		if(str && !tmp){
			var jsons = jQuery.parseJSON(str);
			if(uploadLimit <= jsons.length){
				$('#uploadifive-'+fileId).addClass("disabled");
				$('#uploadifive-'+fileId).find(".hot-area").hide();
			}
			var relationData = "";
			for(var i=0;i<jsons.length;i++){
				relationData += jsons[i].uid+",";
				addImagelist(fileId,contentId,relationId,jsons[i]);
			}
			saveRelationId(relationId,relationData);
		}
	}
}

/**
 * 处理上传后服务器返回的数据
 * 
 * @param fileId
 * @param data
 */
function handleReturnData(fileId,contentId,relationId,data) {
	if(data){
		if(data.err_msg){
			showMsg(data.err_msg);
			$('#uploadifive-'+fileId).addClass("disabled");
			$('#uploadifive-'+fileId).find(".hot-area").hide();
			return false;
		}
		
		var contentData = [];
		if ($("#"+contentId).val() != "") {
			eval("contentData=" + $("#"+contentId).val());
		} 
		
		var relationData = "";
		if ($("#"+relationId).val() != "") {
			relationData+= $("#"+relationId).val()+",";
		}
		
		var files = data.files;
		for(var i=0;i<files.length;i++){
			data.files[i]["flag"]="N";
			contentData.push(data.files[i]);
			relationData+= data.files[i].uid+",";
			addfilelist(fileId,contentId,relationId,data.files[i]);// 添加上传列表
		}
		
		saveContentData(contentId,contentData);// 回填文件内容
		saveRelationId(relationId,relationData);// 回填文件id
	}
}

/**
 * 处理上传后服务器返回的数据
 * @param fileId
 * @param contentId
 * @param relationId
 * @param data
 * @returns
 */
function handleReturnDataImage(fileId,contentId,relationId,data) {
	if(data){
		if(data.err_msg){
			showMsg(data.err_msg);
			$('#uploadifive-'+fileId).addClass("disabled");
			$('#uploadifive-'+fileId).find(".hot-area").hide();
			return false;
		}
		
		var contentData = [];
		if ($("#"+contentId).val() != "") {
			eval("contentData=" + $("#"+contentId).val());
		} 
		
		var relationData = "";
		if ($("#"+relationId).val() != "") {
			relationData+= $("#"+relationId).val()+",";
		}
		
		var files = data.files;
		for(var i=0;i<files.length;i++){
			data.files[i]["flag"]="N";
			contentData.push(data.files[i]);
			relationData+= data.files[i].uid+",";
			
			addImagelist(fileId,contentId,relationId,data.files[i]);// 添加上传列表
		}
		
		saveContentData(contentId,contentData);// 回填文件内容
		saveRelationId(relationId,relationData);// 回填文件id
	}
}

/**
 * 回填文件内容
 * @param contentId
 * @param contentData
 * @returns
 */
function saveContentData(contentId,contentData){
	if(contentId){
		if(contentData.length>0){
			$("#"+contentId).val(JSON.stringify(contentData));
		}else{
			$("#"+contentId).val("");
		}
	}
}

/**
 * 回填文件id
 * @param relationId
 * @param relationData
 * @returns
 */
function saveRelationId(relationId,relationData){
	if(relationId){
		if(relationData.length>0){
			$("#"+relationId).val(relationData.substring(0, relationData.length-1));
		}else{
			$("#"+relationId).val("");
		}
	}
}

/**
 * 添加上传列表
 * @param fileId
 * @param contentId
 * @param relationId
 * @param json
 * @returns
 */
function addfilelist(fileId,contentId,relationId,json){
	$("#"+fileId+"_list").append("<div class='file_list'><i class='uploadifive_icon'></i><a href='#' onclick=\"downfile('"+json.appId+"','"+json.uid+"','"+json.name+"','"+json.saveDirect+"','"+json.suffix+"','"+json.save_path+"')\">"+json.originalFilename+"</a>  <a class='del_button' onclick='delfile(\""+fileId+"\",\""+contentId+"\",\""+relationId+"\",this)' filenew='"+json.uid+"'></a></div>");
}

/**
 * 添加图片上传列表
 * @param fileId
 * @param contentId
 * @param relationId
 * @param json
 * @returns
 */
function addImagelist(fileId,contentId,relationId,json){
    if($("#"+fileId+"_img").length>0){
        var url="download?APP_ID="+json.appId+"&UID="+json.uid+"&IS_TMP_FILE="+json.saveDirect+"&FILE_SUFFIX="+json.suffix;
        var onclickstr="onclick=\"downfile('"+json.appId+"','"+json.uid+"','"+json.name+"','"+json.saveDirect+"','"+json.suffix+"','"+json.save_path+"')\"";
        $("#"+fileId+"_img").html('<img id="img_upload" alt="" src="" style="width:340px;height:200px;border:1px solid #000;" '+onclickstr+'>');
        $("#"+fileId+"_img").find("img").attr("src",url);
    }
}

/**
 * 添加删除上传列表,及其对应的数据
 * @param fileId
 * @param contentId
 * @param relationId
 * @param obj
 * @returns
 */
function delfile(fileId,contentId,relationId,obj){
	var kk = $("#"+fileId).data('uploadifive');
	if(kk.settings.multi==false){
		$('#uploadifive-'+fileId).addClass("disabled");
		$('#uploadifive-'+fileId).find(".hot-area").hide();
	}
	
	var contentData = [];
	var tmpJson=eval("([])");
	if ($("#"+contentId).val() != "") {
		eval("contentData=" + $("#"+contentId).val());
	} 
	for(var i=0;i<contentData.length;i++){
		if(contentData[i].uid){
			if(contentData[i].uid==$(obj).attr("filenew")){
				contentData[i]["flag"]="D";
				tmpJson.push(contentData[i]);
				$(obj).parents(".file_list").delay(200).fadeOut(500, function() {
					$(this).remove();
				});
			}else{
				tmpJson.push(contentData[i]);
			}
		}
	}
	contentData=tmpJson;
	saveContentData(contentId,contentData);
	
	var relationData = "";
	if ($("#"+relationId).val() != "") {
		var relationStr = $("#"+relationId).val();
		var relationArr = relationStr.split(",");
		for(var i=0;i<relationArr.length;i++){
			if(relationArr[i]!=$(obj).attr("filenew")){
				relationData+=relationArr[i]+",";
			}
		}
	}
	saveRelationId(relationId,relationData);
	
	$('#uploadifive-'+fileId).removeClass("disabled");
	$('#uploadifive-'+fileId).find(".hot-area").show();
}

/**
 * 通用下载页面
 * @param appId
 * @param uid
 * @param filename
 * @param isTmpFile
 * @param filesuffix
 * @returns
 */
function downfile(appId,uid,filename,saveDirect,filesuffix,savePath){
	var path = "/cache/"+uid;
	var isTmpFile = "1";
	if(saveDirect == "1"){
		path = savePath;
		isTmpFile = "";
	}
	var url="download?TYPE=02&APP_ID="+appId+"&UID="+uid+"&IS_TMP_FILE="+isTmpFile+"&FILE_SUFFIX="+filesuffix+"&NAME_TO_SHOW="+filename+"&PATH="+path;
	jQuery('<form action="' + url + '" method="post" target="_blank"></form>').appendTo('body').submit().remove();
}

/**
 * 回填时展示数据
 * @param contentId
 * @returns
 */
function init_showlist(fileId,contentId) {
	$("#"+fileId).hide();
	$("#"+fileId).after("<div id='"+fileId+"_list'></div>");
	var str=$("#"+contentId).val();
	if(str){
		var jsons=jQuery.parseJSON(str);
		for(var i=0;i<jsons.length;i++){
			addshowlist(fileId, jsons[i]);
		}
	}
}

/**
 * 添加上传列表
 * @param fileId
 * @param json
 * @returns
 */
function addshowlist(fileId,json){
	$("#"+fileId+"_list").append("<div class='file_list_show' ><i class='uploadifive_icon'></i><a href='#' onclick=\"downfile('"+json.appId+"','"+json.uid+"','"+json.name+"','"+json.saveDirect+"','"+json.suffix+"','"+json.save_path+"')\">"+json.originalFilename+"</a> </div>");
}