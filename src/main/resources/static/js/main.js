var g_openModelWinNum = 0;
var g_jsonData = {};

var g_first_home_page_title = '首页';

var g_open_tab_num = 0;
var g_open_tab_max_num = 10;

$(window).resize(function() {

	// $("#id_iframe_home").height($(window).height() - $("#id_div_favorites_list").height() - 160);
	resizeEasyUiWindow();

	// for (var i = 1; i <= g_openModelWinNum; i++) {
	// if (!$("#g_win" + i).window('options').closed) {
	// // 有显示的才居中
	// $("#g_win" + i).window('center');
	// }
	// }

});

$(window).load(function() {
	hideWaitMsg();
	$("#id_menu_controll").show();
});

function toggleLayout(obj, val) {
	if ($(obj).html().indexOf('隐藏') > -1) {
		$('.easyui-layout').layout('collapse', val);
		$(obj).html($(obj).html().replace('隐藏', '显示'));
	} else {
		$('.easyui-layout').layout('expand', val);
		$(obj).html($(obj).html().replace('显示', '隐藏'));
	}
	return false;
}

function addTab(title, url) {
	// 去除空格、制表符
	title = title.replace(/^\s+/g, "").replace(/\s+$/g, "");
	if ($('#tabs').tabs('exists', title)) {
		$('#tabs').tabs('select', title);// 选中并刷新
	} else {
		if (g_open_tab_num >= g_open_tab_max_num) {
			showMsg("只能打开" + g_open_tab_max_num + "个页面数，请先关闭其它页面后再打开！");
			return false;
		}
		g_open_tab_num = g_open_tab_num + 1;
		var content = createFrame(url);

		var f_closable = true;
		if (title == g_first_home_page_title) {
			f_closable = false;
		}

		$('#tabs').tabs('add', {
			title : title,
			content : content,
			closable : f_closable
		});
		tabClose();
	}
	$('#tabs').tabs('resize');
}

/**
 * 根据url，创建tab
 * 
 * @param url 网页地址
 * @returns {String}
 */
function createFrame(url) {
	var s = '<iframe  frameborder="0" id="g_win_xx_' + Math.random().toString().replace("0.", "0") + '" src="' + url + '" style="display: block;width:100%;height:100%;"></iframe>';
	return s;
}

/**
 * 增加模块收藏
 * 
 * @param json_data
 */
function addTabFavorites(json_data) {
	showMsg(json_data.g_message);
	if (json_data.g_result == 1) {
		// 重载收藏菜单模块
		postSyncAjaxAndGetHtml("do", "action=html__tab_get_my_favorites&noHtmlHeadFlag=1", "getTabFavorites");
	}
}
/*
 * 加载左边菜单
 */
function getMyPowerMenu(json_data) {
	if (json_data.g_result == 1) {
		$("#id_resource_menu").html(json_data.back_html);
	}
	var skins = $('.li-skinitem span').click(function() {
		var $this = $(this);
		if ($this.hasClass('cs-skin-on')) {
			return;
		}
		skins.removeClass('cs-skin-on');
		$this.addClass('cs-skin-on');
		var skin = $this.attr('rel');
		// $('#swicth-style').attr('href', themes[skin]);
		setCookie('cs-skin', skin);
		skin == 'dark-hive' ? $('.cs-north-logo').css('color', '#FFFFFF') : $('.cs-north-logo').css('color', '#000000');
		// 改变已打开的窗口
		changeIframeThemes(window.top);
	});
}

/**
 * 获取当前待办
 * 
 * @param info
 */
function getTabTodo(info) {
	$("#id_div_todo").html(info);
	$.each($("#id_div_todo").find("div"), function() {
		$(this).hover(function() {
			$(this).find("span:first").show();
			// $(this).css("background", getJTreeJson().c1);
			// $(this).css("border", "1px solid");
		}, function() {
			$(this).find("span:first").hide();
			$(this).css("background", "");
			$(this).css("border", "");
		});
	});

}

