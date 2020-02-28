/**
 * <input>,<textarea>限制输入
 * 如：<input type="text" name='NAEM1' inputlimit='number'/>
 */
var _inputlimit = {
	attrName:'inputlimit',
	keyValuecheckConfig:{
		number:'^[0-9]+$',
	},
	resultValueCheckConfig:{
		money2:'^[0-9]*(\\.[0-9]{1,2})?$',
		money4:'^[0-9]*(\\.[0-9]{1,4})?$'
	}
};
$(function(){
	//_inputlimit.init();
});

_inputlimit.init = function(){
	_inputlimit.inited = true;
	$("input["+_inputlimit.attrName+"],textarea["+_inputlimit.attrName+"]").each(function(){
		_inputlimit.bind($(this));
	});
};

_inputlimit.bind = function(jqueryObj){
	var attrValue = jqueryObj.attr(_inputlimit.attrName);
	if(_inputlimit.keyValuecheckConfig[attrValue]){
		jqueryObj.off('input');//keypress
		jqueryObj.on('input', null, _inputlimit.keydown);
	}
};

_inputlimit.keydown = function(e){
	if(e.inputType==='deleteContentBackward' || e.inputType==='deleteContentForward'){
		return false;
	}
	
	var reg = new RegExp(e.data.regStr);
	console.log(e.target.value);
	console.log(reg.test(e.target.value));
	if(!reg.test(e.target.value)){
		e.preventDefault();
	}
};