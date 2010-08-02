<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText(actionName)}</title>
<style>
.series .index{
float:left;
margin:20px;
}
.series .chapter{
float:left;
margin-top:20px;
}
.series .index .selected{
font-weight:bold;
}
</style>
</head>
<body>
<div class="clearfix series ${actionName}">
<ul class="index">
<#list pages as var>
<#assign selected=page?? && page.path==var.path/>
<li<#if selected> class="selected"</#if>><#if selected><span><#else><a href="<@url value="/${actionName}/p${var.path}"/>" class="ajax view"></#if>${var.title}<#if selected></span><#else></a></#if></li>
</#list>
</ul>
<#if page??>
<div class="chapter">
<div class="title"><#if page.title??><#assign title=page.title?interpret><@title/></#if></div>
<div class="content">
<#assign designMode=(Parameters.designMode!)=='true'&&statics['org.ironrhino.core.util.AuthzUtils'].authorize("","ROLE_ADMINISTRATOR","","")>
<#if designMode>
<div class="editme" url="<@url value="/common/page/editme?id=${page.id}"/>" name="page.content">
</#if>
<#if page.content??><#assign content=page.content?interpret><@content/></#if>
<#if designMode>
</div>
</#if>
</div>
</div>
</#if>
</div>
</body>
</html></#escape>
