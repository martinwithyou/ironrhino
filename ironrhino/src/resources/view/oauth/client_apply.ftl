<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('apply')}${action.getText('client')}</title>
</head>
<body>
<@s.form action="apply" method="post" cssClass="ajax">
	<@s.textfield label="%{getText('name')}" name="client.name" cssClass="required checkavailable" size="50"/>
	<@s.textfield label="%{getText('redirectUri')}" name="client.redirectUri" cssClass="required" size="50"/>
	<@s.textarea label="%{getText('description')}" name="client.description"  cssStyle="width:400px;height:150px;"/>
	<@s.submit value="%{getText('apply')}" />
</@s.form>
</body>
</html></#escape>