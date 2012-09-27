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
	<@s.form method="post" action="signup" cssClass="ajax focus form-horizontal well">
		<@s.textfield label="%{getText('email')}" name="email" type="email" cssClass="required email span2" />
		<@s.textfield label="%{getText('username')}" name="username" cssClass="span2" />
		<@s.password label="%{getText('password')}" name="password" cssClass="required span2"/>
		<@s.password label="%{getText('confirmPassword')}" name="confirmPassword" cssClass="required span2"/>
		<@s.submit value="%{getText('signup')}"/>
	</@s.form>
	</div>
</div>
</body>
</html></#escape>