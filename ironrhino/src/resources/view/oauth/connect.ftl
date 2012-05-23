<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('login')}</title>
<@authorize ifAnyGranted="ROLE_BUILTIN_USER">
<meta http-equiv="refresh" content="0; url=<@url value="/"/>" />
</@authorize>
</head>
<body>
<#if providers??>
<div class="row">
<#list providers as var>
<div class="span2" style="height:100px;">
<a href="${request.requestURL}?id=${var.name}<#if targetUrl??>&targetUrl=${targetUrl?url}</#if>">
	<img src="${var.logo}" alt="${var.name}"/>
</a>
</div>
</#list>
</div>
</#if>
</body>
</html></#escape>
