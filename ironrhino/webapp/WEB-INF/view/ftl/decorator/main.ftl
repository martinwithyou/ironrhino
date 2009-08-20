<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#compress>
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title><#noescape>${title}</#noescape></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="context_path" content="${request.contextPath}" />
<link rel="shortcut icon" href="${base}/images/favicon.ico" />
<link href="${base}/styles/all-min.css" media="screen" rel="stylesheet" type="text/css" />
<!--[if IE]>
	<link href="${base}/styles/ie.css" media="all" rel="stylesheet" type="text/css" />
<![endif]-->
<script src="${base}/scripts/all.js" type="text/javascript"></script>
<#if request.servletPath?starts_with('/product/')>
<script type="text/javascript" src="${base}/scripts/app.product.js"></script>
</#if>
<#noescape>${head}</#noescape>
</head>

<body>
<div id="wrapper">
<div id="header">
<div id="logo"><a href="${base}"><img src="${base}/images/logo.gif" alt="返回首页" /></a></div>
<div id="menu">
<ul class="nav">
	<@authorize ifNotGranted="ROLE_BUILTIN_ACCOUNT">
		<li><a id="login_link" href="${base}/account/login">登录</a></li>
		<li><a href="${base}/account/signup">注册</a></li>
	</@authorize>
	<@authorize ifAnyGranted="ROLE_BUILTIN_ACCOUNT">
		<li><a>${authentication('principal.friendlyName')}</a></li>
		<li><a href="${base}/account/manage">账户设置</a></li>
		<li><a href="${base}/account/order">我的订单</a></li>
		<li><a href="${base}/account/favorite">我的收藏</a></li>
		<li><a href="${base}/account/logout">注销</a></li>
	</@authorize>
	<li><a href="${base}/product">产品列表</a></li>
	<li><a href="${base}/product/random">随便看看</a></li>
	<li><a href="${base}/cart">购物车</a></li>
	<li class="last"><a href="${base}" id="root">返回首页</a></li>
</ul>
</div>
<div id="search">
<form id="search_form" action="${base}/search" method="get">
<span><input id="q" type="text" name="q" size="20"
	class="autocomplete_off" value="${request.getParameter('q')?if_exists}" /></span>
<div id="q_update"
	style="display: none; border: 1px solid black; background-color: white;"></div>
<span><@s.submit value="搜索" theme="simple" /></span></form>

</div>
</div>
<#if request.servletPath?starts_with('/product/')||request.servletPath='/'||request.servletPath='/index'>
<div id="left" style="float: left; width: 20%;"><div style="margin: 0 10px;"><@s.action name="left" executeResult="true" /></div></div>
</#if>
<div id="content" style="float: left; width: 60%;">
<div id="message">
<@s.actionerror cssClass="action_error" /><@s.actionmessage cssClass="action_message" /></div>
<#noescape>${body}</#noescape></div>
<#if request.servletPath?starts_with('/product/')||request.servletPath='/'||request.servletPath='/index'>
<div id="right" style="float: left; width: 20%;">
<div style="margin: 0 10px;"><@s.action name="right" executeResult="true" /></div>
</div>
</#if>

<div id="footer">
<div class="text">
<p>© 2008 ironrhino.org</p>
<ul class="footer">
	<li><a href="${base}/feedback">问题反馈</a></li>
	<li><a href="${base}/about.html">关于我们</a></li>
	<li><a href="${base}/contact.html">联系我们</a></li>
</ul>
</div>
</div>
</div>
</body>
</html></#escape></#compress>