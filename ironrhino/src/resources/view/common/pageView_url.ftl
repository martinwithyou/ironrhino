<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('pageView')}</title>
</head>
<body>
<h3 style="text-align:center;"><#if date??>${date?string('yyyy-MM-dd')}<#else>${action.getText('total')}</#if></h3>
<ul class="unstyled">
	<#if urls??>
	<#list urls?keys as url>
	<li style="float:left;width:50%;">
	<span style="margin-right:10px;">${url}</span>
	<strong class="pull-right" style="margin-right:20px;">${urls[url]?string}</strong>
	</li>
	</#list>
	</#if>
</ul>
</body>
</html></#escape>
