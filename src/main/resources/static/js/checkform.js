var regs = new Array();
regs[0] = new Array("yz_notblank", "^\\S+$", "不能为空,且不含空格");
regs[1] = new Array("yz_number", "^\\d+\\.?\\d*$", "必须为数字，且不含负数");
regs[2] = new Array("yz_date", "^\\d{4}-\\d{1,2}-\\d{1,2}$", "日期格式为:yyyy-mm-dd，如2007-01-01");
regs[3] = new Array("yz_selectone", "^\\S+$", "下拉框必须选择一项");
regs[4] = new Array("yz_email", "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*", "Email格式如aa@bb.com");
regs[5] = new Array("yz_url", "[a-zA-Z]+://[^\\s]*", "URL格式如http//www.kind.com.cn");
regs[6] = new Array("yz_cnpostcode", "^[0-9]\\d{5}$", "邮政编码为数字，6位");
regs[7] = new Array("yz_cnidcard", "^(\\d{14}|\\d{17})(\\d|x)$", "身份证不能为空，且必须为15位或18位数字！");
regs[8] = new Array("yz_phone", "^(([0\\+]\\d{2,3}-)?(0\\d{2,3})-)?(\\d{7,11})(-(\\d{3,}))?$", "电话格式不正确!"); // ^(([0\\+]\\d{2,3}-)?(0\\d{2,3})-)?(\\d{7,11})(-(\\d{3,}))?$
regs[9] = new Array("yz_telephone", "^(([0\\+]\\d{2,3}-)?(0\\d{2,3})-)?(\\d{7,8})(-(\\d{3,}))?$", "固定电话格式不正确!"); // ^(([0\\+]\\d{2,3}-)?(0\\d{2,3})-)?(\\d{7,8})(-(\\d{3,}))?$
regs[10] = new Array("yz_mobilephone", "^1[3|4|5|6|7|8|9][0-9]\\d{8}$", "手机电话格式不正确!");// 就验证11位
regs[11] = new Array("yz_positivenum", "^\\d+$", "必须为正整数");
regs[12] = new Array("yz_certificate", "", "证件号格式不正确!");
regs[13] = new Array("yz_containblank", "[\\s\\S]+$", "不能为空");
regs[14] = new Array("yz_integer", "^[0-9]\\d*$", "数据必须为整数,且不能为负数");
regs[15] = new Array("yz_integerAndNotZero", "^[0-9]*[1-9][0-9]*$", "数据必须为非零整数,且不能为负数");
regs[16] = new Array("yz_float", "^(\\-?)(\\d+)(\\.?)(\\d*)$", "数据必须为数字,包含零或负数");
regs[17] = new Array("yz_number1", "^\\d{1,10}(\\.\\d{0,2})?$", "必须为数字，且不含负数");
regs[18] = new Array("yz_number2", "^\\d{1,12}(\\.\\d{0,6})?$", "必须为数字，且不含负数");
regs[19] = new Array("yz_positivenum1", "^\\d{1,9}$", "必须为正整数");
regs[20] = new Array("yz_money", "//^\\d+\\.?\\d*$//", "必须输入正确的金额");
regs[21] = new Array("yz_letter", "^[A-Za-z]+$", "必须只能为字母!");
regs[21] = new Array("yz_letterAndNumber", "^[A-Za-z0-9]+$", "必须只能为字母或数字!");
regs[22] = new Array("yz_variable", "^[a-zA-Z][a-zA-Z0-9_]*$", "字母开头,只含有英文字母、数字和下划线!");
regs[23] = new Array("yz_year", "^[1-9][0-9]{3}$", "年份不正确!");
regs[24] = new Array("yz_number3", "^\\d{1,6}(\\.\\d{0,2})?$", "必须为数字，且不含负数");
regs[25] = new Array("yz_money2","^[0-9]*(\\.[0-9]{1,2})?$","金额，保留两位小数");
regs[26] = new Array("yz_nocheck", "^[\\s\\S]*$", "任意字符");
regs[27] = new Array("yz_notblankonside", "^(\\S|\\S[\\s\\S]*\\S)$", "头尾不能为空白字符");
regs[28] = new Array("yz_cnidcard2", "", "身份证为空，或者不为15位或18位数字！");//特殊处理
regs[29] = new Array("yz_notallblank", "^([\\s\\S]*\\S+[\\s\\S]*)$", "不能为空，或全为空白字符");
regs[30] = new Array("yz_licensenumber", "^\\d{17}$", "执业证号不能为空，且必须为17位数字！");//律师执业证号
regs[31] = new Array("yz_money3","(^[0-9]{1,12}$)|[0-9]{1,12}[\.]{1}[0-9]{1,6}$","整数部分不能超过12位，小数部分不能超过6位，且为非负数！");
regs[32] = new Array("yz_bfb","(^[0-9]{1,3}$)|[0-9]{1,3}[\.]{1}[0-9]{1,4}$","整数部分不能超过3位，小数部分不能超过4位，且为非负数！");//百分比
regs[33] = new Array("yz_mj","(^[0-9]{1,16}$)|[0-9]{1,16}[\.]{1}[0-9]{1,2}$","整数部分不能超过16位，小数部分不能超过2位，且为非负数！");//面积
regs[34] = new Array("yz_zzl","(^[0-9]{1,6}$)|[0-9]{1,6}[\.]{1}[0-9]{1,2}$","整数部分不能超过6位，小数部分不能超过2位，且为非负数！");//增值率
regs[35] = new Array("yz_zjl","(^[0-9]{1,6}$)|[0-9]{1,6}[\.]{1}[0-9]{1,2}$","整数部分不能超过5位，小数部分不能超过2位，且为非负数！");//增减率
var g_ErrorMessage = "以下原因导致提交失败：\n";
var ispass = "true";

