<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>search</title>
</head>
<body>
<@cache key='search'+Parameters.q! timeToLive="1800">
<div id="search_result">
<#if searchResults??>
 Search took ${searchResults.searchTime}ms
 <#if searchResults.hits??>
		<#list searchResults.hits as var>
			<p><a href="<@url value="/product/view/${var.data().code}"/>">${var.data().name}</a>(${action.formatScore(var.score())})<br />
			${var.data().shortDescription!}</p>
		</#list>
		<#if searchResults.pages??>
			<p>
			<#assign index=0>
			<#list searchResults.pages as var>
				<#assign index=index+1>
				<#if var.selected>
					<span>${var.from}-${var.to}</span>
					<#else>
					<a href="<@url value="/search?q=${q}&amp;pn=${index}&amp;ps=${ps}"/>">${var.from}-${var.to}</a>
				</#if>
			</#list></p>
		</#if>
	<#else>
	No matched result
	</#if>
</#if>
</div>
</@cache>
</body>
</html></#escape>
