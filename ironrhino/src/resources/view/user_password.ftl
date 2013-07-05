<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('change')}${action.getText('password')}</title>
</head>
<body>
<@s.form action="${actionBaseUrl}/password" method="post" cssClass="form-horizontal ajax focus reset">
	<@s.password label="%{getText('currentPassword')}" name="currentPassword" cssClass="required" />
	<@s.password label="%{getText('password')}" name="password" cssClass="required" />
	<@s.password label="%{getText('confirmPassword')}" name="confirmPassword" cssClass="required" />
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>