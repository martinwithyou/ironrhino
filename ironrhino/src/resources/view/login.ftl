<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('login')}</title>
<@authorize ifAnyGranted="ROLE_BUILTIN_USER">
<meta http-equiv="refresh" content="0; url=<@url value="/"/>" />
</@authorize>
</head>
<body>
<div class="row">
	<div class="span4 offset4">
	<@s.form id="login" action="login" method="post" cssClass="ajax focus form-horizontal well">
		<@s.hidden id="targetUrl" name="targetUrl" />
		<@s.textfield label="%{getText('username')}" name="username" cssClass="required"/>
		<@s.password label="%{getText('password')}" name="password" cssClass="required"/>
		<@s.checkbox label="%{getText('rememberme')}" name="rememberme"/>
		<@captcha/>
		<@s.submit value="%{getText('login')}" />
	</@s.form>
	</div>
</div>
</body>
</html></#escape>
