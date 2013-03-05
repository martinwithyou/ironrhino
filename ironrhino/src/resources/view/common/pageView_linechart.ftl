<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('pageView')}</title>
</head>
<body>
<div style="padding:5px;">
	<#if date??><span style="margin-right:10px;">${date?string('yyyy-MM-dd')}<#elseif from??&&to??>${from?string('yyyy-MM-dd')} -> ${to?string('yyyy-MM-dd')}</span></#if>
	<#if max??>
	<span class="pull-right" style="margin:0 10px;">${action.getText('max')}:
	<strong>${max.b?string}</strong>
	${max.a?string('yyyy-MM-dd')}
	</span>
	</#if>
	<#if total??>
	<span class="pull-right" style="margin:0 10px;">${action.getText('total')}:<strong>${total?string}</strong></span>
	</#if>
</div>
<ul class="unstyled flotlinechart" style="height:300px;" data-format="<#if date??>%H(%m-%d)<#else>%m-%d</#if>">
	<#if dataList??>
	<#list dataList as var>
	<li style="float:left;width:200px;padding:10px;">
	<strong style="margin-right:10px;">${var.b?string}</strong>
	<span class="pull-right" data-time="${var.a.time}"><#if date??>${var.a?string('HH(yyyy-MM-dd)')}<#else>${var.a?string('yyyy-MM-dd')}</#if></span>
	</li>
	</#list>
	</#if>
</ul>
</body>
</html></#escape>
