<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('login')}</title>
<@authorize ifAnyGranted="ROLE_BUILTIN_USER">
<meta http-equiv="refresh" content="0; url=${base}" />
</@authorize>
</head>

<body>
<div class="tabs" tab="#${Parameters.tab?if_exists}">
<ul>
	<li><a href="#login"><span>${action.getText('login')}</span></a></li>
	<li><a href="#forgot"><span>${action.getText('login.forgot')}</span></a></li>
	<li><a href="#resend"><span>${action.getText('login.resend')}</span></a></li>
</ul>
<div id="login"><@s.form id="login_form" action="check"
	method="post" cssClass="ajax view">
	<@s.hidden id="targetUrl" name="targetUrl" />
	<@s.textfield label="%{getText('username')}" name="username" cssClass="required"/>
	<@s.password label="%{getText('password')}" name="password" cssClass="required"/>
	<@s.checkbox label="%{getText('rememberme')}" name="rememberme"/>
	<#if captchaRequired?if_exists>
	<@s.textfield label="%{getText('captcha')}" name="captcha" size="6" cssClass="autocomplete_off required captcha"/>
	</#if>
	<@s.submit value="%{getText('login')}" />
</@s.form></div>
<div id="forgot"><@s.form action="forgot" method="post"
	cssClass="ajax reset">
	<@s.textfield label="%{getText('email')}" name="account.email" cssClass="required email"/>
	<#if captchaRequired?if_exists>
	<@s.textfield label="%{getText('captcha')}" name="captcha" size="6" cssClass="autocomplete_off required captcha"/>
	</#if>
	<@s.submit value="%{getText('confirm')}" />
</@s.form></div>
<div id="resend"><@s.form action="resend" method="post"
	cssClass="ajax reset">
	<@s.textfield label="%{getText('username')}" name="account.username" cssClass="required"/>
	<@s.textfield label="%{getText('email')}" name="account.email" cssClass="required email"/>
	<#if captchaRequired?if_exists>
	<@s.textfield label="%{getText('captcha')}" name="captcha" size="6" cssClass="autocomplete_off required captcha"/>
	</#if>
	<@s.submit value="%{getText('confirm')}" />
</@s.form></div>
</div>
</body>
</html></#escape>
