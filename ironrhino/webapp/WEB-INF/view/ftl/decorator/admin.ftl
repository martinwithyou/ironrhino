<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#compress><#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title><#noescape>${title}</#noescape></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="Cache-Control" content="no-store" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Expires" content="0" />
<meta name="context_path" content="${request.contextPath}" />
<link rel="shortcut icon" href="${base}/images/favicon.ico" />
<link href="${base}/styles/all-min.css" media="screen" rel="stylesheet" type="text/css" />
<!--[if IE]>
	<link href="${base}/styles/ie.css" media="all" rel="stylesheet" type="text/css" />
<![endif]-->
<link rel="alternate" href="${base}/product/feed" title="ironrhino products" type="application/atom+xml" />
<script src="${base}/scripts/all-min.js" type="text/javascript"></script>
<#noescape>${head}</#noescape>
</head>

<body>
<div id="wrapper">
<div id="header" style="height: 40px">
<div id="menu">
<ul class="nav">
	<li><a>系统配置</a>
	<ul>
		<li><a href="${base}/common/console">控制台</a></li>
		<li><a href="${base}/common/setting">参数设置</a></li>
		<li><a href="${base}/common/monitor">系统监控</a></li>
		<li><a href="${base}/common/customizeEntity">属性定制</a></li>
	</ul>
	</li>
	<li><a href="${base}/common/region">区域管理</a></li>
	<li><a>用户管理</a>
	<ul>
		<li><a href="${base}/ums/user">用户管理</a></li>
	</ul>
	</li>
	<li><a>产品管理</a>
	<ul>
		<li><a href="${base}/pms/category">目录管理</a></li>
		<li><a href="${base}/pms/product">产品管理</a></li>
	</ul>
	</li>
	<li><a href="${base}/logout">注销</a></li>
</ul>
</div>
</div>

<div id="main">
<div id="content" style="margin: 20px 20px; padding: 10px;">
<div id="message">
<@s.actionerror cssClass="action_error" />
<@s.actionmessage cssClass="action_message" />
</div>
<#noescape>${body}</#noescape>
</div>
</div>
<div id="footer"></div>
</div>
</body>
</html></#escape></#compress>
