(function($) {
	function init(target) {
		var s_title = "请点击操作!";
		var opt = $.data(target, "plugintext").options;
		// $(target).addClass("easyui-validatebox");

		// var box = $("<span class='combo'></span>");
		// box.css({ "width": (opt.width - 2) + "px", "height": "20px", "line-height": "20px" });
		if (opt.title) {
			s_title = opt.title;
		}

		// var span = $("<span><span title='" + s_title + "' class='searchbox-button " + opt.iconCls + "'></span></span>");
		var span = $("<span title='" + s_title + "' class='searchbox-button " + opt.iconCls + "'></span>");
		span.click(function() {
			opt.onClick();
		});

		// $(target).addClass("combo-text");
		// $(target).css({ "border": "none", "width": (opt.width - 24) + "px" }).wrapAll(box);
		$(target).after(span);
		// $(target).validatebox(opt);
	}

	$.fn.plugintext = function(options, params) {
		if (typeof options === "string") {
			return $(this).plugintext.methods[options].call(this, params);
		}

		options = options || {};
		return this.each(function() {
			var opt = $.data(this, "plugintext");
			if (opt) {
				$.extend(opt.options, options);
			} else {
				$.data(this, "plugintext", {
					options : $.extend({}, $.fn.plugintext.defaults, options, $.fn.plugintext.parseOptions(this))
				});
				init(this);
			}
		});
	};

	$.fn.plugintext.methods = {
		options : function() {
			return this.data().plugintext.options;
		}
	};

	$.fn.plugintext.parseOptions = function(target) {
		var jq = $(target);
		return {
			id : jq.attr("id")
		};
	};

	$.fn.plugintext.defaults = {
		iconCls : "icon-search"
	};

	if ($.parser) {
		$.parser.plugins.push("plugintext");
	}
})(jQuery);