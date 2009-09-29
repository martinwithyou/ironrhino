<#if request.scheme!='https' && !request.servletPath?string?contains('cart')>
	<#assign current=uid!''>
	<#list categoryTreeControl.categoryTree.children as cat>
		<div class="category_top rounded">
		<div class="category_top_title"><a
			href="${base}/product/list/${cat.code}" class="ajax view category<#if cat.code==current> selected</#if>">${cat.name}</a></div>
		<#if cat.children.size() gt 0>
			<div class="category_second rounded">
			<#list cat.children as child>
				<div class="category_third"><span class="category_second_title">
					<a href="${base}/product/list/${child.code}" class="ajax view category<#if child.code==current> selected</#if>">${child.name}</a></span>
					<#if child.children.size() gt 0>:
					<#assign index=0>
					<#list child.children as var>
					<#assign index=index+1>
						<span>
						<a href="${base}/product/list/${var.code}" class="ajax view category<#if var.code==current> selected</#if>">${var.name}</a></span>
						<#if index!=child.children.size()>|</#if>
					</#list>
				</#if></div>
			</#list></div>
		</#if></div>
		<div class="blankline"></div>
	</#list>
	<div class="category_top rounded">
	<div class="category_top_title"><a
		href="${base}/product/list/null" class="ajax view category<#if 'null'==current> selected</#if>">未分类产品</a></div>
	</div>
	<div class="blankline"></div>
	<div class="category_top rounded">
	<div class="category_top_title"><a
		href="${base}/product/list/history" class="ajax view category<#if 'history'==current> selected</#if>">最近浏览过的产品</a></div>
	</div>
</#if>