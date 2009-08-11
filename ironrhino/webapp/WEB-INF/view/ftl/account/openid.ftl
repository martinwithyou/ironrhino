<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<#if authRequest?exists>
	<form id="openid-form-redirection" action="${authRequest.OPEndpoint}" method="post" accept-charset="utf-8">
	<#list authRequest.parameterMap.entrySet as entry>
		<input type="hidden" name="${entry.key}" value="${entry.value}" />
	</#list>
	<button type="submit">Continue...</button>
	</form>
	<script>document.getElementById('openid-form-redirection').submit();</script>
</#if>
</body>
</html></#escape>
