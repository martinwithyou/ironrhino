<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('profile')}</title>
</head>
<body>
<@s.form action="${actionBaseUrl}/profile" method="post" cssClass="form-horizontal ajax">
	<@s.hidden name="user.id"/>
	<@s.textfield label="%{getText('name')}" name="user.name" cssClass="required"/>
	<@s.textfield label="%{getText('email')}" name="user.email" type="email" cssClass="email checkavailable"/>
	<@s.textfield label="%{getText('phone')}" name="user.phone"/>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>


