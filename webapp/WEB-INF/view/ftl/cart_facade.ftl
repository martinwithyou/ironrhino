<div id="cart" class="rounded" style="clear:both;">
<div id="cart_header">购物车</div>
<div class="rounded container">
<div id="cart_items">
<#if cart.order.items.size() gt 0>
<ul>
	<#list cart.order.items as var>
		<li><img class="cart_item draggable"
			src="<@url value="/product/${var.productCode}.s.jpg"/>"
			alt="${var.productCode}" />
		<span>${var.productName}<#if var.quantity gt 1>(${var.quantity})</#if></span>
		</li>
	</#list>
</ul>
<#else>
您的购物车是空的,您可以拖动图片到这里加入购物车
</#if>
</div>
</div>
<div style="height: 5px;"></div>
<div id="cart_footer"><a href="<@url value="/cart"/>">管理</a>/<a
	href="<@url value="/account/order/addressee"/>">结算</a></div>
</div>