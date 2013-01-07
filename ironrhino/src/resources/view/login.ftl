<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('login')}</title>
<@authorize ifAnyGranted="ROLE_BUILTIN_USER">
<meta http-equiv="refresh" content="0; url=<@url value="/"/>" />
</@authorize>
</head>
<body>
<@authorize ifNotGranted="ROLE_BUILTIN_USER">
<div class="row">
	<div class="span4 offset4">
	<@s.form id="login" action="login" method="post" cssClass="ajax focus form-horizontal well">
		<@s.hidden id="targetUrl" name="targetUrl" />
		<@s.textfield label="%{getText('username')}" name="username" cssClass="required span2"/>
		<@s.password label="%{getText('password')}" name="password" cssClass="required span2"/>
		<@s.checkbox label="%{getText('rememberme')}" name="rememberme" cssClass="custom"/>
		<@captcha/>
		<@s.submit value="%{getText('login')}" cssClass="btn-primary">
		<#if getSetting??&&'true'==getSetting('signup.enabled')>
		<@s.param name="after"> <a class="btn" href="${getUrl('/signup')}">${action.getText('signup')}</a></@s.param>
		</#if>
		</@s.submit>
	</@s.form>
	</div>
</div>
<#if getSetting??&&'true'==getSetting('signup.enabled')&&'true'==getSetting('oauth.enabled')>
<div class="ajaxpanel" data-url="<@url value="/oauth/connect"/>">
</div>
</#if>
</@authorize>
</body>
</html></#escape>
