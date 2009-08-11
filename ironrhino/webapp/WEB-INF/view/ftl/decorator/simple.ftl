<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#compress><#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title><#noescape>${title}</#noescape></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="Cache-Control" content="no-store" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Expires" content="0" />
<meta name="context_path" content="${request.contextPath}" />
<link href="${base}/styles/all-min.css" media="screen" rel="stylesheet" type="text/css" />
<!--[if IE]>
	<link href="${base}/styles/ie.css" media="all" rel="stylesheet" type="text/css" />
<![endif]-->
<script src="${base}/scripts/all-min.js" type="text/javascript"></script>
<#noescape>${head}</#noescape>
</head>
<body>
<div id="content">
<div id="message">
<@s.actionerror cssClass="action_error" />
<@s.actionmessage cssClass="action_message" />
</div>
<#noescape>${body}</#noescape>
</div>
</body>
</html></#escape></#compress>