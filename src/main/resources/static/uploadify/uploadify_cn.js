/**
 * 上传的文件个数
 */
var g_uploadifyFileNum=0;

/**
 * 关联ID
 */
var attachids= [];

/**
 * 上传时的回调函数
 */
var g_uploadify_func = "";
function init_showlist(s_id) {
	$.each($("textarea[id^='"+s_id+"'],input[id^='"+s_id+"']"), function() {
		$(this).after("<DIV  id='"+$(this).attr("ID")+"_list'></DIV>");
			var t_id=$(this).attr("id");
			var str=$("#"+t_id).val();
			if(str){
				var jsons=jQuery.parseJSON(str);
				for(var i=0;i<jsons.length;i++){
					addshowlist(t_id, jsons[i]);
				}

		}
	});
}

function init_showlist(s_id,t_id) {
	$("#"+s_id).hide();
	$.each($("textarea[id^='"+s_id+"'],input[id^='"+s_id+"']"), function() {
		$(this).after("<DIV  id='"+$(this).attr("ID")+"_list'></DIV>");
			var f_id=$(this).attr("id");
			var str=$("#"+t_id).val();
			if(str){
				var jsons=jQuery.parseJSON(str);
				
				for(var i=0;i<jsons.length;i++){
					
					addshowlist(f_id, jsons[i]);
				}

		}
	});
}


function init_showimglist(s_id,t_id) {
	$("#"+s_id).hide();
	$.each($("textarea[id^='"+s_id+"'],input[id^='"+s_id+"']"), function() {
		$(this).after("<DIV  id='"+$(this).attr("ID")+"_list'></DIV>");
			var f_id=$(this).attr("id");
			var str=$("#"+t_id).val();
			if(str){
				
				var jsons=jQuery.parseJSON(str);
				addimgshowlist(f_id, jsons);
			}
	});
}

/**
 * 初始化上传控件
 * 
 * @param s_id
 *            需要变成上传控件的id,以要s_id开头的都将被初始化
 * @param s_upload_url
 *   		  上传至服务器的地址
 * @param b_func
 *          上传完成时调用的方法
 */
