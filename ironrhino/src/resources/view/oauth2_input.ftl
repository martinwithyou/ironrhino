<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('grant')}</title>
</head>
<body>
	<#if !displayForNative>
	<#if client??>
	<div>grant access of ${authorization.scope!} to <strong title="${client.description!}" class="tiped">${client.name}</strong></div>
	</#if>
	<@s.form id="grant_form" action="grant" method="post">
		<#if id??><@s.hidden name="id" /></#if>
		<#if client_id??><@s.hidden name="client_id" /></#if>
		<#if redirect_uri??><@s.hidden name="redirect_uri" /></#if>
		<#if scope??><@s.hidden name="scope" /></#if>
		<#if response_type??><@s.hidden name="response_type" /></#if>
		<#if state??><@s.hidden name="state" /></#if>
		<@authorize ifNotGranted="ROLE_BUILTIN_USER">
			<@s.textfield label="%{getText('username')}" name="username"/>
			<@s.password label="%{getText('password')}" name="password"/>
		<@captcha/>
		</@authorize>
		<div class="field">
		<@s.submit value="%{getText('grant')}" theme="simple"/>
		<@s.submit value="%{getText('deny')}" theme="simple" onclick="document.getElementById('grant_form').action='deny';"/>
		</div>
	</@s.form>
	</#if>
</body>
</html></#escape>
