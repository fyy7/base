var j_tree_color = {};
j_tree_color.black = {
	"c1" : "#F0E1E3",
	"c2" : "#F0E1E3"
};
j_tree_color.bootstrap = {
	"c1" : "#F2F2F2",
	"c2" : "#F2F2F2"
};
j_tree_color.default1 = {
	"c1" : "#E0ECFF",
	"c2" : "#E0ECFF"
};
j_tree_color.gray = {
	"c1" : "#F3F3F3",
	"c2" : "#F3F3F3"
};
j_tree_color.metro = {
	"c1" : "#F1F1F1",
	"c2" : "#F1F1F1"
};
j_tree_color.metro_blue = {
	"c1" : "#DAEEF5",
	"c2" : "#DAEEF5"
};
j_tree_color.metro_gray = {
	"c1" : "#C7D1CC",
	"c2" : "#C7D1CC"
};
j_tree_color.metro_green = {
	"c1" : "#E5F0C9",
	"c2" : "#E5F0C9"
};
j_tree_color.metro_orange = {
	"c1" : "#F0E3BF",
	"c2" : "#F0E3BF"
};
j_tree_color.metro_red = {
	"c1" : "#F0E1E3",
	"c2" : "#F0E1E3"
};
j_tree_color.ui_pepper_grinder = {
	"c1" : "#F4F4F4",
	"c2" : "#F4F4F4"
};
j_tree_color.ui_sunny = {
	"c1" : "#9D9687",
	"c2" : "#9D9687"
};
j_tree_color.ui_cupertino = {
	"c1" : "#E6F1F9",
	"c2" : "#E6F1F9"
};
j_tree_color.ui_dark_hive = {
	"c1" : "#E6F1F9",
	"c2" : "#E6F1F9"
};

(function($) {
	$.fn.extend({
		accordion : function(options) {
			var defaults = {
				accordion : 'true',
				speed : 200,
				closedSign : '<a class="arrows_up" href="javascript:void(0)"></a>',
				openedSign : '<a class="arrows_down" href="javascript:void(0)"></a>'
			};
			var opts = $.extend(defaults, options);
			var $this = $(this);
			
			//默认第一个菜单为打开状态
			$this.find("li").each(function() {
				if ($(this).find("ul").size() != 0) {
					$(this).find("a:first").prepend("<span>" + opts.closedSign + "</span>");
					if ($(this).find("a:first").attr('href') == "#") {
						$(this).find("a:first").click(function() {
							return false;
						});
					}
				}
			});
			
			//设置菜单皮肤
			var s_color = null;
			
			if(getCookie('cs-skin')){
				var s_str="s_color=j_tree_color." + getCookie('cs-skin').replace(/-/g, "_").replace(/default/g, 'default1');
				eval(s_str);
			}else{
				s_color=j_tree_color.default1;
			}
			
			//eval("s_color=j_tree_color." + getCookie('cs-skin').replace(/-/g, "_").replace(/default/g, 'default1'));

			//
			var i =1;
			$("#id_li_active").find("ul").each(function() {
				if(i<=1){
					$(this).slideDown(opts.speed);
					$(this).parent("li").find("span:first").html(opts.openedSign);
					$(this).parent("li").find("a:first").css("background-color", getJTreeJson().c2);// "#B848FF"
					$(this).parent("li").find("a:first").attr("treeSelected", "1");
					i++;
				}
				
			});
			
			$this.find("li a").click(function() {
				if ($(this).parent().find("ul").size() != 0) {
					if (opts.accordion) {
						if (!$(this).parent().find("ul").is(':visible')) {
							parents = $(this).parent().parents("ul");
							visible = $this.find("ul:visible");
							visible.each(function(visibleIndex) {
								var close = true;
								parents.each(function(parentIndex) {
									if (parents[parentIndex] == visible[visibleIndex]) {
										close = false;
										return false;
									}
								});
								if (close) {
									if ($(this).parent().find("ul") != visible[visibleIndex]) {
										$(visible[visibleIndex]).slideUp(opts.speed, function() {
											$(this).parent("li").find("span:first").html(opts.closedSign);
										});

									}
								}
							});
						}
					}
					if ($(this).parent().find("ul:first").is(":visible")) {
						// 隐藏的
						$(this).parent().find("ul:first").slideUp(opts.speed, function() {
							$(this).parent("li").find("span:first").delay(opts.speed).html(opts.closedSign);
						});
						$(this).css("background-color", getJTreeJson().c2);// "#B848FF"
						$(this).attr("treeSelected", "1");
					} else {
						// 显示的
						$(this).parent().find("ul:first").slideDown(opts.speed, function() {
							$(this).parent("li").find("span:first").delay(opts.speed).html(opts.openedSign);
						});
						$(this).css("background-color", getJTreeJson().c2);// "#B848FF"
						$(this).attr("treeSelected", "1");
						// 隐藏，收起其它节点的
						var allordidx = $(this).attr("allordidx");
						$this.find("li a").each(function() {
							if ($(this).parent().find("ul").size() != 0) {
								if (allordidx.indexOf($(this).attr("allordidx")) != 0 && $(this).attr("allordidx").indexOf(allordidx) != 0) {
									$(this).parent().find("ul:first").slideUp(opts.speed, function() {
										$(this).parent("li").find("span:first").delay(opts.speed).html(opts.closedSign);
									});
									$(this).css("background-color", getJTreeJson().c1);// "#B848FF"
									$(this).removeAttr("treeSelected", "2");
								}
							}
						});
					}
				}
			});
		}
	});
})(jQuery);

$(document).ready(function() {
	chgColor(1);
	$(".topnav").accordion({
		accordion : false,
		speed : 200,
		closedSign : '<a class="arrows_up" href="javascript:void(0)"></a>',
		openedSign : '<a class="arrows_down" href="javascript:void(0)"></a>'
	});
});

/**
 * 变颜色
 * 
 * @param num
 */
function chgColor(num) {
	if (num == 1) {
		$(".parent_tree").css("background-color", getJTreeJson().c1);// #F0E1E3
	} else {
		$(".parent_tree").css("background-color", getJTreeJson().c2);// #F0E1E3
	}
}
/**
 * 获取树json
 * 
 * @returns
 */
function getJTreeJson() {
	var s_color = null;
	if(getCookie('cs-skin')){
		var s_str="s_color=j_tree_color." + getCookie('cs-skin').replace(/-/g, "_").replace(/default/g, 'default1');
		eval(s_str);
	}else{
		s_color=j_tree_color.default1;
	}
	
	return s_color;
}