function init_uploadFile(s_id,s_upload_url,s_type) {
	$.each($("input[id^='"+s_id+"']"), function() {
		var i_uploadLimit = 10;//最大文件个数
		if ($(this).attr("uploadLimit") != undefined) {
			i_uploadLimit = parseInt($(this).attr("uploadLimit"));
		}

		$(this).before("<DIV  id='"+$(this).attr("ID")+"_list'></DIV>"); 
		var s_fileReturnContentId="";
		if ($(this).attr("uploadFileReturnContentId") != undefined) {
			s_fileReturnContentId=$(this).attr("uploadFileReturnContentId");
		}
		var s_fileRelationId="";
		if ($(this).attr("uploadFileReturnRelationId") != undefined) {
			s_fileRelationId=$(this).attr("uploadFileReturnRelationId");
		}
		var buttontext="选择图片";
		if ($(this).attr("buttonWord") != undefined) {
			buttontext=$(this).attr("buttonWord");
		}
		var str="$('#"+$(this).attr("id")+"').uploadify({"+
			"'swf' : '../uploadify/uploadify.swf',"+
			"'uploader' : '"+s_upload_url+"',"+
		//	"'uploadLimit' : "+i_uploadLimit+","+   //不建议使用，或者建议个个较大的值，避免删除重传操作异常
			"'onSWFReady' : function() {";
			
			
			if(!s_type){
				str=str+"inishowlist('"+$(this).attr("ID")+"', '"+s_fileReturnContentId+"','"+s_fileRelationId+"','"+i_uploadLimit+"','"+s_type+"');"+
				"},";
				str=str+"'buttonText' : '选择文件',";
			} else 	if(s_type=="img"){
				str=str+"inishowimglist('"+$(this).attr("ID")+"', '"+s_fileReturnContentId+"','"+s_fileRelationId+"','"+i_uploadLimit+"','"+s_type+"');"+
				"},";
				str=str+"'buttonText' : '"+buttontext+"',";
			}
			
		
		if ($(this).attr("fileSizeLimit") != undefined) {
			str+="'fileSizeLimit':'"+$(this).attr("fileSizeLimit")+"',";
		}
		if ($(this).attr("fileTypeDesc") != undefined && $(this).attr("fileTypeExts") != undefined ) {
			str+="'fileTypeDesc':'"+$(this).attr("fileTypeDesc")+"',";
			str+="'fileTypeExts':'"+$(this).attr("fileTypeExts")+"',";
		}
		if ($(this).attr("formData") != undefined ) {
			str+="'formData':"+$(this).attr("formData")+",";
		}
		var successReturnFunc ="";
		if(!s_type){
			successReturnFunc = "handleReturnData";
		} else 	if(s_type=="img"){
			successReturnFunc="handleReturnDataImage";
		}
		//如果有特殊事件则执行特殊事件
		if ($(this).attr("successReturnFunc") != undefined) {
			successReturnFunc=$(this).attr("successReturnFunc");
			
		}

		str+="'auto' : true," +
			// 多个文件上传， 避免无法控制数量设置为不能多选
			"'multi' : false,"+
			"'width' : 82,"+
			"'successTimeout':60,"+
			"'removeTimeout':0,"+
			"'removeCompleted':true,"+
			"'height' : 34,"+
			"'onUploadError':function(file,errorCode, errorMsg, errorString){;" +
			" if('IO Error'==errorString){"+ //分段删除可能导致后续上传被终止
			" showMsg('上传文件超出后台限制！终止上传！');"+
			"$('#"+$(this).attr("id")+"').uploadify('disable', false); "+
			"}" +
			"}," +
			
			"'onUploadSuccess' : function(file, data, response) {"+
			" if(jQuery.parseJSON(data).err_msg){"+
			" showMsg(jQuery.parseJSON(data).err_msg);"+
			"$('#"+$(this).attr("id")+"').uploadify('disable', false); "+
			" return false; "+
			" }"+
			"	if ('"+s_fileReturnContentId+"'!= '') {"+
			"		"+successReturnFunc+"('"+$(this).attr("id")+"','"+s_fileReturnContentId+"','"+s_fileRelationId+"',data);"+
			"	}else{"+
		//	"		"+successReturnFunc+"('"+$(this).attr("id")+"','',data);"+
			"}"+
			"	handleUploadSuccess(file, data, response);"+
	
			"	if ("+i_uploadLimit+" <= $('#"+$(this).attr("ID")+"_list').children('.file_list').length) {"+
			"		$('#"+$(this).attr("id")+"').uploadify('disable', true);"+
			"	}"+
			"},"+
			"'onQueueComplete':function(queueData){"+//用于判断是否都已传完
			"},"+
			"'onUploadStart':function(file){"+
			
		   // "this.cancelUpload(file.id); "+
		   // "$('#' + file.id).remove(); "+
			//"	alert(123);"+

			"},"+
			"'onClearQueue':function(){"+
	
			"},"+
			"'onUploadProgress':function(file, bytesUploaded, bytesTotal, totalBytesUploaded, totalBytesTotal){"+
			"},"+
			
			"'onSelect':function(file){"+	
			"	if ("+i_uploadLimit+" == 1) {"+
			"	$('#"+$(this).attr("id")+"').uploadify('disable', true);"+	
			"	}"+
		
			"},"+
			"'onSelectError':function(file, errorCode, errorMsg){"+
			"	$('#"+$(this).attr("id")+"').uploadify('disable', false);"+	
			"},"+
			"'onFallback' : function() {"+
			"	alert('您当前的浏览器不支持flash.');"+
			"},"+
			"'onCancel' : function(file) {"+
				// alert('The file ' + file.name + ' was cancelled.');
			"	if (i_uploadLimit == 1) {"+
			"		$('#"+$(this).attr("id")+"').uploadify('disable', false);"+
			"	}"+
			"}"+
		"});";
		eval(str);
		
	});
	$('.uploadify-button').css('display', 'inline');
	$('.uploadify').css('display', 'inline');
	$('.uploadify-queue').css('max-height', '250px');
	$('.uploadify-queue').css('overflow', 'auto');
}

/**
 * 开始上传
 * 
 * @param s_id
 * @param s_func
 *            上传后回调函数
 */