/**
 * 累加错误字符串
 * 
 * @param index
 *            顺序号
 * @param str
 *            错误信息
 * @returns {String}
 */
function AddError(index, str) {
	return index + "：" + str;
}

/**
 * 检测表单是否满足规则
 * 
 * @param formId
 *            表单ID
 * @param fieldsuffix
 *            字段后缀
 * @returns {Boolean} false验证不通过，true验证通过
 */
function CheckForm(formId, fieldsuffix) {
	var ind = 1;
	var ErrorMessage = g_ErrorMessage;
	var checkform_longMsgNum = 0;// 超长提示语个数，太长需要增加提示框高度
	// 先去除不检查的属性
	$("#" + formId).find("input,select,textarea").each(function(n, id) {
		if ($(this).attr('nocheck') == '1') {
			$(this).removeAttr('nocheck');
		}
	});

	// 查找隐藏的节点，赋所有子节点为不检查，
	$("#" + formId).find("*").each(function(n) {
		var s_tagName = $(this).prop('tagName').toUpperCase();
		if ($(this).css("display") == "none") {
			if (s_tagName != 'INPUT' && s_tagName != 'SELECT' && s_tagName != 'TEXTAREA') {
				if (!($(this).attr('class') && ($(this).attr('class').indexOf('panel') > -1|| $(this).attr('class').indexOf('cleckhide') >-1))) {
					// 不含ui框架中panel的样式class，这里需要特殊处理下
					$(this).find("input,select,textarea").each(function(m) {
						if (!$(this).attr('nocheck')) {
							$(this).attr('nocheck', '1');
						}
					});
				}
			} else {
				if (!$(this).attr('nocheck')) {
					$(this).attr('nocheck', '1');
				}
			}
		}
	});

	// 去除已经赋样式的css
	$("#" + formId).find("input,select,textarea").each(function(n) {
		if ($(this).attr('class')) {
			var s_class = $(this).attr('class');
			if (s_class.indexOf('redBack') > -1) {
				$(this).removeClass('redBack');
			}
		}
		$(this).focus(function() {
			if ($(this).attr('class')) {
				var s_class = $(this).attr('class');
				if (s_class.indexOf('redBack') > -1) {
					if ($(this).attr('type') == 'radio') {
						$("#" + formId).find("input[type=radio][name=" + $(this).attr('name') + "]").removeClass('redBack');
					} else {
						$(this).removeClass('redBack');
					}
				}
			}
		});
	});

	// 先验证必录为空的表单元素(不含radio、hidden)
	$("#" + formId).find("input,select,textarea").each(function(n) {
		if (!$(this).attr("nocheck")) {
			// [required='true'],textarea[required='true'],select[required='true'] 在ie8中这个不认
			if ($(this).attr('type') != 'radio' && $(this).attr('type') != 'hidden' && ($(this).attr('required') == 'true' || $(this).attr('required') == 'required')) {
				if (!$(this).attr("disabled") && $(this).css("display") != "none") {
					var sVal = $(this).val();
					if ($(this).attr('type') == 'checkbox') {
						if (!$(this).attr("checked")) {
							sVal = "";
						}
					}
					if (sVal == "" || sVal == null) {
						// 验证不通过,累加记录warning
						var err_warning_length = checksum($(this).attr("warning"))
						if(err_warning_length>40){
							checkform_longMsgNum++;
						}
						if (ErrorMessage.indexOf(GetCheckRegWarning($(this).attr("name"), $(this).attr("check"), $(this).attr("warning"), 0)) == -1) {
							ErrorMessage += AddError(ind, GetCheckRegWarning($(this).attr("name"), $(this).attr("check"), $(this).attr("warning"), 0) + "\n");
							ispass = "false";
							ind++;
						}
						$(this).addClass('redBack');
					}
				}
			}
		}
	});

	var json_radio = {};
	// 验证必录为空的radio表单元素
	$.each($("#" + formId + " input[type=radio]"), function() {
		if (!$(this).attr("nocheck")) {
			if ($(this).attr('required') == 'true' || $(this).attr('required') == 'required') {
				if ($(this).attr("checked") && !$(this).attr("disabled") && $(this).css("display") != "none") {
					// 有选择的放json中
					eval("json_radio." + $(this).attr("name") + "=1");
				}
			}
		}
	});
	$.each($("#" + formId + " input[type=radio]"), function() {
		if (!$(this).attr("nocheck")) {
			if ($(this).attr('required') == 'true' || $(this).attr('required') == 'required') {
				if (!$(this).attr("checked") && !$(this).attr("disabled") && $(this).css("display") != "none" && !$(this).attr("nocheck")) {
					// 为空的，检查是否在json中
					var i_flag = null;
					eval("i_flag=json_radio." + $(this).attr("name"));
					if (!i_flag || i_flag == null) {
						var err_warning_length = checksum($(this).attr("warning"))
						if(err_warning_length>40){
							checkform_longMsgNum++;
						}
						if (ErrorMessage.indexOf(GetCheckRegWarning($(this).attr("name"), "", $(this).attr("warning"), 0)) == -1) {
							ErrorMessage += AddError(ind, GetCheckRegWarning($(this).attr("name"), "", $(this).attr("warning"), 0) + "\n");
							ispass = "false";
							ind++;
							$.each($("#" + formId + " input[type=radio][name=" + $(this).attr('name') + "]"), function() {
								$(this).addClass('redBack');
							});
						}
					}
				}
			}
		}
	});

	// 表单有值，验证是否满足规则的(不含radio、hidden)
	$("#" + formId).find("input[check],input[maxnum],textarea[check],textarea[maxnum],select[check]").each(function(n) {
		if (!$(this).attr("nocheck")) {

			if ($(this).attr('type') != 'radio' && $(this).attr('type') != 'hidden' && $(this).attr('type') != 'checkbox') {
				// 父节点为隐藏的不检查
				if (!$(this).attr("disabled") && $(this).css("display") != "none") {
					var sVal = $(this).val();
					if (sVal != "" && $(this).attr("check") != "") {
						// 取得验证的正则字符串
						var sReg = GetCheckReg($(this).attr("check"));
						// 字符串->正则表达式,不区分大小写
						var reg = new RegExp(sReg, "i");
						var err_str=OverMaxLength("maxnum",this);
						
						if ($(this).attr("check")=="yz_cnidcard2") {//yz_cnidcard2身份证号码特殊处理
							if (!IdentityCodeValid(sVal)) {
								// 验证不通过,累加记录warning
								//添加title标识，作为未定义error时的指向名称
								var err_title=($(this).attr("currentname")?$(this).attr("currentname"):($(this).attr("title")?$(this).attr("title"):$(this).attr("name")));
								
								var err_warning=$(this).attr("warning");
			
								err_warning=(err_warning?(err_warning.replace(/[!！.。]$/gi,(err_str?",":"!"))+(err_str?"或":"")+err_str):(err_title+err_str));
								var err_warning_length = checksum(err_warning)
								if(err_warning_length>40){
									checkform_longMsgNum++;
								}
			
								if (ErrorMessage.indexOf(GetCheckRegWarning(err_title, $(this).attr("check"),err_warning, 0)) == -1) {
									ErrorMessage += AddError(ind, GetCheckRegWarning(err_title, $(this).attr("check"), err_warning, 0) + "\n");
									ispass = "false";
									ind++;
								}
								$(this).addClass('redBack');
							}
						}else if (!reg.test(sVal)||err_str) {
							// 验证不通过,累加记录warning
							//添加title标识，作为未定义error时的指向名称
							var err_title=($(this).attr("currentname")?$(this).attr("currentname"):($(this).attr("title")?$(this).attr("title"):$(this).attr("name")));
							var err_warning="";
							if(!reg.test(sVal)){
								err_warning = $(this).attr("warning");	
							}
							
							err_warning=(err_warning?(err_warning.replace(/[!！.。]$/gi,(err_str?"，":"!"))+(err_str?"或":"")+err_str):(err_title+err_str));
							var err_warning_length = checksum(err_warning)
							if(err_warning_length>40){
								checkform_longMsgNum++;
							}
		
							if (ErrorMessage.indexOf(GetCheckRegWarning(err_title, $(this).attr("check"),err_warning, 0)) == -1) {
								ErrorMessage += AddError(ind, GetCheckRegWarning(err_title, $(this).attr("check"), err_warning, 0) + "\n");
								ispass = "false";
								ind++;
							}
							$(this).addClass('redBack');
						}
					}
				}
			}
		}
	});

	if (ispass == "false") {
		// 验证不通过,弹出提示warning
		var strMsg = ErrorMessage.replace(/\n/gi, '<br/>');
		var num = strMsg.split("<br/>").length;
		var i_height = 62 + (num+checkform_longMsgNum) * 20;// 算高度，动态加高

		window.top.$.messager.show({
			title : '提示',
			msg : '<div>' + strMsg + '</div>',
			showType : 'slide',// slide，fade
			timeout : 2000,// 2秒
			height : (i_height < 150 ? 'auto' : 150),
			width : 350,
			style : {
				right : '',
				top : document.body.scrollTop + document.documentElement.scrollTop,
				bottom : ''
			}
		});

		// 该表单元素取得焦点,用通用返回函数
		// GoBack(els[this.ErrorItem[1]]);
		ispass = "true";
		return false;
	}
	document.body.onbeforeunload = function() {
	};
	return true;
}