/**
 * 获取当前收藏模块
 * 
 * @param info
 */
function getTabFavorites(info) {
	$("#id_div_favorites_list").html(info);
	$.each($("#id_div_favorites_list").find("div"), function() {
		$(this).hover(function() {
			$(this).find("span:first").show();
			$(this).css("background", getJTreeJson().c1);
			// $(this).css("border", "1px solid");
		}, function() {
			$(this).find("span:first").hide();
			$(this).css("background", "");
			$(this).css("border", "");
		});
	});
}
/**
 * 删除模块收藏
 * 
 * @param rlogo
 */
function delTabFavorites(rlogo) {
	postAjax("do", "action=tab_del_favorites&rlogo=" + rlogo, "delTabFavorites_return");
}

/**
 * 删除模块收藏回调函数
 * 
 * @param json_data
 */
function delTabFavorites_return(json_data) {
	showMsg(json_data.g_message);
	if (json_data.g_result == 1) {
		postSyncAjaxAndGetHtml("do", "action=html__tab_get_my_todo&noHtmlHeadFlag=1", "getTabTodo");
	}
}

/**
 * 根据标题，关闭一个tab
 * 
 * @param title
 */
function closeOneTab(title) {
	$('#tabs').tabs('select', title);// 选中并刷新
	var currTab = $('#tabs').tabs('getSelected');
	$(currTab.panel('options').content).attr('src', "about:blank");
	$('#tabs').tabs('close', title);
}

/**
 * 关闭tab
 */
function tabClose() {
	/* 双击关闭TAB选项卡 */
	$(".tabs-inner").dblclick(function() {
		// var subtitle = $(this).children(".tabs-closable").text();
		// closeOneTab(subtitle);
	});

	/* 为选项卡绑定右键 */
	$(".tabs-inner").bind('contextmenu', function(e) {
		var subtitle = $(this).children(".tabs-closable").text();
		if (subtitle == "") {
			subtitle = g_first_home_page_title;
		}

		if (subtitle == "" || subtitle == g_first_home_page_title) {
			// 首页的
			$('#mm2').menu('show', {
				left : e.pageX,
				top : e.pageY
			});
			$('#mm2').data("currtab", subtitle);
		} else {
			$('#mm').menu('show', {
				left : e.pageX,
				top : e.pageY
			});
			$('#mm').data("currtab", subtitle);
		}

		$('#tabs').tabs('select', subtitle);
		return false;
	});
}

/**
 * 绑定右键菜单事件
 */
