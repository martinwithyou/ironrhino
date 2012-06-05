<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('signup')}</title>
<@authorize ifAnyGranted="ROLE_BUILTIN_USER">
<meta http-equiv="refresh" content="0; url=<@url value="/"/>" />
</@authorize>
</head>
<body>
<div class="row">
	<div class="span4 offset4">
	<@s.form method="post" action="signup" cssClass="ajax form-horizontal">
		<@s.textfield label="%{getText('email')}" name="email" size="24" cssClass="required email" />
		<@s.textfield label="%{getText('username')}" name="username" size="24" />
		<@s.password label="%{getText('password')}" name="password" size="24" cssClass="required"/>
		<@s.password label="%{getText('confirmPassword')}" name="confirmPassword" size="24" cssClass="required"/>
		<@s.submit value="%{getText('signup')}"/>
	</@s.form>
	</div>
</div>
</body>
</html></#escape>