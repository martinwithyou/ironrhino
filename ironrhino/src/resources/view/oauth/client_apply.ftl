<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('apply')}${action.getText('client')}</title>
</head>
<body>
<@s.form action="apply" method="post" cssClass="ajax reset form-horizontal">
	<@s.textfield label="%{getText('name')}" name="client.name" cssClass="required checkavailable" cssClass="span4"/>
	<@s.textfield label="%{getText('redirectUri')}" name="client.redirectUri" cssClass="required" cssClass="span4"/>
	<@s.textarea label="%{getText('description')}" name="client.description" cssClass="span4" cssStyle="height:150px;"/>
	<@s.submit value="%{getText('apply')}" />
</@s.form>
</body>
</html></#escape>