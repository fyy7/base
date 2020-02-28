var KWebOffice = {};

KWebOffice.init = function(){
	KWebOffice.openWebOffice = function(title,url,data,callback){
		if(data){
			$.each(data,function(key,value){
				url += "&"+key+"="+value;
			});
		}
		openWindow(title,url,2000,2000,callback);
	}
	
	KWebOffice.setGraySkin = function(WebOfficeObj){
	 //烟枪灰皮肤
	 //参数顺序依次为：控件标题栏颜色、自定义菜单开始颜色、自定义工具栏按钮开始颜色、自定义工具栏按钮结束颜色、
	 //自定义工具栏按钮边框颜色、自定义工具栏开始颜色、控件标题栏文本颜色（默认值为：0x000000）
	 if (!WebOfficeObj.WebSetSkin(0xdbdbdb, 0xeaeaea, 0xeaeaea, 0xdbdbdb, 0xdbdbdb, 0xdbdbdb, 0x000000))
	   WebOfficeObj.Alert(WebOfficeObj.Status);
	}
	
	KWebOffice.getAppUrl = function(){
		//请求获取文件时需要拼接完整的url，url地址若不同则需要另外实现
		var url = window.location.href;
		url = url.substr(0,url.indexOf("/do")+1);
		return url;
	}
	
	
	KWebOffice.saveFile = function(WebOfficeObj,params,callback){
		WebOfficeObj.WebSave2(params,callback);
	}
	
	KWebOffice.openFile = function(WebOfficeObj,uid,istemp,callback){
		WebOfficeObj.WebOpenBase64(uid,istemp,callback);
		//var url = WebOfficeObj.ServerUrl + "&KIND_WEBOFFICE_OPTION=LoadFile&UID="+uid + "&IS_TMP_FILE=" + istemp + "&FILETYPE="+WebOfficeObj.FileType;
		//WebOfficeObj.WebOpen3(url);
	}
}

$(function(){KWebOffice.init();});