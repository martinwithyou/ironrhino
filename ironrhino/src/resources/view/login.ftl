<!DOCTYPE html>
<#escape x as x?html><html>
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