function start_uploadFile(s_id, s_func) {
	var b_queue = false;
	$.each($("div .uploadify"), function() {
		if ($(this).attr('id').indexOf(s_id) == 0) {
			// 队列文件大小
			g_uploadifyFileNum += parseInt($("#" + $(this).attr('id')).data('uploadify').queueData.queueLength);
			b_queue = true;
			//$("#" + $(this).attr('id')).uploadify('disable', true);
			$("#" + $(this).attr('id')).uploadify('upload', '*');
		}
	});
	g_uploadify_func = s_func;
	if (!b_queue) {
		// 没有文件需要上传的
		eval(s_func + "()");
	}
}

/**
 * 上传成功后回调函数
 * 
 * @param file
 * @param data
 * @param response
 */
function handleUploadSuccess(file, data, response) {
	g_uploadifyFileNum--;
	if (g_uploadifyFileNum == 0) {
		eval(g_uploadify_func + "()");
	}
}

/**
 * 处理上传后服务器返回的数据
 * 
 * @param s_id
 * @param data
 */
function handleReturnData(f_id,s_id,r_id, data) {

	if(data){
		if(data.err_msg){
			showMsg(json.err_msg);
		$("#"+f_id).uploadify('disable', true)
			return false;
		}
		var arr_data = [];
		var json=jQuery.parseJSON(data) 
		json.files[0]["FLAG"]="N";
		if ($("#" + s_id).val() != "") {
			eval("arr_data=" + $("#" + s_id).val());
		} 
		addfilelist(f_id,s_id,r_id, json.files[0]);
		arr_data.push(json.files[0]);
		attachids.push(json.files[0].UID);
		$("#"+r_id).val(attachids);
		saveToId(s_id,arr_data);
	}
}

//添加上传列表
function addfilelist(f_id,v_id,r_id, json){
	$("#"+f_id+"_list").append("<div class='file_list' ><i class='uploadify_icon'></i><a href='#' onclick=\"downfile('"+json.APP_ID+"','"+json.UID+"','"+json.FILE_NAME+"','"+json.IS_TMP_FILE+"','"+json.FILE_SUFFIX+"')\">"+json.FILE_NAME+"."+json.FILE_SUFFIX+"</a>  <a class='del_button' onclick='delfile(\""+f_id+"\",\""+v_id+"\",\""+r_id+"\",this)' filenew='"+json.UID+"'></a></div>");
}
//添加上传列表
function addshowlist(f_id, json){
	$("#"+f_id+"_list").append("<div class='file_list_show' ><i class='uploadify_icon'></i><a href='#' onclick=\"downfile('"+json.APP_ID+"','"+json.UID+"','"+json.FILE_NAME+"','"+json.IS_TMP_FILE+"','"+json.FILE_SUFFIX+"')\">"+json.FILE_NAME+"."+json.FILE_SUFFIX+"</a> </div>");
}

//添加上传列表
function addimgshowlist(f_id, json){
	if($("#"+f_id+"_img").length>0){
        var url="download?APP_ID="+json.APP_ID+"&UID="+json.UID+"&IS_TMP_FILE="+json.IS_TMP_FILE+"&FILE_SUFFIX="+json.FILE_SUFFIX;
        var onclickstr="onclick=\"downfile('"+json.APP_ID+"','"+json.UID+"','"+json.FILE_NAME+"','"+json.IS_TMP_FILE+"','"+json.FILE_SUFFIX+"')\"";
        $("#"+f_id+"_img").html('<img id="img_upload" alt="" src="" style="width:340px;height:200px;border:1px solid #000;" '+onclickstr+'>');
        $("#"+f_id+"_img").find("img").attr("src",url);
    }
}

