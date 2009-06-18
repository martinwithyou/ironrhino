<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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
<link href="<c:url value="/styles/all.css"/>" media="all"
	rel="stylesheet" type="text/css" />
<!--[if IE]>
	<link href="<c:url value="/styles/ie.css"/>" media="all"
		rel="stylesheet" type="text/css" />
	<![endif]-->
<script type="text/javascript"
	src="<c:url value="/scripts/all.js"/>"></script>
<script type="text/javascript" src="<c:url value="/dwr/engine.js"/>"></script>
<script type="text/javascript"
	src="<c:url value="/dwr/interface/ApplicationContextConsole.js"/>"></script>
<decorator:head />
</head>

<body>
<div id="wrapper">
<div id="header" style="height: 40px">
<div id="menu">
<ul class="nav">
	<li><a>系统配置</a>
	<ul>
		<li><a href="<c:url value="/backend/controlPanel"/>">控制面板</a></li>
		<li><a href="<c:url value="/backend/securityConfig"/>">安全配置</a></li>
		<li><a href="<c:url value="/backend/common/setting"/>">参数设置</a></li>
		<li><a href="<c:url value="/backend/common/customizeEntity"/>">属性定制</a></li>
	</ul>
	</li>
	<li><a href="<c:url value="/backend/common/region"/>">区域管理</a></li>
	<li><a>用户管理</a>
	<ul>
		<li><a href="<c:url value="/backend/ums/user"/>">用户管理</a></li>
		<li><a href="<c:url value="/backend/ums/role"/>">角色管理</a></li>
		<li><a href="<c:url value="/backend/ums/group"/>">用户组管理</a></li>
	</ul>
	</li>
	<li><a>产品管理</a>
	<ul>
		<li><a href="<c:url value="/backend/pms/category"/>">目录管理</a></li>
		<li><a href="<c:url value="/backend/pms/product"/>">产品管理</a></li>
	</ul>
	</li>
	<li><a href="<c:url value="/backend/online/account"/>">帐号管理</a></li>
	<authz:authorize ifAllGranted="ROLE_SUPERVISOR">
		<li><a href="<c:url  value="/backend/switchUser"/>">切换用户</a></li>
	</authz:authorize>
	<authz:authorize ifAllGranted="ROLE_PREVIOUS_ADMINISTRATOR">
		<li><a href="<c:url  value="/backend/exit"/>">退出用户</a></li>
	</authz:authorize>
	<li><a href="<c:url value="/backend/changePassword"/>">修改密码</a></li>
	<li><a href="<c:url value="/backend/logout"/>">注销</a></li>
</ul>
</div>
</div>

<div id="main">
<div id="content" style="margin: 20px 20px; padding: 10px;">
<div id="message"><s:actionerror cssClass="action_error" /><s:actionmessage
	cssClass="action_message" /></div>
<decorator:body /></div>
</div>

<div id="footer"></div>
</div>
</body>
</html>
