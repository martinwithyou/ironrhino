<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('permit')}</title>
</head>
<body>
<@s.form  method="post" cssClass="ajax">
	<@s.hidden name="role" />
	<@s.checkboxlist name="id" list="resources" listKey="key" listValue="value"/>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>