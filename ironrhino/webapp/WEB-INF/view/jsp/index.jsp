<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="authz"%>
<%@ taglib uri="http://ehcache.sourceforge.net" prefix="ehcache"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<!--ehcache:cache key="products"-->
<s:set name="pl" value="productFacade.recommendedProducts" />
<s:if test="%{#pl!=null&&#pl.size()>0}">
	<div style="clear:both;">
	<div class="round_corner" style="background: #fcc;">
	<h1>每周精选</h1>
	</div>
	<ul class="product_list">
		<s:iterator value="#pl">
			<li><img src="<s:url value="%{'/pic/' + code + '.small.jpg'}"/>"
				alt="<s:property
			value="code" />" class="product_list" /> <a
				href="<s:url value="%{'/product/' + code + '.html'}"/>"
				class="ajax view tooltip product_view"
				title="<s:property
				value="shortDescription" />"><s:property
				value="name" /></a> <a
				id="add_cart_<s:property value="code" />"
				href="<s:url value="%{'/cart/add/'+code}"/>" class="ajax view"
				options="{replacement:'cart_items'}">放入购物车</a></li>
		</s:iterator>
	</ul>
	</div>
</s:if>
<s:set name="pl" value="productFacade.getTopSaleProducts(4)" />
<s:if test="%{#pl!=null&&#pl.size()>0}">
	<div style="clear:both;">
	<div class="round_corner" style="background: #fcc;">
	<h1>畅销排行</h1>
	</div>
	<ul class="product_list">
		<s:iterator value="#pl">
			<li><img
				src="<s:url value="%{'/pic/' + principal.code + '.small.jpg'}"/>"
				alt="<s:property
			value="principal.code" />"
				class="product_list " />
			<div><s:property value="count" /> sales</div>
			<a href="<s:url value="%{'/product/' + principal.code + '.html'}"/>"><s:property
				value="principal.name" /></a> <a
				href="<s:url value="%{'/cart/add/'+principal.code}"/>"
				class="ajax view" options="{replacement:'cart_items'}">放入购物车</a></li>
		</s:iterator>
	</ul>
	</div>
</s:if>
<s:set name="pl" value="productFacade.getTopScoreProducts(4)" />
<s:if test="%{#pl!=null&&#pl.size()>0}">
	<div style="clear:both;">
	<div class="round_corner" style="background: #fcc;">
	<h1>得分排行</h1>
	</div>
	<ul class="product_list">
		<s:iterator value="#pl">
			<li><img
				src="<s:url value="%{'/pic/' + principal.code + '.small.jpg'}"/>"
				alt="<s:property
			value="principal.code" />" class="product_list" />
			<div><s:property value="average" />/5(<s:property
				value="count" /> votes)</div>
			<a href="<s:url value="%{'/product/' + principal.code + '.html'}"/>"><s:property
				value="principal.name" /></a> <a
				href="<s:url value="%{'/cart/add/'+principal.code}"/>"
				class="ajax view" options="{replacement:'cart_items'}">放入购物车</a></li>
		</s:iterator>
	</ul>
	</div>
</s:if>
<s:set name="pl" value="productFacade.getTopFavoriteProducts(4)" />
<s:if test="%{#pl!=null&&#pl.size()>0}">
	<div style="clear:both;">
	<div class="round_corner" style="background: #fcc;">
	<h1>收藏排行</h1>
	</div>
	<ul class="product_list">
		<s:iterator value="#pl">
			<li><img
				src="<s:url value="%{'/pic/' + principal.code + '.small.jpg'}"/>"
				alt="<s:property
			value="principal.code" />" class="product_list" />
			<div><s:property value="count" /> favorites</div>
			<a href="<s:url value="%{'/product/' + principal.code + '.html'}"/>"><s:property
				value="principal.name" /></a> <a
				href="<s:url value="%{'/cart/add/'+principal.code}"/>"
				class="ajax view" options="{replacement:'cart_items'}">放入购物车</a></li>
		</s:iterator>
	</ul>
	</div>
</s:if>
<s:set name="pl" value="productFacade.getTopSendProducts(4)" />
<s:if test="%{#pl!=null&&#pl.size()>0}">
	<div style="clear:both;">
	<div class="round_corner" style="background: #fcc;">
	<h1>推荐排行</h1>
	</div>
	<ul class="product_list">
		<s:iterator value="#pl">
			<li><img
				src="<s:url value="%{'/pic/' + principal.code + '.small.jpg'}"/>"
				alt="<s:property
			value="principal.code" />" class="product_list" />
			<div><s:property value="count" /> sendings</div>
			<a href="<s:url value="%{'/product/' + principal.code + '.html'}"/>"><s:property
				value="principal.name" /></a> <a
				href="<s:url value="%{'/cart/add/'+principal.code}"/>"
				class="ajax view" options="{replacement:'cart_items'}">放入购物车</a></li>
		</s:iterator>
	</ul>
	</div>
</s:if>
<!--/ehcache:cache-->
</body>
</html>
