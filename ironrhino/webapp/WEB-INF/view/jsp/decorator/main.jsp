<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="authz"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator"
	prefix="decorator"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title><decorator:title default="ironrhino" /></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="Cache-Control" content="no-store" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Expires" content="0" />
<meta name="context_path" content="${pageContext.request.contextPath}" />
<link rel="shortcut icon" href="<c:url value="/images/favicon.ico"/>" />
<link href="<c:url value="/styles/main.css"/>" media="screen"
	rel="stylesheet" type="text/css" />
<link rel="stylesheet" href="<c:url value="/themes/base/ui.all.css"/>"
	type="text/css" media="screen" />
<!--[if IE]>
	<link href="<c:url value="/styles/ie.css"/>" media="all"
		rel="stylesheet" type="text/css" />
	<![endif]-->
<link rel="alternate" href="<c:url value="/product/feed"/>"
	title="ironrhino products" type="application/atom+xml" />
<script src="<c:url value="/scripts/jquery.js"/>" type="text/javascript"></script>
<script src="<c:url value="/scripts/jquery.cookie.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/effects.core.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/effects.highlight.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/ui.core.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/ui.dialog.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/ui.resizable.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/ui.draggable.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/ui.tabs.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/jquery.history.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/jquery.form.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/jquery.corner.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/jquery.bgiframe.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/jquery.autocomplete.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/jquery.emptyonclick.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/jquery.dragable.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/sortabletable.js"/>"
	type="text/javascript"></script>
<script src="<c:url value="/scripts/application.js"/>"
	type="text/javascript"></script>

<c:if
	test="${fn:startsWith(pageContext.request.servletPath,'/product/')&&fn:endsWith(pageContext.request.servletPath,'.html')}">
	<script type="text/javascript"
		src="<c:url value="/scripts/product.js"/>"></script>
</c:if>
<decorator:head />
</head>

<body>
<div id="wrapper">
<div id="header">
<div id="logo"><a href="<c:url value="/"/>"><img
	src="<c:url value="/images/logo.gif"/>" alt="返回首页" /></a></div>
<div id="menu">
<ul class="nav">
	<authz:authorize ifNotGranted="ROLE_BUILTIN_ACCOUNT">
		<li><a id="login_link" href="<c:url value="/account/login"/>">登录</a></li>
		<li><a href="<c:url value="/account/signup"/>">注册</a></li>
	</authz:authorize>
	<authz:authorize ifAnyGranted="ROLE_BUILTIN_ACCOUNT">
		<li><a><authz:authentication
			property="principal.friendlyName" /></a></li>
		<li><a href="<c:url value="/account/manage"/>">账户设置</a></li>
		<li><a href="<c:url value="/account/order"/>">我的订单</a></li>
		<li><a href="<c:url value="/account/favorite"/>">我的收藏</a></li>
		<li><a href="<c:url value="/account/logout"/>">注销</a></li>
	</authz:authorize>
	<li><a href="<c:url value="/product"/>">产品列表</a></li>
	<li><a href="<c:url value="/product/random"/>">随便看看</a></li>
	<li><a href="<c:url value="/cart"/>">购物车</a></li>
	<li class="last"><a href="<c:url value="/"/>" id="root">返回首页</a></li>
</ul>
</div>
<div id="search">
<form id="search_form" action="<c:url value="/search"/>" method="get">
<span><input id="q" type="text" name="q" size="20"
	class="autocomplete_off" value="${param['q']}"/></span>
<div id="q_update"
	style="display: none; border: 1px solid black; background-color: white;"></div>
<span><s:submit value="搜索" theme="simple"/></span></form>

</div>
</div>


<div id="left" style="float: left; width: 20%;">
<div style="margin: 0px 20px;"><s:action name="left" namespace="/"
	executeResult="true" /></div>
</div>
<div id="content" style="float: left; width: 60%;">
<div id="message"><s:actionerror cssClass="action_error" /><s:actionmessage
	cssClass="action_message" /></div>
<decorator:body /></div>
<div id="right" style="float: left; width: 20%;">
<div style="margin: 0px 20px;"><s:action name="right"
	namespace="/" executeResult="true" /></div>
</div>


<div id="footer">
<div class="text">
<p>© 2008 ironrhino.org</p>
<ul class="footer">
	<li><a href="<c:url value="/feedback"/>">问题反馈</a></li>
	<li><a href="<c:url value="/about.html"/>">关于我们</a></li>
	<li><a href="<c:url value="/contact.html"/>">联系我们</a></li>
</ul>
</div>
</div>
</div>
</body>
</html>
