<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('query')}</title>
<style>
#result th:not(:first-of-type) {
	text-align: center;
	min-width: 100px;
	padding: .5em 0;
}
#result th:first-of-type{
	width:50px;
}
#result td {
	white-space: nowrap;
}
</style>
<script>
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
});
</script>
</head>
<body>
<@s.form id="query-form" action="${actionBaseUrl}" method="post" cssClass="form-horizontal ajax view history">
	<@s.textarea label="sql" name="sql" cssClass="required span10" placeholder="select username,name,email from user where username=:username"/>
	<#if params??>
	<#list params as var>
	<@s.textfield label="${var}" name="paramMap['${var}']" cssClass="required"/>
	</#list>
	</#if>
	<@s.submit value="%{getText('submit')}" />
	<#if resultPage?? && resultPage.result?? && resultPage.result?size gt 0>
	<#assign map=resultPage.result[0]/>
	<div id="result">
		<table class="pin table table-hover table-striped table-bordered sortable filtercolumn resizable">
			<thead>
			<tr>
				<th class="nosort filtercolumn"></th>
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
		<div class="toolbar row-fluid">
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
				${resultPage.totalResults} ${action.getText('record')}
			</div>
		</div>
	</div>
	</#if>
</@s.form>
</body>
</html></#escape>