/**
 * 检测表单是否满足规则（除必填项验证外）
 * 
 * @param formId
 *            表单ID
 * @param fieldsuffix
 *            字段后缀
 * @returns {Boolean} false验证不通过，true验证通过
 */
function CheckFormNoRequired(formId, fieldsuffix) {
	 
    var ind = 1;
    var ErrorMessage = g_ErrorMessage;
    var checkform_longMsgNum = 0;// 超长提示语个数，太长需要增加提示框高度
    // 先去除不检查的属性
    $("#" + formId).find("input,select,textarea").each(function(n, id) {
        if ($(this).attr('nocheck') == '1') {
            $(this).removeAttr('nocheck');
        }
    });

    // 查找隐藏的节点，赋所有子节点为不检查，
    $("#" + formId).find("*").each(function(n) {
        var s_tagName = $(this).prop('tagName').toUpperCase();
        if ($(this).css("display") == "none") {
            if (s_tagName != 'INPUT' && s_tagName != 'SELECT' && s_tagName != 'TEXTAREA') {
                if (!($(this).attr('class') && ($(this).attr('class').indexOf('panel') > -1|| $(this).attr('class').indexOf('cleckhide') >-1))) {
                    // 不含ui框架中panel的样式class，这里需要特殊处理下
                    $(this).find("input,select,textarea").each(function(m) {
                        if (!$(this).attr('nocheck')) {
                            $(this).attr('nocheck', '1');
                        }
                    });
                }
            } else {
                if (!$(this).attr('nocheck')) {
                    $(this).attr('nocheck', '1');
                }
            }
        }
    });

    // 去除已经赋样式的css
    $("#" + formId).find("input,select,textarea").each(function(n) {
        if ($(this).attr('class')) {
            var s_class = $(this).attr('class');
            if (s_class.indexOf('redBack') > -1) {
                $(this).removeClass('redBack');
            }
        }
        $(this).focus(function() {
            if ($(this).attr('class')) {
                var s_class = $(this).attr('class');
                if (s_class.indexOf('redBack') > -1) {
                    if ($(this).attr('type') == 'radio') {
                        $("#" + formId).find("input[type=radio][name=" + $(this).attr('name') + "]").removeClass('redBack');
                    } else {
                        $(this).removeClass('redBack');
                    }
                }
            }
        });
    });

    // 表单有值，验证是否满足规则的(不含radio、hidden)
    $("#" + formId).find("input[check],input[maxnum],textarea[check],textarea[maxnum],select[check]").each(function(n) {
        if (!$(this).attr("nocheck")) {
            
            if ($(this).attr('type') != 'radio' && $(this).attr('type') != 'hidden' && $(this).attr('type') != 'checkbox') {
                // 父节点为隐藏的不检查
                if (!$(this).attr("disabled") && $(this).css("display") != "none") {
                	var placeholder = $(this).attr("placeholder");//存在placeholder属性的，当placeholder属性和val相同时就算空值
                    var sVal = $(this).val();
                   	if(placeholder == sVal){
                           sVal = "";
                    }
                    if (sVal != "" && $(this).attr("check") != "") {
                        // 取得验证的正则字符串
                        var sReg = GetCheckReg($(this).attr("check"));
                        // 字符串->正则表达式,不区分大小写
                        var reg = new RegExp(sReg, "i");
                        var err_str=OverMaxLength("maxnum",this);
                        
                        if ($(this).attr("check")=="yz_cnidcard2") {//yz_cnidcard2身份证号码特殊处理
                            if (!IdentityCodeValid(sVal)) {
                                // 验证不通过,累加记录warning
                                //添加title标识，作为未定义error时的指向名称
                                var err_title=($(this).attr("currentname")?$(this).attr("currentname"):($(this).attr("title")?$(this).attr("title"):$(this).attr("name")));
                                
                                var err_warning=$(this).attr("warning");
            
                                err_warning=(err_warning?(err_warning.replace(/[!！.。]$/gi,(err_str?",":"!"))+(err_str?"或":"")+err_str):(err_title+err_str));
                                var err_warning_length = checksum(err_warning)
                                if(err_warning_length>40){
                                    checkform_longMsgNum++;
                                }
            
                                if (ErrorMessage.indexOf(GetCheckRegWarning(err_title, $(this).attr("check"),err_warning, 0)) == -1) {
                                    ErrorMessage += AddError(ind, GetCheckRegWarning(err_title, $(this).attr("check"), err_warning, 0) + "\n");
                                    ispass = "false";
                                    ind++;
                                }
                                $(this).addClass('redBack');
                            }
                        }else if (!reg.test(sVal)||err_str) {
                            // 验证不通过,累加记录warning
                            //添加title标识，作为未定义error时的指向名称
                            var err_title=($(this).attr("currentname")?$(this).attr("currentname"):($(this).attr("title")?$(this).attr("title"):$(this).attr("name")));
                            var err_warning="";
                            if(!reg.test(sVal) && sVal){
                                err_warning = $(this).attr("warning");  
                            }
                            
                            err_warning=(err_warning?(err_warning.replace(/[!！.。]$/gi,(err_str?"，":"!"))+(err_str?"或":"")+err_str):(err_title+err_str));
                            var err_warning_length = checksum(err_warning)
                            if(err_warning_length>40){
                                checkform_longMsgNum++;
                            }
        
                            if (ErrorMessage.indexOf(GetCheckRegWarning(err_title, $(this).attr("check"),err_warning, 0)) == -1) {
                                ErrorMessage += AddError(ind, GetCheckRegWarning(err_title, $(this).attr("check"), err_warning, 0) + "\n");
                                ispass = "false";
                                ind++;
                            }
                            $(this).addClass('redBack');
                        }
                    }
                }
            }
        }
    });

    if (ispass == "false") {
        // 验证不通过,弹出提示warning
        var strMsg = ErrorMessage.replace(/\n/gi, '<br/>');
        var num = strMsg.split("<br/>").length;
        var i_height = 62 + (num+checkform_longMsgNum) * 20;// 算高度，动态加高

        window.top.$.messager.show({
            title : '提示',
            msg : '<div>' + strMsg + '</div>',
            showType : 'slide',// slide，fade
            timeout : 2000,// 2秒
            height : (i_height < 150 ? 'auto' : 150),
            width : 350,
            style : {
                right : '',
                top : document.body.scrollTop + document.documentElement.scrollTop,
                bottom : ''
            }
        });

        // 该表单元素取得焦点,用通用返回函数
        // GoBack(els[this.ErrorItem[1]]);
        ispass = "true";
        return false;
    }
    document.body.onbeforeunload = function() {
    };
    return true;
}

