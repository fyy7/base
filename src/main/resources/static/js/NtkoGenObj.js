// 请勿修改，否则可能出错
var Sys = {};
var ua = navigator.userAgent.toLowerCase();
var s;
(s = ua.match(/msie ([\d.]+)/)) ? Sys.ie = s[1] : (s = ua.match(/firefox\/([\d.]+)/)) ? Sys.firefox = s[1] : (s = ua.match(/chrome\/([\d.]+)/)) ? Sys.chrome = s[1] : (s = ua.match(/opera.([\d.]+)/)) ? Sys.opera = s[1] : (s = ua.match(/version\/([\d.]+).*safari/)) ? Sys.safari = s[1] : 0;
//var classid = '01DFB4B4-0E07-4e3f-8B7A-98FD6BFF153F';
//var classversion = '5,0,2,4';

var classid = 'C9BC4DFF-4248-4a3c-8A49-63A7D317F404';
var classversion = '5,0,1,6';

var OFFICE_CONTROL_OBJ_ID = 'OFFICE_CONTROL_OBJ';
function isIE() { //ie?  
    if (!!window.ActiveXObject || "ActiveXObject" in window)  
        return true;  
    else  
        return false;  
}  
function generateNtkoObj(preId, cabPath) {
	var objId = OFFICE_CONTROL_OBJ_ID;
	if (arguments.length > 0) {
		objId = preId;
	}
	var ntkoObj = '';

	if (Sys.ie||isIE()) {
		ntkoObj += '<object id="' + objId + '" classid="clsid:' + classid + '"   ';
		ntkoObj += 'codebase='+cabPath+'/OfficeControl5016.cab#version=' + classversion + '" width=100% height=100% >';
	    ntkoObj += '<param name="BorderColor" value="14402205">   ';
	    ntkoObj += '<param name="TitlebarColor" value="15658734">   ';
	    ntkoObj += '<param name="TitlebarTextColor" value="0">   ';
	    ntkoObj += '<param name="MenubarColor" value="14402205">   ';
	    ntkoObj += '<param name="MenuButtonColor" VALUE="16180947">   ';
	    ntkoObj += '<param name="MenuBarStyle" value="3">   ';
	    ntkoObj += '<param name="MenuButtonStyle" value="7">   ';
	    ntkoObj += '<param name="MakerCaption" value="福建省凯特科技有限公司">';
	    ntkoObj += '<param name="MakerKey" value="F0C8932235620D61DD3D482CBC92DBC9019963EE">';
		ntkoObj += '<param name="ProductCaption" value="福建省电子信息（集团）有限责任公司">';
		ntkoObj += '<param name="ProductKey" value="3FC64AB9A307054860EB4168E0B532B839407201"> ';
		ntkoObj += '<param name="EkeyType" value="1">   ';
		ntkoObj += '<param name="Titlebar" value="0">   ';
		ntkoObj += '<SPAN STYLE="color:red">不能装载NTKO 文档控件。请在检查浏览器的选项中检查浏览器的安全设置。</SPAN>   ';
		ntkoObj += '</object>';
	} else if (Sys.firefox) {
		ntkoObj += '<object id="' + objId + '" type="application/ntko-plug" codebase="ofctnewclsid.cab#version=' + classversion + '" width=800 height=500 ';
		ntkoObj += 'clsid="{' + classid + '}" ';
		ntkoObj += '_BackColor="16744576" ';
		ntkoObj += '_ForeColor="16777215" ';
		ntkoObj += '_EkeyType="1"> ';
		ntkoObj += '<SPAN STYLE="color:red">尚未安装NTKO Web FireFox跨浏览器插件。请点击<a href='+cabPath+'>安装组件</a></SPAN>   ';
		ntkoObj += '</object>   ';
	} else if (Sys.chrome) {
		ntkoObj += '<object id="' + objId + '" type="application/ntko-plug"  codebase="officeControl.cab#version=' + classversion + '" width=800 height=500 ';
		ntkoObj += 'clsid="{' + classid + '}" ';
		ntkoObj += '_BackColor="16744576" ';
		ntkoObj += '_ForeColor="16777215" ';
		ntkoObj += '_EkeyType="1"> ';
		ntkoObj += '<SPAN STYLE="color:red">尚未安装NTKO Web Chrome跨浏览器插件。请点击<a href="ntko.crx">安装组件</a></SPAN>   ';
		ntkoObj += '</object>   ';
	} else if (Sys.opera) {
		ntkoObj += "sorry,ntko 跨浏览器暂时不支持opera!";
	} else if (Sys.safari) {
		ntkoObj += "sorry,ntko 跨浏览器暂时不支持safari!";
	} else {
		ntkoObj += "sorry,ntko 跨浏览器暂时不支持此浏览器!";
	}
	return ntkoObj;
}