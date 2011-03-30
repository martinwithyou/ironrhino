<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText(name)}</title>
</head>
<body>
<div class="crumbs"> 
${action.getText('current.location')}:
<a href="<@url value="/"/>">${action.getText('index')}</a><span>&gt;</span>
	${action.getText(name)}
</div>
<div class="clearfix issue ${name}">
<#if page??>
<div class="chapter">
	<h3 class="title">${page.title!}</h3>
	<div class="date">${page.createDate?date}</div>
	<div class="content">
		<@includePage path="${page.path}"/>
	</div>
</div>
</#if>
</div>
</body>
</html></#escape>