/**
 * 返回查找的正则表达式字符串
 * 
 * @param regExpr
 *            检查check的ID值
 * @returns 返回字符串
 */
function GetCheckReg(regExpr) {
	var reg = regExpr;
	for (var i = 0; i < regs.length; i++) {
		if (regExpr == regs[i][0]) {
			reg = regs[i][1];
			break;
		}
	}
	return reg;
}

/**
 * @param name
 *            元素名称
 * @param regExpr
 *            正则表达式名称，check
 * @param warning1
 *            表单中配置的提示语
 * @param style
 *            开关：0、1
 * @returns 返回提示语
 */
function GetCheckRegWarning(name, regExpr, warning1, style) {
	if (warning1) {
		return warning1;
	}
	if (!regExpr || regExpr == "") {
		if (style == 0) {
			warning = "[" + name + "]不能为空。";
		} else {
			warning = "[" + name + "]验证未通过。";
		}
	}
	var warning = "";
	for (var i = 0; i < regs.length; i++) {
		if (regExpr == regs[i][0]) {
			warning = "[" + name + "]" + regs[i][2];
			break;
		}
	}
	if (warning == "") {
		if (style == 0) {
			warning = "[" + name + "]不能为空。";
		} else {
			warning = "[" + name + "]验证未通过。";
		}
	}
	return warning;
}

