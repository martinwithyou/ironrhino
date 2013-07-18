<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('query')}</title>
<style>
#result th {
	text-align: center;
	min-width: 100px;
	padding: .5em 0;
}
#result td {
	white-space: nowrap;
}
#result .pagination ul{
  -webkit-border-radius: 0;
     -moz-border-radius: 0;
          border-radius: 0;
  -webkit-box-shadow: none;
     -moz-box-shadow: none;
          box-shadow: none;
}
#result .pagination a{
	padding:0 8px;
	line-height: 26px;
}
#result .pagination li.disabled a i{
	filter: Alpha(Opacity=20); 
	opacity: 0.20;
}
#result .pagination li.firstPage a{
  -webkit-border-radius: 3px 0 0 3px;
     -moz-border-radius: 3px 0 0 3px;
          border-radius: 3px 0 0 3px;
}
#result .pagination li.lastPage a{
  -webkit-border-radius: 0 3px 3px 0;
     -moz-border-radius: 0 3px 3px 0;
          border-radius: 0 3px 3px 0;
}
#result .pagination .inputPage {
	width: 25px;
	height: 18px;
	text-align:right;
}
#result .pagination span.input-append {
  padding: 0 4px;
  line-height: 20px;
  background-color: transparent;
  border: 0;
  margin-bottom: 4px;
}
#result .pagination span.add-on {
  float: none;
}
#result .pagination span.divider {
  float: none;
  padding: 0;
  line-height: 20px;
  background-color: transparent;
  border: 0;
}
#result .pagination .totalPage {
	min-width: 25px;
	height: 18px;
	text-align:left;
}
#result .pagination .pageSize {
	border: 1px solid #ddd;
	-webkit-border-radius: 3px;
    -moz-border-radius: 3px;
    border-radius: 3px;
	width: 60px;
	height: 28px;
}
#result .pagination,.status{
	margin-top:18px;
	margin-bottom:0;
}
</style>
<script>
$(function(){
	$(document).on('click',
			'.firstPage:not(.disabled) a', function(event) {
				var form = $(this).closest('form');
				$('.inputPage', form).val(1);
				form.submit();
				return false;
			}).on('click', '.pagination .prevPage:not(.disabled) a',
			function(event) {
				var form = $(this).closest('form');
				$('.inputPage', form).val(function(i, v) {
							return parseInt(v) - 1
						});
				form.submit();
				return false;
			}).on('click', '.pagination .nextPage:not(.disabled) a',
			function(event) {
				var form = $(this).closest('form');
				$('.inputPage', form).val(function(i, v) {
							return parseInt(v) + 1
						});
				form.submit();
				return false;
			}).on('click', '.pagination .lastPage:not(.disabled) a',
			function(event) {
				var form = $(this).closest('form');
				$('.inputPage', form).val($('.totalPage strong', form).text());
				form.submit();
				return false;
			}).on('change', '.pagination .inputPage', function(event) {
				var form = $(event.target).closest('form');
				form.submit();
				event.preventDefault();
			}).on('change', '.pagination select.pageSize', function(event) {
				var form = $(event.target).closest('form');
				$('.inputPage', form).val(1);
				form.submit();
	});

});
</script>
</head>
<body>
<@s.form id="query-form" action="${actionBaseUrl}" method="post" cssClass="form-horizontal ajax view">
	<@s.textarea label="sql" name="sql" cssClass="required span10"/>
	<#if paramNames??>
	<#list paramNames as var>
	<@s.textfield label="${var}" name="params['${var}']" cssClass="required"/>
	</#list>
	</#if>
	<@s.submit value="%{getText('submit')}" />
	<#if resultPage?? && resultPage.result?? && resultPage.result?size gt 0>
	<#assign map=resultPage.result[0]/>
	<div id="result">
		<table class="pin table table-hover table-striped table-bordered sortable filtercolumn resizable">
			<thead>
			<tr>
				<#list map.keySet() as name>
				<th>${name}</th>
				</#list>
				<th class="nosort" style="min-width:40px;"></th>
			</tr>
			</thead>
			<tbody>
			<#list resultPage.result as row>
			<tr>
				<#list map.keySet() as name>
				<td>${(row[name]?string)!}</td>
				</#list>
				<td></td>
			</tr>
			</#list>
			</tbody>
		</table>
		<div class="row">
			<div class="pagination span8">
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
			</div>
			<div class="status span4">
				<span class="pull-right">${resultPage.totalResults} ${action.getText('record')}</span>
			</div>
		</div>
	</div>
	</#if>
</@s.form>
</body>
</html></#escape>


