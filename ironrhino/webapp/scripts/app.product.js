if (typeof(Initialization) == 'undefined')
	Initialization = {};
Initialization.initPage = function() {
	var code = getCode();
	if (!code)
		return;
	addHistory();
	// load();
	$('#comment_form').attr('replacement','comments');
};

function addHistory() {
	var name = 'HISTORY';
	var value = $.cookie(name);
	var code = getCode();
	if (!value) {
		value = code;
	} else {
		var code_array = value.split(',');
		code_array.unshift(code);
		// code_array = code_array.uniq();
		if (code_array.length > 10)
			code_array.pop();
		value = code_array.join(',');
	}
	var date = new Date();
	date.setTime(date.getTime() + (30 * 24 * 60 * 60 * 1000));
	$.cookie(name, value, {
				path : CONTEXT_PATH || '/',
				expires : date
			});
}

/*
 * function load(){ var array = new Array();
 * $A(document.getElementsByClassName('loading')).each(function(ele){array.push(ele.id)});
 * if(array.length>0){ var url = document.location.href; url =
 * url.substring(0,url.lastIndexOf('/'))+'/view/'+getCode(); new
 * Ajax.Call(url,{replacement:array.join(','),oncomplete:function(){$('comment').onsuccess=addComment}});
 * }else{ $('comment').onsuccess=addComment; } }
 */

function updateScore() {
	if (Ajax.jsonResult) {
		$('#score_average').text(Ajax.jsonResult.scoreResult.average);
		$('#score_count').text(Ajax.jsonResult.scoreResult.count);
	}
}

function getCode() {
	var url = document.location.href;
	if (url.lastIndexOf('.html') < 0)
		return null;
	return url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.html'));
}
