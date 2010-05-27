<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#compress>
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title><#noescape>${title}</#noescape></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="chrome=1" />
<meta name="context_path" content="${request.contextPath}" />
<link rel="shortcut icon" href="<@url value="/assets/images/favicon.ico"/>" />
<link href="<@url value="/assets/styles/ironrhino-min.css"/>" media="screen" rel="stylesheet" type="text/css" />
<link href="<@url value="/assets/styles/app-min.css"/>" media="screen" rel="stylesheet" type="text/css" />
<!--[if IE]>
	<link href="<@url value="/assets/styles/ie.css"/>" media="all" rel="stylesheet" type="text/css" />
<![endif]-->
<script src="<@url value="/assets/scripts/ironrhino-min.js"/>" type="text/javascript"></script>
<script src="<@url value="/assets/scripts/app-min.js"/>" type="text/javascript"></script>
<#if request.servletPath?starts_with('/product/')>
<script type="text/javascript" src="<@url value="/assets/scripts/app.product.js"/>"></script>
</#if>
<#noescape>${head}</#noescape>
</head>

<body>
<div id="wrapper">
<div id="header">
<div id="logo"><a href="<@url value="/"/>"><img src="<@url value="/assets/images/logo.gif"/>" alt="返回首页" /></a></div>
<div id="menu">
<ul class="nav">
	<@authorize ifNotGranted="ROLE_BUILTIN_USER">
		<li><a id="login_link" href="<@url value="/login"/>">登录</a></li>
		<li><a href="<@url value="/signup"/>">注册</a></li>
	</@authorize>
	<@authorize ifAnyGranted="ROLE_BUILTIN_USER">
		<li><a>${authentication('principal.friendlyName')}</a></li>
		<li><a href="<@url value="/account/manage"/>">账户设置</a></li>
		<li><a href="<@url value="/account/order"/>">我的订单</a></li>
		<li><a href="<@url value="/account/favorite"/>">我的收藏</a></li>
		<li><a href="<@url value="/logout"/>">注销</a></li>
	</@authorize>
	<li><a href="<@url value="/product"/>">产品列表</a></li>
	<li><a href="<@url value="/product/random"/>">随便看看</a></li>
	<li><a href="<@url value="/cart"/>">购物车</a></li>
	<li class="last"><a href="<@url value="/"/>" id="root">返回首页</a></li>
</ul>
</div>
<div id="search">
<form id="search_form" action="<@url value="/search"/>" method="get">
<span><input id="q" type="text" name="q" size="30" maxlength="256" value="${Parameters.q!}" /></span>
<div id="q_update"
	style="display: none; border: 1px solid black; background-color: white;"></div>
<span><@s.submit value="搜索" theme="simple" /></span></form>

</div>
</div>

<#if request.servletPath?starts_with('/product/')||request.servletPath='/'||request.servletPath='/index'>
<div id="content" class="layout grid-s5m0e6">
	<div class="col-main">
		<div class="main-wrap">
			<div id="message"><@s.actionerror cssClass="action_error" /><@s.actionmessage cssClass="action_message" /></div>
			<#noescape>${body}</#noescape>
		</div>
	</div>
	<div class="col-sub">
		<@s.action name="left" executeResult="true" />
	</div>
	<div class="col-extra">
		<@s.action name="right" executeResult="true" />
	</div>
</div>
<#else>
<div id="content" class="layout">
	<div class="col-main">
    <div id="message"><@s.actionerror cssClass="action_error" /><@s.actionmessage cssClass="action_message" /></div>
	<#noescape>${body}</#noescape>
	</div>
</div>
</#if>

<div id="footer">
<div class="text">
<p>© 2008 ironrhino.org</p>
<ul class="footer">
	<li><a href="<@url value="/feedback"/>">问题反馈</a></li>
	<li><a href="<@url value="/about.html"/>">关于我们</a></li>
	<li><a href="<@url value="/contact.html"/>">联系我们</a></li>
</ul>
</div>
</div>
</div>
</body>
</html></#escape></#compress>