/**
 * 通用返回函数,验证没通过返回的效果.
 * 
 * 分三类进行取值 文本输入框,光标定位在文本输入框的末尾
 * 
 * 单多选,第一选项取得焦点
 * 
 * 单多下拉菜单,取得焦点
 * 
 * @param el
 *            表单元素
 */
function GoBack(el) {
	// 取得表单元素的类型
	var sType = el.type;
	try {
		switch (sType) {
		case "text":
		case "hidden":
		case "password":
		case "file":
		case "textarea":
			el.focus();
			var rng = el.createTextRange();
			rng.collapse(false);
			rng.select();
			break;
		case "checkbox":
		case "radio":
			var els = document.getElementsByName(el.name);
			els[0].focus();
			break;
		case "select-one":
		case "select-multiple":
		default:
			el.focus();
			break;

		}
	} catch (e) {
	}
}

/**
 * 取消验证 tab为表格
 * 
 * @param tab
 *            表格
 */
function setNonCheck(tab) {
	var element = window.event.srcElement;
	while (element.tagName != "TR") {
		element = element.parentElement;
	}
	var lenC = 0;
	if (tab.rows[element.rowIndex].cells != null) {
		lenC = tab.rows[element.rowIndex].cells.length;
	}
	for (var i = 0; i < lenC; i++) {
		var mytdlength = tab.rows[element.rowIndex].cells[i].childNodes.length;
		for (var j = 0; j < mytdlength; j++) {
			var tname = tab.rows[element.rowIndex].cells[i].childNodes[j].tagName;
			if (tname != null && tname != "") {
				if (tname.toLowerCase() == "input" || tname.toLowerCase() == "select" || tname.toLowerCase() == "textarea") {
					tab.rows[element.rowIndex].cells[i].childNodes[j].className = "noncheck";
				}
			}
		}
	}
}
/**
 * 取消验证 tab为表格（适用于整个表格循环）
 */
