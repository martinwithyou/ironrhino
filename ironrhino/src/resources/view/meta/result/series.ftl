<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText(name)}</title>
</head>
<body>
<div class="crumbs"> 
	${action.getText('current.location')}:
	<a href="<@url value="/"/>">${action.getText('index')}</a><span>&gt;</span>
<#if !page??>
	${action.getText(name)}
<#else>
	<a href="<@url value="/${name}"/>">${action.getText(name)}</a><span>&gt;</span>
	${page.title!}
</#if>
</div>
<div class="clearfix series ${name}">
	<ul class="catalog">
		<#list pages as var>
		<#assign selected=page?? && page.path==var.path/>
		<li<#if selected> class="selected"</#if>><#if selected><span><#else><a href="<@url value="/${name}/p${var.path}"/>" class="ajax view"></#if>${var.title}<#if selected></span><#else></a></#if></li>
		</#list>
	</ul>
	<#if page??>
	<div class="chapter">
		<div class="content">
			<@includePage path="${page.path}"/>
		</div>
		<#if showBar>
		<div class="bar">
			<#if previousPage??><a href="<@url value="/${name}/p${previousPage.path}"/>" class="ajax view">${action.getText('previouspage')}:${previousPage.title}</a></#if>
			<#if nextPage??><a href="<@url value="/${name}/p${nextPage.path}"/>" class="ajax view">${action.getText('nextpage')}:${nextPage.title}</a></#if>
		</div>
		</#if>
	</div>
	</#if>
</div>
</body>
</html></#escape>
