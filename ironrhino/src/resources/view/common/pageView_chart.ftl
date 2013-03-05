<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('pageView')}</title>
</head>
<body>
<ul>
<#if max?? || total??>
<div style="padding:5px;">
	<#if max??>
	<span>${action.getText('max')}:</span>
	<strong style="margin-right:10px;">${max.b?string}</strong>
	<span>${max.a?string('yyyy-MM-dd')}</span>
	</#if>
	<#if total??>
	<span style="margin-left:10px;">${action.getText('total')}:</span>
	<strong>${total?string}</strong>
	</#if>
</div>
</#if>
<ul class="unstyled flotlinechart" style="height:300px;" data-format="<#if date??>%H(%m-%d)<#else>%m-%d</#if>">
	<#if data??>
	<#list data as var>
	<li style="float:left;width:200px;padding:10px;">
	<strong style="margin-right:10px;">${var.b?string}</strong>
	<span class="pull-right" data-time="${var.a.time}"><#if date??>${var.a?string('HH(yyyy-MM-dd)')}<#else>${var.a?string('yyyy-MM-dd')}</#if></span>
	</li>
	</#list>
	</#if>
</ul>
</ul>
</body>
</html></#escape>
