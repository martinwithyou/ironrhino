<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('access.denied')}</title>
<@authorize ifNotGranted="ROLE_BUILTIN_USER">
<meta http-equiv="refresh" content="0; url=<@url value="${ssoServerBase!}/login?targetUrl=${request.requestURL?url}"/>" />
</@authorize>
</head>
<body>
<div style="text-align:center;">${action.getText('access.denied')}</div>
</body>
</html></#escape>