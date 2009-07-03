<div id="cart" class="round_corner" style="clear:both;">
<div id="cart_header">购物车</div>
<div class="round_corner container" corners="top">
<div id="cart_items">
<#if cart.order.items.size() gt 0>
	<#list cart.order.items as var>
		<img class="cart_item draggable"
			src="${base}/pic/${var.productCode}.small.jpg"
			alt="${var.productCode}" />
		<span>${var.productName}<#if var.quantity gt 1>${var.quantity}</#if><</span>
	</#list>
<#else>
您的购物车是空的,您可以拖动图片到这里加入购物车
</#if>
</div>
</div>
<div style="height: 5px;"></div>
<div id="cart_footer"><a href="${base}/cart">管理</a>/<a
	href="${base}/account/order/addressee">结算</a></div>
</div>