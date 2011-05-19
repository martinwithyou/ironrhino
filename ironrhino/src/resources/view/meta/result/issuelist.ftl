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
<div class="list">
<dl>
<#list resultPage.result as page>
	<dd>
		<a href="<@url value="/${name}/p${page.path}"/>"><#if page.title??><#assign title=page.title?interpret><@title/></#if></a><span class="date">${page.createDate?date}</span>
	</dd>
</#list>
</dl>
<@pagination class="ajax view history" replacement="list" cache="true"/>
</div>
</div>
</body>
</html></#escape>
