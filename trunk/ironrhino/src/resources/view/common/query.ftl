<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('query')}</title>
<style>
div.preview{
	padding: 4px 6px;
	display:inline-block;
	white-space:pre-wrap;
	border: 1px solid #ccc;
	-webkit-border-radius: 4px;
	-moz-border-radius: 4px;
	border-radius: 4px;
	-webkit-box-shadow: inset 0 1px 1px rgba(0,0,0,0.075);
	-moz-box-shadow: inset 0 1px 1px rgba(0,0,0,0.075);
	box-shadow: inset 0 1px 1px rgba(0,0,0,0.075);
}
div.preview span.comment{
	background-color:yellow;
	color:#999;
}
div.preview span.param{
	font-weight:bold;
}
</style>
<script>
var BLOCK_COMMENT =new RegExp('/\\*(?:.|[\\n\\r])*?\\*/','g');
var LINE_COMMENT =new RegExp('\r?\n?\\s*--.*\r?(\n|$)','g');
var PARAM =new RegExp(':([a-z]\\w*)','gi');
function clearComments(sql){
	return $.trim(sql.replace(BLOCK_COMMENT, '').replace(LINE_COMMENT,'\n'));
}
function highlight(sql){
	return sql.replace(BLOCK_COMMENT, '<span class="comment">$&</span>').replace(LINE_COMMENT,'<span class="comment">$&</span>').replace(PARAM,'<span class="param">$&</span>');
}
function preview(textarea){
	$(textarea).hide();
	$('<div class="preview"></div>').width($(textarea).width()).css('min-height',$(textarea).height()+'px').insertAfter(textarea).html(highlight($(textarea).val()));
		
}
function edit(preview){
	$(preview).hide().prev('textarea[name="sql"]').show().focus();
}
$(function(){
	$(document).on('click','#result tbody .btn',function(){
		if(!$('#row-modal').length)
			var modal = $('<div id="row-modal" class="modal" style="z-index:10000;"><div style="padding: 5px 5px 0 0;"><button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button></div><div  id="row-modal-body" class="modal-body" style="min-height:300px;max-height:600px;"></div></div>')
									.appendTo(document.body).fadeIn().find('button.close').click(function() {
						$(this).closest('.modal').fadeOut().remove();
					});
		var ths = $('th:not(:first-child)',$(this).closest('table'));
		var tds = $('td:not(:first-child)',$(this).closest('tr'));
		var arr = [];
		arr.push('<div class="form-horizontal">');
		for(var i=0;i<ths.length;i++){
			arr.push('<div class="control-group"><label class="control-label">');
			arr.push(ths.eq(i).text());
			arr.push('</label><div class="controls">');
			arr.push(tds.eq(i).text());
			arr.push('</div></div>');
		}
		arr.push('</div>');
		$('#row-modal-body').html(arr.join(''));
	});
	
	$(document).on('change','#tables',function(){
		var t = $(this);
		var table = t.val();
		if(table){
			var textarea = $('textarea[name="sql"]',t.closest('form'));
			if(!textarea.val())
				textarea.val('select * from '+table);
			else
				textarea.val(textarea.val()+' '+table);
			setTimeout(function(){
			var pos = textarea.val().length;
			var ta = textarea.get(0);
			if (ta.setSelectionRange) {
		    	ta.setSelectionRange(pos, pos);
		    } else if (ta.createTextRange) {
		    	var range = ta.createTextRange();
		    	range.collapse(true);
		    	range.moveEnd('character', pos);
		    	range.moveStart('character', pos);
		    	range.select();
		    }else{
		    	textarea.focus();
		    }
			},100);
		}
	});
	$(document).on('blur','textarea[name="sql"]',function(){
		preview(this);
		var sql = clearComments(this.value);
		var map = {};
		$('input[name^="paramMap[\'"]').each(function(){
			if(this.value)
				map[this.name]=this.value;
			$(this).closest('.control-group').remove();	
		});
		
		var params = [];
		var result;
		while((result=PARAM.exec(sql))){
			var param = result[1];
			if($.inArray(param, params)==-1)
				params.push(param);
		}
		
		for(var i=params.length-1;i>=0;i--){
			var param = params[i];
			var name = "paramMap['"+param+"']";
			if(!$('input[name="'+name+'"]').length)
				input = $('<div class="control-group"><label class="control-label" for="query-form_paramMap_\''+param+'\'_">'+param+'</label><div class="controls"><input type="text" name="'+name+'" id="query-form_paramMap_\''+param+'\'_" autocomplete="off" maxlength="255"></div></div>')
				.insertAfter($('textarea[name="sql"]').closest('.control-group')).find('input').val(map[name]);
		}
		
		var paramMap = $('input[name^="paramMap[\'"]');
		for(var i=0;i<paramMap.length;i++){
			var input = paramMap[i];
			if(!input.value){
				setTimeout(function(){$(input).focus()},200);
				break;
			}
		}
	});
	$(document).on('click','div.preview',function(){
		edit(this);
	});
});
Observation.sql = function(container){
	$('textarea[name="sql"]',container).each(function(){
		preview(this);
	});
}
</script>
</head>
<body>
<@s.form id="query-form" action="${actionBaseUrl}" method="post" cssClass="form-horizontal ajax view history">
	<@s.textarea label="sql" name="sql" cssClass="required span8" placeholder="select username,name,email from user where username=:username">
	<#if tables?? && tables?size gt 0>
	<@s.param name="after">
	<div style="display:inline-block;vertical-align:top;margin-left:20px;">
	<@s.select id="tables" theme="simple" cssClass="chosen" list="tables" listKey="top" listValue="top" headerKey="" headerValue=""/>
	</div>
	</@s.param>
	</#if>
	</@s.textarea>
	<#if params??>
	<#list params as var>
	<@s.textfield label="${var}" name="paramMap['${var}']"/>
	</#list>
	</#if>
	<@s.submit value="%{getText('submit')}" />
	<#if resultPage??>
	<#if resultPage.result?size gt 0>
	<#assign map=resultPage.result[0]/>
	<div id="result">
		<table class="pin table table-hover table-striped table-bordered sortable filtercolumn resizable" style="white-space: nowrap;">
			<thead>
			<tr>
				<th class="nosort filtercolumn" style="width:50px;"></th>
				<#list map.keySet() as name>
				<th>${name}</th>
				</#list>
			</tr>
			</thead>
			<tbody>
			<#list resultPage.result as row>
			<tr>
				<td><button type="button" class="btn">${action.getText('view')}</button></td>
				<#list row.entrySet() as entry>
				<td>${(entry.value?string)!}</td>
				</#list>
			</tr>
			</#list>
			</tbody>
		</table>
		<div class="toolbar row">
			<div class="pagination span5">
				<#if resultPage.paginating>
				<ul>
				<#if resultPage.first>
				<li class="disabled firstPage"><a title="${action.getText('firstpage')}"><i class="icon-fast-backward"></i></a></li>
				<li class="disabled"><a title="${action.getText('previouspage')}"><i class="icon-step-backward"></i></a></li>
				<#else>
				<li class="firstPage"><a title="${action.getText('firstpage')}" href="${resultPage.renderUrl(1)}"><i class="icon-fast-backward"></i></a></li>
				<li class="prevPage"><a title="${action.getText('previouspage')}" href="${resultPage.renderUrl(resultPage.previousPage)}"><i class="icon-step-backward"></i></a></li>
				</#if>
				<#if resultPage.last>
				<li class="disabled"><a title="${action.getText('nextpage')}"><i class="icon-step-forward"></i></a></li>
				<li class="disabled lastPage"><a title="${action.getText('lastpage')}"><i class="icon-fast-forward"></i></a></li>
				<#else>
				<li class="nextPage"><a title="${action.getText('nextpage')}" href="${resultPage.renderUrl(resultPage.nextPage)}"><i class="icon-step-forward"></i></a></li>
				<li class="lastPage"><a title="${action.getText('lastpage')}" href="${resultPage.renderUrl(resultPage.totalPage)}"><i class="icon-fast-forward"></i></a></li>
				</#if>
				<li>
				<span class="input-append">
				    <input type="text" name="resultPage.pageNo" value="${resultPage.pageNo}" class="inputPage integer positive" title="${action.getText('currentpage')}"/><span class="add-on totalPage"><span class="divider">/</span><strong title="${action.getText('totalpage')}">${resultPage.totalPage}</strong></span>
				</span>
				<li class="visible-desktop">
				<select name="resultPage.pageSize" class="pageSize" title="${action.getText('pagesize')}">
				<#assign array=[5,10,20,50,100,500]>
				<#assign selected=false>
				<#list array as ps>
				<option value="${ps}"<#if resultPage.pageSize==ps><#assign selected=true> selected</#if>>${ps}</option>
				</#list>
				<#if resultPage.canListAll>
				<option value="${resultPage.totalResults}"<#if !selected && resultPage.pageSize==resultPage.totalResults> selected</#if>>${action.getText('all')}</option>
				</#if>
				</select>
				</li>
				</ul>
				</#if>
			</div>
			<div class="action span2">
				<input type="submit" class="btn noajax" value="${action.getText('export')}" formaction="${actionBaseUrl}/export"/>
			</div>
			<div class="status span5">
				${resultPage.totalResults} ${action.getText('record')} , ${action.getText('tookInMillis',[resultPage.tookInMillis])}
			</div>
		</div>
	</div>
	<#elseif resultPage.executed>
	<div class="alert">
	  <button type="button" class="close" data-dismiss="alert">&times;</button>
	  <strong>${action.getText('query.result.empty')}</strong>
	</div>
	</#if>
	</#if>
</@s.form>
</body>
</html></#escape>