function setNonCheckForTable() {
	var element = window.event.srcElement;
	while (element.tagName != "TABLE") {
		element = element.parentElement;
	}

	// alert(element+"element.tab"+element.tagName);
	var tab = element;

	for (var k = 0; k < tab.rows.length; k++) {
		var lenC = tab.rows[k].cells.length;
		for (var i = 0; i < lenC; i++) {
			var mytdlength = tab.rows[k].cells[i].childNodes.length;
			for (var j = 0; j < mytdlength; j++) {
				var tname = tab.rows[k].cells[i].childNodes[j].tagName;
				if (tname != null && tname != "") {
					if (tname.toLowerCase() == "input" || tname.toLowerCase() == "select" || tname.toLowerCase() == "textarea") {
						tab.rows[k].cells[i].childNodes[j].className = "noncheck";
					} else if (tname.toLowerCase() == "table") { // 表格内嵌表格
						var insideTable = tab.rows[k].cells[i].childNodes[j];
						for (var _tr = 0; _tr < insideTable.rows.length; _tr++) {
							for (var _td = 0; _td < insideTable.rows[_tr].childNodes.length; _td++) {
								for (var _obj = 0; _obj < insideTable.rows[_tr].childNodes[_td].childNodes.length; _obj++) {
									var obj = insideTable.rows[_tr].childNodes[_td].childNodes[_obj];
									if (obj != null && obj.tagName != null) {
										if (obj.tagName.toLowerCase() == "input" || obj.tagName.toLowerCase() == "select" || obj.tagName.toLowerCase() == "textarea") {
											obj.className = "noncheck";
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

}

/**
 * @param sdate
 * @returns {String} 返回验证信息
 */
function checkLessThanToday(sdate) {
	var sReg = regs[2][1];// 日期
	var reg = new RegExp(sReg, "i");

	if (!reg.test(sdate)) {
		return "日期格式不正确!";
	}
	// 需要在html里定义today变量，取服务器时间
	try {
		if (StringToDate(sdate) > StringToDate(today)) {
			return "时间必须小于今天！";
		}
	} catch (e) {
		return "验证失败!";
	}
	return "验证通过!";
}

/**
 * 检查字符串是否前缀字符串
 * 
 * @param fieldname
 *            字符串
 * @param fieldsuffix
 *            包含的子字符串
 * @returns {Boolean} true包含，false不包含
 */
function checkFieldSuffix(fieldname, fieldsuffix) {
	if (fieldsuffix == null || fieldsuffix == "") {
		return (true);
	}
	return (fieldname.indexOf(fieldsuffix) > -1 ? true : false);
}

function OverMaxLength(attrname,obj){
	if($(obj).attr(attrname)!=null&&$(obj).attr(attrname)!=""&&$(obj).val()!=null){
		var ml=$(obj).attr(attrname);
		if(attrname=="maxnum"){
			var check_num=checksum($(obj).val());
			if(check_num>parseInt(ml)){
				
				if(check_num==$(obj).val().length){
					return "不能超过"+ml+"个英文字符或数字！";
				}else{
					return "不能超过"+ml+"个英文字符或数字！";
				}
			}
		}
	}
	return "";
}
//判断字符长度
function checksum(chars){
	var sum = 0; 
	if(chars){
		for (var i=0; i<chars.length; i++){ 
		    var c = chars.charCodeAt(i); 
		    if ((c >= 0x0001 && c <= 0x007e) || (0xff60<=c && c<=0xff9f)){ 
		    	sum++;
		    }else{     
		    	sum+=2;
		    } 
		}
	}
	return sum;
}

$(function(){
	requiredSign();
})
/**
 * 添加表单中必点元素后的红点提示
 */
function requiredSign(){
	$("form [required]").each(function(){
		if ($(this).attr('type') != 'radio' && $(this).attr('type') != 'hidden' && ($(this).attr('required') == 'true' || $(this).attr('required') == 'required')){
			if(!($(this).next().attr("required-sign")=='true')){
				$(this).after("<span style='color:red;padding-left:2px;' required-sign='true'>*<span>");
			}
		}
	});
}
/**
 * 身份证验证专用
 */
function IdentityCodeValid(code) { 
	var city={11:"北京",12:"天津",13:"河北",14:"山西",15:"内蒙古",21:"辽宁",22:"吉林",23:"黑龙江 ",31:"上海",32:"江苏",33:"浙江",34:"安徽",35:"福建",36:"江西",37:"山东",41:"河南",42:"湖北 ",43:"湖南",44:"广东",45:"广西",46:"海南",50:"重庆",51:"四川",52:"贵州",53:"云南",54:"西藏 ",61:"陕西",62:"甘肃",63:"青海",64:"宁夏",65:"新疆",71:"台湾",81:"香港",82:"澳门",91:"国外 "};
	var tip = "";
	var pass= true;

	if(!code || !/^\d{6}(18|19|20)?\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}(\d|X)$/i.test(code)){
        tip = "输入的身份证号有误,请重新输入。";
        pass = false;
    }else if(!city[code.substr(0,2)]){
        tip = "输入的身份证号有误,请重新输入。";
        pass = false;
    }
	else{
	//18位身份证需要验证最后一位校验位
		if(code.length == 18){
			code = code.split('');
			//∑(ai×Wi)(mod 11)
			//加权因子
			var factor = [ 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 ];
			//校验位
			var parity = [ 1, 0, 'X', 9, 8, 7, 6, 5, 4, 3, 2 ];
			var sum = 0;
			var ai = 0;
			var wi = 0;
			for (var i = 0; i < 17; i++){
				ai = code[i];
				wi = factor[i];
				sum += ai * wi;
			}
			var last = parity[sum % 11];
			if(parity[sum % 11] != code[17]){
				tip = "输入的身份证号有误,请重新输入。";
				pass =false;
			}
		}
	}
	return pass;
}
