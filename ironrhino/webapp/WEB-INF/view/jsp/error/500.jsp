<%@ page contentType="text/html; charset=utf-8" isErrorPage="true"%>
<%@ page import="org.apache.commons.logging.LogFactory"%>
<%
	if (exception == null && request.getAttribute("exception") != null) 
		exception = (Exception) request.getAttribute("exception");
	org.apache.commons.logging.LogFactory.getLog((String)request.getAttribute("javax.servlet.error.request_uri")).error(exception.getMessage(),exception);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>错误页面</title>
</head>
<body>
<div id="content">
<h3>系统错误</h3>
<button onclick="history.go(-1)">返回</button>
<br />
</div>
</body>
</html>
