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
<ul class="clearfix">
<#list providers as var>
<li style="float:left;width:200px;">
<a href="${request.requestURL}?id=${var.name}<#if targetUrl??>&targetUrl=${targetUrl?url}</#if>">
	<img src="${var.logo}" alt="${var.name}" style="padding:5px 0 0 5px;max-width:195px;"/>
</a>
</li>
</#list>
</ul>
</#if>
</body>
</html></#escape>
