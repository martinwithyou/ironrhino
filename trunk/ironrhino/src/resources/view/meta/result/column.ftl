<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText(actionName)}</title>
</head>
<body>
<div class="crumbs"> 
${action.getText('current.location')}:
<a href="<@url value="/"/>">${action.getText('index')}</a><span>&gt;</span>
<#if !column??>
	${action.getText(actionName)}
<#else>
	<a href="<@url value="/${actionName}"/>">${action.getText(actionName)}</a><span>&gt;</span>
	${column!}
</#if>
</div>
<div class="clearfix column ${actionName}">
<ul class="catalog">
<#list columns as var>
<#assign selected=column?? && column==var/>
<li<#if selected> class="selected"</#if>><#if selected><span><#else><a href="<@url value="/${actionName}/list/${var}"/>" class="ajax view"></#if>${var}<#if selected></span><#else></a></#if></li>
</#list>
</ul>
<#if column??>
<div class="list">
<dl>
<#list resultPage.result as page>
	<dd>
		<a href="<@url value="/p${page.path}"/>"><#if page.title??><#assign title=page.title?interpret><@title/></#if></a>
	</dd>
</#list>
</dl>
<@pagination class="ajax view" replacement="list" cache="true"/>
</div>
</#if>
</div>
</body>
</html></#escape>
