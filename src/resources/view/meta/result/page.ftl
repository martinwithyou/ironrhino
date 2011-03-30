<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>
<#if preview>[${action.getText('preview')}]</#if><#if page.title??><#assign title=page.title?interpret><@title/></#if></title>
<#noescape>${page.head!}</#noescape>
</head>
<body>
<div class="page content">
	<#assign designMode=(Parameters.designMode!)=='true'&&statics['org.ironrhino.core.util.AuthzUtils'].authorize("","ROLE_ADMINISTRATOR","","")>
	<#if designMode>
	<div class="editme" url="<@url value="/common/page/editme?id=${page.id}"/>" name="page.content">
	</#if>
	<#if page.content??><#assign content=page.content?interpret><@content/></#if>
	<#if designMode>
	</div>
	</#if>
</div>
</body>
</html></#escape>
