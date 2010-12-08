<#assign ua = request.getAttribute('userAgent')/>
<#if ua?? && (ua.name!='msie' || ua.majorVersion gt 8)>
<!DOCTYPE html>
<html>
<#else>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
</#if>
<#compress><#escape x as x?html>
<head>
<title><#noescape>${title}</#noescape></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="chrome=1" />
<meta name="context_path" content="${request.contextPath}" />
<link rel="shortcut icon" href="<@url value="/assets/images/favicon.ico"/>" />
<link href="<@url value="/assets/styles/ironrhino-min.css"/>" media="screen" rel="stylesheet" type="text/css" />
<link href="<@url value="/assets/styles/app-min.css"/>" media="screen" rel="stylesheet" type="text/css" />
<#if ua?? && ua.name=='msie' && ua.majorVersion lt 9>
<!--[if IE]>
	<link href="<@url value="/assets/styles/ie.css"/>" media="all" rel="stylesheet" type="text/css" />
<![endif]-->
</#if>
<script src="<@url value="/assets/scripts/ironrhino-min.js"/>" type="text/javascript"></script>
<#noescape>${head}</#noescape>
</head>

<body>
<div id="wrapper">
<div id="header">
<@authorize ifAnyGranted="ROLE_BUILTIN_USER">
<ul class="menu rounded" corner="top 8px">
	<li><a href="<@url value="/index"/>">${action.getText('index')}</a></li>
	<@authorize ifAnyGranted="ROLE_ADMINISTRATOR">
		<li><a href="<@url value="/user"/>">${action.getText('user')}</a></li>
	</@authorize>
	<li><a href="<@url value="/user/profile"/>">${action.getText('profile')}</a></li>
	<li><a href="<@url value="/user/password"/>">${action.getText('change')}${action.getText('password')}</a></li>
	<li><a href="<@url value="${ssoServerBase!}/logout"/>">${action.getText('logout')}</a></li>
</ul>
</@authorize>
<@authorize ifNotGranted="ROLE_BUILTIN_USER">
<div class="menu rounded" corner="top 8px" style="text-align:center;font-size:1.2em;font-weight:bold;">
${title}
</div>
</@authorize>
</div>

<div id="content" class="rounded" corner="bottom 8px">
<div id="message">
<@s.actionerror cssClass="action_error" />
<@s.actionmessage cssClass="action_message" />
</div>
<#noescape>${body}</#noescape>
</div>

<!--
<div id="footer">
	<ul>
		<li><a href="<@url value="/index"/>">${action.getText('index')}</a></li>
	</ul>
	<p class="copyright">Copyright 2010</p>
</div>
-->

</div>
</body>
</html></#escape></#compress>