function tabCloseEven() {
	// 收藏
	$('#mm-tab_favorites').click(function() {
		var currTab = $('#tabs').tabs('getSelected');
		var url = $(currTab.panel('options').content).attr('src');
		if (url != undefined && currTab.panel('options').title != g_first_home_page_title) {
			var url = $(currTab.panel('options').content).attr('src');
			if (url.indexOf("RLOGO") > -1) {
				var rlogo = url.split("RLOGO=")[1];
				postAjax("do", "action=tab_add_favorites&rlogo=" + rlogo, "addTabFavorites");
			}
		}
	});

	// 刷新
	$('#mm-tabupdate').click(function() {
		var currTab = $('#tabs').tabs('getSelected');
		var url = $(currTab.panel('options').content).attr('src');
		// if (url != undefined && currTab.panel('options').title != g_first_home_page_title) {
		if (url != undefined) {
			$('#tabs').tabs('update', {
				tab : currTab,
				options : {
					content : createFrame(url)
				}
			});
		}
	});
	// 刷新
	$('#mm2-tabupdate').click(function() {
		$('#mm-tabupdate').click();
	});

	// 关闭除当前之外的TAB
	$('#mm2-tabcloseother').click(function() {
		$('#mm-tabcloseother').click();
	});

	// 关闭当前
	$('#mm-tabclose').click(function() {
		var currtab_title = $('#mm').data("currtab");
		closeOneTab(currtab_title);
	});

	// 全部关闭
	$('#mm-tabcloseall').click(function() {
		$('.tabs-inner span').each(function(i, n) {
			var t = $(n).text();
			if (t != g_first_home_page_title) {
				closeOneTab(t);
			}
		});
	});

	// 关闭除当前之外的TAB
	$('#mm-tabcloseother').click(function() {
		var prevall = $('.tabs-selected').prevAll();
		var nextall = $('.tabs-selected').nextAll();
		if (prevall.length > 0) {
			prevall.each(function(i, n) {
				var t = $('a:eq(0) span', $(n)).text();
				if (t != g_first_home_page_title) {
					closeOneTab(t);
				}
			});
		}

		if (nextall.length > 0) {
			nextall.each(function(i, n) {
				var t = $('a:eq(0) span', $(n)).text();
				if (t != g_first_home_page_title) {
					closeOneTab(t);
				}
			});
		}
		return false;
	});

	// 关闭当前右侧的TAB
	$('#mm-tabcloseright').click(function() {
		var nextall = $('.tabs-selected').nextAll();
		if (nextall.length == 0) {
			// msgShow('系统提示','后边没有啦~~','error');
			alert('后边没有啦~~');
			return false;
		}

		nextall.each(function(i, n) {
			var t = $('a:eq(0) span', $(n)).text();
			closeOneTab(t);
		});

		return false;
	});

	// 关闭当前左侧的TAB
	$('#mm-tabcloseleft').click(function() {
		var prevall = $('.tabs-selected').prevAll();
		if (prevall.length == 0) {
			showMsg('到头了，前边没有啦~~');
			return false;
		}

		prevall.each(function(i, n) {
			var t = $('a:eq(0) span', $(n)).text();
			closeOneTab(t);
		});

		return false;
	});

	// 退出
	$("#mm-exit").click(function() {
		$('#mm').menu('hide');
	});
}

/**
 * 切换皮肤
 * 
 * @param theme_id 皮肤ID
 */
function changeThemeFun(theme_id) {
	$('.li-skinitem span[rel="' + theme_id + '"]').trigger("click");
	postAjax("do", "action=sys_cs_skin&CS_SKIN=" + theme_id, "");
}

/**
 * 皮肤主题库
 */
var themes = {
	'gray' : 'js/themes/gray/easyui.css',
	'black' : 'js/themes/black/easyui.css',
	'bootstrap' : 'js/themes/bootstrap/easyui.css',
	'default' : 'js/themes/default/easyui.css',
	'metro' : 'js/themes/metro/easyui.css',
	'metro-blue' : 'js/themes/metro-blue/easyui.css',
	'metro-gray' : 'js/themes/metro-gray/easyui.css',
	'metro-green' : 'js/themes/metro-green/easyui.css',
	'metro-orange' : 'js/themes/metro-orange/easyui.css',
	'metro-red' : 'js/themes/metro-red/easyui.css',
	'ui-pepper-grinder' : 'js/themes/ui-pepper-grinder/easyui.css',
	'ui-cupertino' : 'js/themes/ui-cupertino/easyui.css',
	'ui-dark-hive' : 'js/themes/ui-dark-hive/easyui.css',
	'ui-sunny' : 'js/themes/ui-sunny/easyui.css'
};

/*
 * 切换字体大小
 */
function changeFontSize(s_size) {
	setCookie('cs-font_size', s_size);
	$("body").css("font-size", s_size + "px");
	$.each((window.top).$("iframe"), function() {
		if ($(this).get(0).contentWindow.document.getElementById('swicth-style')) {
			var w_t = $(this).get(0).contentWindow;
			w_t.changeFontSize(s_size);
		}
	});
}

/**
 * 切换主题皮肤
 * 
 * @param frameObj
 */
