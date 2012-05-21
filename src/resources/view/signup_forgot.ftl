<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('signup')}</title>
<@authorize ifAnyGranted="ROLE_BUILTIN_USER">
<meta http-equiv="refresh" content="0; url=<@url value="/"/>" />
</@authorize>
</head>
<body>
<@s.form method="post" action="forgot" cssClass="ajax reset">
	<@s.textfield label="%{getText('email')}" name="email" cssClass="required email"/>
	<@captcha/>
	<@s.submit value="%{getText('confirm')}" />
</@s.form>
</body>
</html></#escape>