<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
</head>
<body>
<form action="<@url value="/cart/update"/>" method="post" class="ajax view" replacement="detail">
<div id="detail">
<table class="sortable">
	<thead>
		<tr>
			<th class="nosort"><input type="checkbox" /></th>
			<th>name</th>
			<th>price</th>
			<th class="nosort">quantity</th>
			<th>subtotal</th>
			<th class="nosort"></th>
		</tr>
	</thead>
	<tbody>
		<#assign index=0>
		<#list cart.order.items! as var>
			<tr class="row">
				<td><input type="checkbox" name="id"
					value="${var.productCode}" /></td>
				<td><a href="<@url value="/product/view/${var.productCode}"/>">${var.productName}</a></td>
				<td>${var.productPrice}</td>
				<td><input type="hidden" name="items[${index}].productCode" value="${var.productCode}" />
				<input type="text" size="2" name="items[${index}].quantity" value="${var.quantity}" /></td>
				<td>${var.subtotal}</td>
				<td><@button type="link" text="${action.getText('remove')}" href="${getUrl('/cart/remove/'+var.productCode)}" class="ajax view" replacement="detail" onprepare="confirm('are you sure to remove ${var.productCode}')"/></td>
			</tr>
			<#assign index=index+1>
		</#list>
	</tbody>
</table>
</div>
<div>
<@button text="继续购物" onclick="history.go(-1)" />
<@s.submit value="更新购物车" theme="simple" />
<@button type="link" text="删除选中" href="${getUrl('/cart/remove')}" class="ajax view delete_selected" replacement="detail"/>
<@button type="link" text="清空" href="${getUrl('/cart/clear')}" class="ajax view" replacement="detail" onprepare="confirm('are you sure to clear cart')"/>
<@button type="link" text="结算" href="${getUrl('/account/order/addressee')}"/>
</div>
</form>
</body>
</html></#escape>
