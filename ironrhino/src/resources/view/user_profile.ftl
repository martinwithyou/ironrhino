<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('profile')}</title>
</head>
<body>
<#assign readonly=getSetting?? && ('true'==getSetting('user.profile.readonly','false'))/>
<@s.form action="${actionBaseUrl}/profile" method="post" cssClass="form-horizontal ajax">
	<@s.hidden name="user.id"/>
	<@s.textfield label="%{getText('name')}" name="user.name" cssClass="required" readonly=readonly/>
	<@s.textfield label="%{getText('email')}" name="user.email" type="email" cssClass="email checkavailable" readonly=readonly/>
	<@s.textfield label="%{getText('phone')}" name="user.phone" readonly=readonly/>
	<@s.submit value="%{getText('save')}" disabled=readonly />
</@s.form>
</body>
</html></#escape>


