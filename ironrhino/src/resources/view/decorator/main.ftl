<#assign requestURI=request.requestURI?substring(request.contextPath?length)/>
<#assign modernBrowser = false/>
<#assign ua = request.getAttribute('userAgent')/>
<#if ua?? && (ua.name!='msie' || ua.majorVersion gt 8)>
<#assign modernBrowser = true/>
</#if>
<#if modernBrowser>
<!DOCTYPE html>
<html>
<#else>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
</#if>
<#compress><#escape x as x?html>
<head>
<title><#noescape>${title}</#noescape></title>
<#if modernBrowser>
<meta charset="utf-8">
<#else>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
</#if>
<#if request.contextPath!=''>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="context_path" content="${request.contextPath}" />
</#if>
<link rel="shortcut icon" href="<@url value="/assets/images/favicon.ico"/>" />
<link href="<@url value="/assets/styles/ironrhino${modernBrowser?string('-min','')}.css"/>" media="all" rel="stylesheet" type="text/css" />
<#if !modernBrowser><link href="<@url value="/assets/styles/ie.css"/>" media="all" rel="stylesheet" type="text/css" /></#if>
<script src="<@url value="/assets/scripts/ironrhino${modernBrowser?string('-min','')}.js"/>" type="text/javascript"></script>
<#noescape>${head}</#noescape>
</head>

<body style="padding-top: 60px;padding-bottom: 30px;">

<@authorize ifAnyGranted="ROLE_BUILTIN_USER">
<div class="navbar navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container">
      <a class="brand" href="<@url value="/"/>">ironrhino</a>
      <div class="btn-group pull-right">
        <a href="#" class="btn dropdown-toggle" data-toggle="dropdown">
          <i class="icon-user"></i>${authentication("principal")?string} <span class="caret"></span>
        </a>
        <ul class="dropdown-menu">
          <li><a href="<@url value="${ssoServerBase!}/user/profile"/>">${action.getText('profile')}</a></li>
          <li><a href="<@url value="${ssoServerBase!}/user/password"/>">${action.getText('change')}${action.getText('password')}</a></li>
          <li class="divider"></li>
          <li><a href="<@url value="${ssoServerBase!}/logout"/>">${action.getText('logout')}</a></li>
        </ul>
      </div>
      <div class="nav-collapse">
        <ul class="nav">
          <li><a href="<@url value="/"/>">${action.getText('index')}</a></li>
          <@authorize ifAnyGranted="ROLE_ADMINISTRATOR">
          <li><a href="<@url value="/user"/>">${action.getText('user')}</a></li>
          </@authorize>
        </ul>
      </div>
    </div>
  </div>
</div>
</@authorize>
<@authorize ifNotGranted="ROLE_BUILTIN_USER">
<div class="container">
<h1 style="text-align:center;margin:10px 0;">
${title}
</h1>
</div>
</@authorize>

<div id="content" class="container">
<#if action.hasActionMessages() || action.hasActionErrors()>
<div id="message">
<@s.actionerror />
<@s.actionmessage />
</div>
</#if>
<#noescape>${body}</#noescape>
</div>


</body>
</html></#escape></#compress>
