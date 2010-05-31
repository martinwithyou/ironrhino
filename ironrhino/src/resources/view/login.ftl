<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('login')}</title>
<@authorize ifAnyGranted="ROLE_BUILTIN_USER">
<meta http-equiv="refresh" content="0; url=<@url value="/"/>" />
</@authorize>
</head>
<body>
<div><@includePage path="/login/intro"/></div>
<@authorize ifAnyGranted="ROLE_BUILTIN_USER">
	<p style="text-align: center;">您已经以${authentication('name')}身份登录,换个用户名请先<a href="<@url value="/logout"/>">注销</a>,<a
		href="<@url value="/"/>">进入</a></p>
</@authorize>
<@authorize ifNotGranted="ROLE_BUILTIN_USER">
	<div
		style="margin: auto; width: 300px; font-size: 120%;">
	<@s.form id="login" action="login" method="post" cssClass="ajax focus">
		<@s.hidden id="targetUrl" name="targetUrl" />
		<@s.textfield label="%{getText('username')}" name="username" cssClass="required" labelposition="left" />
		<@s.password label="%{getText('password')}" name="password" cssClass="required" labelposition="left" />
		<div class="fieldset"><@s.submit value="%{getText('login')}" /></div>
	</@s.form></div>
</@authorize>
</body>
</html></#escape>
