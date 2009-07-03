<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<form action="${base}/cart/update" method="post"
	class="ajax view" options="{replacement:'detail'}">
<div id="detail">
<table class="sortable">
	<thead>
		<tr>
			<th class="nosort"><input type="checkbox" /></th>
			<th>name</th>
			<th>price</th>
			<th class="nosort">quantity</th>
			<th>subtotal</th>
			<th class="nosort">remove</th>
		</tr>
	</thead>
	<tfoot>
		<tr>
			<td></td>
			<td>name</td>
			<td>price</td>
			<td>quantity</td>
			<td>subtotal</td>
			<td>remove</td>
		</tr>
	</tfoot>
	<tbody>
		<#assign index=0>
		<#list cart.order.items?if_exists as var>
			<tr class="row">
				<td><input type="checkbox" name="id"
					value="${var.productCode}" /></td>
				<td><a href="${base}/product/${var.productCode}.html"> ${var.productName}</a></td>
				<td>${var.productPrice}</td>
				<td><input type="hidden" name="items[${index}].productCode" value="${var.productCode}" /> 
				<input type="text" size="2" name="items[${index}].quantity" value="${var.quantity}" /></td>
				<td>${var.subtotal}</td>
				<td><a href="${base}/cart/remove/${var.productCode}" class="ajax view"
					options="{replacement:'detail',onprepare:'confirm(\'are you sure to remove ${var.productCode}\')'}">remove</a></td>
			</tr>
			<#assign index=index+1>
		</#list>
	</tbody>
</table>
</div>
<div id="actions"><a class="btn goback"><span><span>继续购物</span></span></a>
<span><@s.submit value="更新购物车" theme="simple" /></span> <a
	href="${base}/cart/remove"
	class="btn ajax view delete_selected" options="{replacement:'detail'}"><span><span>删除选中</span></span></a>
<a id="clear" href="${base}/cart/clear" class="btn ajax view"
	options="{replacement:'detail',onprepare:'confirm(\'are you sure to clear cart\')'}"><span><span>清空</span></span></a>
<a id="checkout"
	href="${base}/account/order/addressee" class="btn"><span><span>结算</span></span></a> </div>
</form>
</body>
</html>
