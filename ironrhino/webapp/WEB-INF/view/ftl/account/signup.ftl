<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('signup')}</title>
</head>
<body>
<@s.form action="signup" method="post" cssClass="ajax">
	<@s.textfield label="%{getText('email')}" name="account.email"
		size="24" cssClass="required email" />
	<@s.textfield label="%{getText('username')}"
		name="account.username" size="24" />
	<@s.password label="%{getText('password')}" name="password" size="24" />
	<@s.password label="%{getText('confirmPassword')}"
		name="confirmPassword" size="24" />
	<@s.submit value="%{getText('signup')}" />
</@s.form>
</body>
</html></#escape>
