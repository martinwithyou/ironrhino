<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<div id="info">
<table>
	<tr>
		<td>${action.getText('code')}</td>
		<td>${order.code}</td>
	</tr>
	<tr>
		<td>${action.getText('createDate')}</td>
		<td>${order.createDate}</td>
	</tr>
	<tr>
		<td>${action.getText('status')}</td>
		<td>${order.status.displayName}</td>
	</tr>
	<tr>
		<td>${action.getText('total')}</td>
		<td>${order.total}</td>
	</tr>
	<tr>
		<td>${action.getText('discount')}</td>
		<td>${order.discount}</td>
	</tr>
	<tr>
		<td>${action.getText('shipcost')}</td>
		<td>${order.shipcost}</td>
	</tr>
	<tr>
		<td>${action.getText('grandtotal')}</td>
		<td>${order.grandtotal}</td>
	</tr>
</table>
<table>
	<tr>
		<td>${action.getText('productCode')}</td>
		<td>${action.getText('productName')}</td>
		<td>${action.getText('productPrice')}</td>
		<td>${action.getText('quantity')}</td>
		<td>${action.getText('subtotal')}</td>
	</tr>
	<#list order.items as var>
		<tr>
			<td>${var.productCode}</td>
			<td>${var.productName}</td>
			<td>${var.productPrice}</td>
			<td>${var.quantity}</td>
			<td>${var.subtotal}</td>
		</tr>
	</#list>
</table>
</div>

<div id="addressee">
<table>
	<tr>
		<td>${action.getText('name')}</td>
		<td>${order.addressee.name}</td>
	</tr>
	<tr>
		<td>${action.getText('address')}</td>
		<td>${order.addressee.address}</td>
	</tr>
	<tr>
		<td>${action.getText('postcode')}</td>
		<td>${order.addressee.postcode}</td>
	</tr>
	<tr>
		<td>${action.getText('phone')}</td>
		<td>${order.addressee.phone}</td>
	</tr>
	<tr>
		<td>${action.getText('description')}</td>
		<td>${order.description}</td>
	</tr>
	<#if order.isNew()>
		<tr>
			<td colspan="2"><a
				href="${base}/account/order/addressee">edit</a></td>
		</tr>
	</#if>
</table>
</div>

<div id="operation"><#if order.isNew()>
	<div><a href="${base}/account/order/confirm">confirm</a>&nbsp;<a
		href="${base}/account/order/cancel">cancel</a></div>
<#else>
	<#if order.status.getName()=='INITIAL'>
		<div id="payment"><#list paymentManager.payments as var>
			<#if !var.disabled>
				<div><img src="${base}/images/${code}.jpg" alt="${var.code}" />${var.name}
				${var.getPayForm(order)}</div>
			</#if>
		</#list></div>
		<div><a
			href="${base}/account/order/cancel/${order.code}">cancel</a></div>
	</#if>
	<#if order.status.getName()=='CANCELLED'>
		<div><a
			href="${base}/account/order/delete/${order.code}">delete</a></div>
	</#if>
</#if></div>
</body>
</html></#escape>
