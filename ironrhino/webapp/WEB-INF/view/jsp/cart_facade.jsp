<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<div id="cart" class="round_corner" style="clear:both;">
<div id="cart_header">购物车</div>
<div class="round_corner container" corners="top">
<div id="cart_items"><s:if test="%{cart.order.items.size()>0}">
	<s:iterator value="cart.order.items">
		<img class="cart_item draggable"
			src="<s:url value="%{'/pic/' + productCode + '.small.jpg'}"/>"
			alt="<s:property
			value="productCode"/>" />
		<span><s:property
			value="%{productName+(quantity>1?'('+quantity+')':'')}" /></span>
	</s:iterator>
</s:if><s:else>
您的购物车是空的,您可以拖动图片到这里加入购物车
</s:else></div>
</div>
<div style="height: 5px;"></div>
<div id="cart_footer"><a href="<s:url value="/cart"/>">管理</a>/<a
	href="<s:url value="/account/order/addressee"/>">结算</a></div>
</div>