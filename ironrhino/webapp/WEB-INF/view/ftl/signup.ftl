<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('signup')}</title>
<@authorize ifAnyGranted="ROLE_BUILTIN_USER">
<meta http-equiv="refresh" content="0; url=<@url value="/"/>" />
</@authorize>
</head>
<body>
<@s.form method="post" cssClass="ajax">
	<@s.textfield label="%{getText('email')}" name="email" size="24" cssClass="required email" />
	<@s.textfield label="%{getText('username')}" name="username" size="24" />
	<@s.password label="%{getText('password')}" name="password" size="24" cssClass="required"/>
	<@s.password label="%{getText('confirmPassword')}" name="confirmPassword" size="24" cssClass="required"/>
	<p>
	<@s.submit value="%{getText('signup')}" theme="simple" cssClass="primary"/>
	<@button type="link" text="${action.getText('login')}" href="${getUrl('/login')}"/>
	<@button type="link" text="${action.getText('forgot')}" href="${getUrl('/signup/forgot')}"/>
	</p>
</@s.form>
</body>
</html></#escape>