//通用下载页面
function downfile(appId,uid,filename,isTmpFile,filesuffix){
	
	var url="download?APP_ID="+appId+"&UID="+uid+"&IS_TMP_FILE="+isTmpFile+"&FILE_SUFFIX="+filesuffix;

	 jQuery('<form action="' + url + '" method="post" target="_blank"></form>').appendTo('body').submit().remove();
}
//添加删除上传列表,及其对应的数据
function delfile(f_id,s_id,r_id,obj){
	var arr_data = [];
	var kk = $("#"+f_id).data('uploadify');

	if(kk.settings.multi==false){
		 $("#"+f_id).uploadify('disable', false);
	}
	
	if ($("#" + s_id).val() != "") {
		eval("arr_data=" + $("#" + s_id).val());
	} 
	var tmp_json=eval("([])");

	for(var i=0;i<arr_data.length;i++){
		if(arr_data[i].UID){
			if(arr_data[i].UID==$(obj).attr("filenew")){
				arr_data[i]["FLAG"]="D";
				tmp_json.push(arr_data[i]);
				$(obj).parents(".file_list").delay(200).fadeOut(500, function() {
					$(this).remove();
				});
				attachids.splice(attachids.indexOf( arr_data[i].UID ), 1);
				$("#"+r_id).val(attachids);
			}else{
				tmp_json.push(arr_data[i]);
			}
		}
	
	}
	arr_data=tmp_json;
	saveToId(s_id,arr_data);
	
	if($("#"+f_id+"_img").length>0){
		$("#"+f_id+"_img").html("未上传图片");
	}
 }

function saveToId(s_id,arr_data){
	if(arr_data.length>0){
		$("#" + s_id).val(JSON.stringify(arr_data));
	}else{
		$("#" + s_id).val("");
	}
}
//页面初始化时展示已传数据
function inishowlist(f_id,t_id,r_id,num,s_type){
	
	if(t_id){
		var str=$("#"+t_id).val();
		var tmp=$("#"+f_id+"_list").html(); //避免切换页面导致重复调用
		if(str&&!tmp){
			var jsons=jQuery.parseJSON(str);
			if(num<=jsons.length){
				$('#'+f_id).uploadify('disable', true);
			}
			for(var i=0;i<jsons.length;i++){
				addfilelist(f_id, t_id, r_id, jsons[i]);
				attachids.push(jsons[i].UID);
				$("#"+r_id).val(attachids);
			}
		}
	}
}

//页面初始化时展示已传数据-img
function inishowimglist(f_id,t_id,r_id,num,s_type){
	
	if(t_id){
		var str=$("#"+t_id).val();
		var tmp=$("#"+f_id+"_list").html(); //避免切换页面导致重复调用
		if(str&&!tmp){
			var jsons=jQuery.parseJSON(str);
			if(num<=jsons.length){
				$('#'+f_id).uploadify('disable', true);
			}
			var imgattachids=[];
			addImagelist(f_id, t_id,r_id, jsons[0]);
			imgattachids.push(jsons[0].UID);
			$("#"+r_id).val(imgattachids);
			
		}
	}
}


/**
 * 处理上传后服务器返回的数据
 * 
 * @param s_id
 * @param data
 */
function handleReturnDataImage(f_id,s_id,r_id, data) {
	
	if(data){
		if(data.err_msg){
			showMsg(json.err_msg);
		$("#"+f_id).uploadify('disable', true)
			return false;
		}
		var imgattachids=[];
		var imgarr_data = [];
		var json=jQuery.parseJSON(data) 
		json.files[0]["FLAG"]="N";
		addImagelist(f_id,s_id,r_id, json.files[0]);
		imgarr_data.push(json.files[0]);
		imgattachids.push(json.files[0].UID);
		$("#"+r_id).val(imgattachids);
		saveToId(s_id,imgarr_data);
	}
	
}

function addImagelist(f_id,s_id,r_id, json){
    if($("#"+f_id+"_img").length>0){
        var url="download?APP_ID="+json.APP_ID+"&UID="+json.UID+"&IS_TMP_FILE="+json.IS_TMP_FILE+"&FILE_SUFFIX="+json.FILE_SUFFIX;
        var onclickstr="onclick=\"downfile('"+json.APP_ID+"','"+json.UID+"','"+json.FILE_NAME+"','"+json.IS_TMP_FILE+"','"+json.FILE_SUFFIX+"')\"";
        $("#"+f_id+"_img").html('<img id="img_upload" alt="" src="" style="width:340px;height:200px;border:1px solid #000;" '+onclickstr+'>');
        $("#"+f_id+"_img").find("img").attr("src",url);
    }
}