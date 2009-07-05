<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('login')}</title>
<script>
	Initialization.initForm = function() {
		$('#login_form')[0].onsuccess = redirect;
		$('#targetUrl')[0].value = self.location.href;
	}

	function redirect() {
		<#if targetUrl?exists>
			top.location.href = '<#if !targetUrl?string?contains('://')>${base}</#if>${targetUrl}';
		<#else>
			top.location.href = '${base}';
		</#if>
	}
</script>
</head>

<body>
<div class="tabs" tab="#${Parameters.tab?if_exists}">
<ul>
	<li><a href="#login"><span>${action.getText('login')}</span></a></li>
	<li><a href="#openid"><span>${action.getText('login.openid')}</span></a></li>
	<li><a href="#forgot"><span>${action.getText('login.forgot')}</span></a></li>
	<li><a href="#resend"><span>${action.getText('login.resend')}</span></a></li>
</ul>
<div id="login"><@s.form id="login_form" action="check"
	method="post" cssClass="ajax">
	<@s.hidden id="targetUrl" name="targetUrl" />
	<@s.textfield label="%{getText('username')}" name="username"
		labelposition="left" />
	<@s.password label="%{getText('password')}" name="password"
		labelposition="left" />
	<@s.checkbox label="%{getText('rememberme')}" name="rememberme" labelposition="left" />
	<@s.submit value="%{getText('login')}" />
</@s.form></div>
<div id="openid"><@s.form action="/account/openid" method="post"
	cssClass="ajax">
	<@s.hidden name="targetUrl" />
	<@s.textfield label="%{getText('openid')}" name="openid" size="30"
		cssClass="openid" labelposition="left" />
	<@s.submit value="%{getText('login')}" />
</@s.form></div>
<div id="forgot"><@s.form action="forgot" method="post"
	cssClass="ajax reset">
	<@s.textfield label="%{getText('email')}" name="account.email"
		cssClass="required email" labelposition="left" />
	<@s.textfield label="%{getText('captcha')}" name="captcha" size="6"
		cssClass="autocomplete_off required captcha" labelposition="left" />
	<@s.submit value="%{getText('confirm')}" />
</@s.form></div>
<div id="resend"><@s.form action="resend" method="post"
	cssClass="ajax reset">
	<@s.textfield label="%{getText('username')}" name="account.username"
		cssClass="required" labelposition="left" />
	<@s.textfield label="%{getText('email')}" name="account.email"
		cssClass="required email" labelposition="left" />
	<@s.textfield label="%{getText('captcha')}" name="captcha" size="6"
		cssClass="autocomplete_off required captcha" labelposition="left" />
	<@s.submit value="%{getText('confirm')}" />
</@s.form></div>
</div>
</body>
</html>