function changeIframeThemes(frameObj) {
	$.each(frameObj.$("iframe"), function() {
		// try {
		if ($(this).get(0).contentWindow.document.getElementById('swicth-style')) {
			var w_t = $(this).get(0).contentWindow;
			w_t.changeThemes();
			if (w_t.g_c_openModelWinNum && w_t.g_c_openModelWinNum > 0) {
				for (var i = 1; i <= w_t.g_c_openModelWinNum; i++) {
					w_t.$("#g_c_win_iframe" + i)[0].contentWindow.changeThemes();
				}
			}
		}
		// } catch (err) {
		// showMsg(err.description);
		// }
	});

	// alert(getCookie('cs-skin'));
	$("#id_logo_main").css("background", "url(images/logo/" + getCookie('cs-skin') + "/logo_main.jpg) no-repeat");
	$("#id_logo_child").css("background", "url(images/logo/" + getCookie('cs-skin') + "/logo_child.jpg) repeat-x");
	chgColor(1);
	$(".topnav li a[treeSelected='1']").css("background-color", getJTreeJson().c2);
	addSelectSkinString();
}

$(function() {
	if (i_model == "1") {
		// postSyncAjax("do", "action=get_model_power_menu&fd_uuid=" + fd_uuid, "getMyPowerMenu");
	} else {
		// postSyncAjax("restful", "action=get_my_power_menu", "getMyPowerMenu");
		// postSyncAjaxAndGetHtml("restful", "action=html__tab_get_my_todo&noHtmlHeadFlag=1", "getTabTodo");
		var appId = $("#appid").val();
		if (!appId) {
			appId = "";
		}
		//if (typeof get_my_power_menu == 'undefined') {
		//	postSyncAjax("restful", "action=get_my_power_menu&APP_ID=" + appId, "getMyPowerMenu");
		//} else {
		postSyncAjax("restful", "action=" + get_my_power_menu + "&APP_ID=" + appId, "getMyPowerMenu");
		//}
	}

	showWaitMsg("页面加载中。。。");
	if (getCookie('cs-font_size')) {
		$("body").css("font-size", getCookie('cs-font_size') + "px");
	}

	tabCloseEven();
	$('.cs-navi-tab').click(function() {
		var $this = $(this);
		var href = $this.attr('src');
		var title = $this.text();
		addTab(title, href);
	});

	if (getCookie('cs-skin')) {
		// var skin = getCookie('cs-skin');
		// $('#swicth-style').attr('href', themes[skin]);
		// $this = $('.li-skinitem span[rel=' + skin + ']');
		// $this.addClass('cs-skin-on');
		// skin == 'dark-hive' ? $('.cs-north-logo').css('color', '#FFFFFF') : $('.cs-north-logo').css('color', '#000000');
	}

	$("#id_logout").click(function() {
		$.messager.confirm('提示', '确认退出?', function(r) {
			if (r) {
				$('#form_logout').submit();
			}
		});
	});
	addSelectSkinString();
	$('#tabs').tabs({
		onBeforeClose : function(title, index) {
			g_open_tab_num = g_open_tab_num - 1;
		}
	});

	$('#tabs').find("li.tabs-first").click(function() {
		// showWaitMsg();
		// setTimeout(function(){
		// postSyncAjaxAndGetHtml("do", "action=html__tab_get_my_todo&noHtmlHeadFlag=1", "getTabTodo");
		// }, 1000);
	});

});

/**
 * 添加当前选择主题文字
 */
function addSelectSkinString() {
	$.each($(".menu").find(".menu-item"), function() {
		if ($(this).attr("onclick")) {
			if ($(this).attr("onclick").indexOf("'" + getCookie('cs-skin') + "'") > -1) {
				$(this).find("div:first").append("--[当前主题]");
			} else {
				if ($(this).find("div:first").html().indexOf("--[当前主题]") > -1) {
					$(this).find("div:first").html($(this).find("div:first").html().replace("--[当前主题]", ""));
				}
			}
		}
	});
}