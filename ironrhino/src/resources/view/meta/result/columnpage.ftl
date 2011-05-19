<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText(name)}</title>
</head>
<body>
<div class="crumbs"> 
${action.getText('current.location')}:
<a href="<@url value="/"/>">${action.getText('index')}</a><span>&gt;</span>
<#if !column??>
	${action.getText(name)}
<#else>
	<a href="<@url value="/${name}"/>">${action.getText(name)}</a><span>&gt;</span>
	${column!}
</#if>
</div>
<div class="clearfix column ${name}">
<ul class="catalog">
<#list columns as var>
<#assign selected=column?? && column==var/>
<li<#if selected> class="selected"</#if>><a href="<@url value="/${name}/list/${var}"/>" class="ajax view history">${var}</a></li>
</#list>
</ul>
<#if page??>
<div class="chapter">
	<div class="content">
		<@includePage path="${page.path}"/>
	</div>
</div>
</#if>
</div>
</body>
</html></#escape>
