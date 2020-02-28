//基于easyui
var K_MULTIPLE_SELECTION = {};

$(function() {
	var K_MULTIPLE_SELECTION_STYLE = document.createElement("style");
	K_MULTIPLE_SELECTION_STYLE.type = "text/css";
	K_MULTIPLE_SELECTION_STYLE.appendChild(document.createTextNode(".textbox.combo input.textbox-text.validatebox-text {height:34px !important;}"));
	K_MULTIPLE_SELECTION_STYLE.appendChild(document.createTextNode(".textbox.combo span.textbox.combo{border:1px solid #ddd !important;border-radius:3px !important;}"));
	K_MULTIPLE_SELECTION_STYLE.appendChild(document.createTextNode(".textbox.combo a.combo-arrow{background-color:#FFFFFF;}"));
	K_MULTIPLE_SELECTION_STYLE.appendChild(document.createTextNode(".textbox.combo{border:1px solid #ddd}"));
	K_MULTIPLE_SELECTION_STYLE.appendChild(document.createTextNode(".combo-panel.panel-body.panel-body-noheader{border:1px solid #ddd;max-height:350px;}"));
	var head = document.getElementsByTagName("head")[0];
	head.appendChild(K_MULTIPLE_SELECTION_STYLE);
});

K_MULTIPLE_SELECTION.detaultOption = {
   valueField: 'ID',
   textField: 'TEXT',
   panelHeight: 'auto',
   multiple: true,
   formatter: function (row) {
       var opts = $(this).combobox('options');
       return '<input type="checkbox" class="combobox-checkbox" id="' + row[opts.valueField] + '">' + row[opts.textField]
   },

   onShowPanel: function () {
       var opts = $(this).combobox('options');
       var target = this;
       var values = $(target).combobox('getValues');
       $.map(values, function (value) {
           var el = opts.finder.getEl(target, value);
           el.find('input.combobox-checkbox')._propAttr('checked', true);
       })
   },
   onSelect: function (row) {
       var opts = $(this).combobox('options');
       var el = opts.finder.getEl(this, row[opts.valueField]);
       el.find('input.combobox-checkbox')._propAttr('checked', true);
       
       //
       if(opts.checkAll){
    	   if($(this).combobox('getValues')!=''){
    		   var values = $(this).combobox('getValues').toString();
    		   var length = K_MULTIPLE_SELECTION.checkOptionsLength(values.split(","),$(this).combobox('getData'));
    		   if( (length+1) == opts.dataLength){
        		   $("#"+opts.allcheckid).prop("checked",true);
        	   }
    	   }
       }
   },
   onUnselect: function (row) {
       var opts = $(this).combobox('options');
       var el = opts.finder.getEl(this, row[opts.valueField]);
       el.find('input.combobox-checkbox')._propAttr('checked', false);
       
       //
       if(opts.checkAll){
    	   if($(this).combobox('getValues')!=''){
    		   var values = $(this).combobox('getValues').toString();
    		   var length = K_MULTIPLE_SELECTION.checkOptionsLength(values.split(","),$(this).combobox('getData'));
    		   if( length == opts.dataLength){
        		   $("#"+opts.allcheckid).prop("checked",false);
        	   }
    	   }
       }
   }
}

K_MULTIPLE_SELECTION.checkAllBtnHtml = '<input type="checkbox" class="combobox-checkbox" style="margin-right: 5px;" target="targetid" id="allcheckid"><label for="allcheckid">全选</label></span>';
K_MULTIPLE_SELECTION.checkOptionsLength = function(arr,data){
	var lenght = 0;
	for(var value in arr) {  
        $.each(data, function(index,json){
        	if(json.ID==arr[value]){
        		lenght++;
        		return true;
        	}
        });
    };
	return lenght;
}

//初始化
K_MULTIPLE_SELECTION.index = 0;
K_MULTIPLE_SELECTION.combobox = function(selector,_jsonDataArr,checkAll,callback){
  var _jqueryObj = $(selector);
  K_MULTIPLE_SELECTION.detaultOption.checkAll = checkAll;
  K_MULTIPLE_SELECTION.detaultOption.allcheckid = "allcheckid_"+(K_MULTIPLE_SELECTION.index++);
  var jsonLength=0;
  if(_jsonDataArr){
	  jsonLength = _jsonDataArr.length;
  }else{
	  $("selector").find("option").length;
  }
  K_MULTIPLE_SELECTION.detaultOption.dataLength = jsonLength;
  _jqueryObj.combobox(K_MULTIPLE_SELECTION.detaultOption);
  if(_jsonDataArr){
	  _jqueryObj.combobox('loadData',_jsonDataArr);
  }
  _jqueryObj.combobox({onChange:callback});
  _jqueryObj.next('span.textbox.combo').find("a.textbox-icon.combo-arrow").height(34);
  _jqueryObj.next('span.textbox.combo').find("input.textbox-text.validatebox-text").prop("readonly","readonly").click(function(){
	  $(this).prev('span.textbox-addon.textbox-addon-right').find("a.textbox-icon.combo-arrow").click();
  });
  if(checkAll){
	  _jqueryObj.next('span.textbox.combo').after(K_MULTIPLE_SELECTION.checkAllBtnHtml.replace("allcheckid",K_MULTIPLE_SELECTION.detaultOption.allcheckid).replace("targetid",selector));
	  $("#"+K_MULTIPLE_SELECTION.detaultOption.allcheckid).change(function(){
		if ( $(this).is(':checked') == true ){
		  //全选
		  _target_combobox = $($(this).attr("target"));
		  var dataJson = _target_combobox.combobox('getData');
		  var dataArr = new Array();
		  $.each(dataJson,function(index,json){
			  dataArr.push(json.ID);
		  });
		  _target_combobox.combobox('setValues',dataArr);
		}else{
		  //清空选择
		  $($(this).attr("target")).combobox('setValue','');
		}
	  });
  }
}