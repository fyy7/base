/*
 * 将页面设置为展示
 */
function setinputshow(fromid) {
	var addstr = "";
	if (fromid) {
		addstr = "#" + fromid + " ";
	}
	$(addstr + "input:visible").each(function() {
		input2show($(this));
	});

	$(addstr + "select:visible").each(function() {
		select2show($(this));
	});
}

function input2show(obj) {
	var type = $(obj).attr('type');
	if (type == 'file') {
		$(obj).remove();
	} else if (type == 'checkbox') {
		$(obj).attr("disabled", 'disabled');
	} else if (type == 'button') {
		if (!$(obj).hasClass('noclean')) {
			$(obj).remove();
		}
	} else {
		$(obj).after($(obj).val());
		$(obj).remove();
	}
}
function select2show(obj) {
	$(obj).after($(obj).find("option:selected").text());
	$(obj).remove();
}