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
	<@s.form method="post" action="forgot" cssClass="ajax reset form-horizontal">
		<@s.textfield label="%{getText('email')}" name="email" type="email" cssClass="required email"/>
		<@captcha/>
		<@s.submit value="%{getText('confirm')}" />
	</@s.form>
	</div>
</div>
</body>
</html></#escape>