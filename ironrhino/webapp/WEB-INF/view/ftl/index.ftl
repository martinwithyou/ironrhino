<#macro detail product info="">
<li><img src="${base}/pic/${product.code}.small.jpg" alt="${product.code}" class="product_list" />
<#if info!="">
<div>${info}</div>
</#if>
<a href="${base}/product/${product.code}.html" class="ajax view tooltip product_view">
${product.name}</a> <a href="${base}/cart/add/${product.code}" class="ajax view" options="{replacement:'cart_items'}">放入购物车</a></li>
</#macro>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<@cache key="index_page">
<#assign list=productFacade.getRecommendedProducts()>
<#if  list?exists && list.size() gt 0>
	<div style="clear:both;">
	<div class="round_corner" style="background: #fcc;">
	<h1>每周精选</h1>
	</div>
	<ul class="product_list">
		<#list list as product>
			<@detail product=product/>
		</#list>
	</ul>
	</div>
</#if>
<#assign list=productFacade.getTopSaleProducts(4)>
<#if list?exists && list.size() gt 0>
	<div style="clear:both;">
	<div class="round_corner" style="background: #fcc;">
	<h1>畅销排行</h1>
	</div>
	<ul class="product_list">
		<#list list as var>
		<#assign info=var.count+' sales'>
		<@detail product=var.principal info=info/>
		</#list>
	</ul>
	</div>
</#if>

<#assign list=productFacade.getTopScoreProducts(4)>
<#if list?exists && list.size() gt 0>
	<div style="clear:both;">
	<div class="round_corner" style="background: #fcc;">
	<h1>得分排行</h1>
	</div>
	<ul class="product_list">
		<#list list as var>
		<#assign info=var.average+'/5('+var.count+' votes)'>
		<@detail product=var.principal info=info/>
		</#list>
	</ul>
	</div>
</#if>

<#assign list=productFacade.getTopFavoriteProducts(4)>
<#if list?exists && list.size() gt 0>
	<div style="clear:both;">
	<div class="round_corner" style="background: #fcc;">
	<h1>收藏排行</h1>
	</div>
	<ul class="product_list">
		<#list list as var>
		<#assign info=var.count+' favorites)'>
		<@detail product=var.principal info=info/>
		</#list>
	</ul>
	</div>
</#if>

<#assign list=productFacade.getTopSendProducts(4)>
<#if list?exists && list.size() gt 0>
	<div style="clear:both;">
	<div class="round_corner" style="background: #fcc;">
	<h1>推荐排行</h1>
	</div>
	<ul class="product_list">
		<#list list as var>
		<#assign info=var.count+' sendings)'>
		<@detail product=var.principal info=info/>
		</#list>
	</ul>
	</div>
</#if>
</@cache>
</body>
</html>
