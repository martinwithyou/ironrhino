<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('pageView')}</title>
</head>
<body>
<ul>
<ul class="unstyled flotlinechart" style="height:500px;">
	<#if data??>
	<#list data as var>
	<li style="float:left;width:200px;padding:10px;">
	<strong style="margin-right:10px;">${var.b?string}</strong>
	<span class="pull-right" data-time="${var.a.time}"><#if date??>${var.a?string('yyyy-MM-dd HH:mm:ss')}<#else>${var.a?string('yyyy-MM-dd')}</#if></span>
	</li>
	</#list>
	</#if>
</ul>
</ul>
</body>
</html></#escape>
