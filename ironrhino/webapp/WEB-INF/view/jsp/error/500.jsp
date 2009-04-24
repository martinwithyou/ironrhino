<%@ page contentType="text/html; charset=utf-8" isErrorPage="true"%>
<%@ page import="org.apache.commons.logging.LogFactory"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>出错页面</title>
</head>
<body>
<div id="content">
<%
	if (exception != null) {
		//Exception from JSP didn't log yet ,should log it here.
		String requestUri = (String) request
				.getAttribute("javax.servlet.error.request_uri");
		LogFactory.getLog(requestUri).error(exception.getMessage(),
				exception);
	} else if (request.getAttribute("exception") != null) {
		//from Spring
		exception = (Exception) request.getAttribute("exception");
	}
%>
<h3>系统运行期错误: <br />
<%=exception.getMessage()%></h3>
<br />
<button class="goback">返回</button>
<br />
<authz:authorize ifAnyGranted="ROLE_SUPERVISOR">
	<p><span class="link"onclick"$('#detail_error_msg').toggle()">开发人员点击此处获取详细错误信息</span></p>
	<div id="detail_error_msg" style="display: none"><pre>
<%
	exception.printStackTrace(new java.io.PrintWriter(out));
%>
</pre></div>
</authz:authorize></div>
</body>
</html>
