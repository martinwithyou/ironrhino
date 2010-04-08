<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
</head>
<body>
<div id="list">
<table>
	<tr>
		<th>${action.getText('code')}</th>
		<th>${action.getText('username')}</th>
		<th>${action.getText('name')}</th>
		<th>${action.getText('status')}</th>
		<th>${action.getText('createDate')}</th>
		<th>Actions</th>
	</tr>
	<#list resultPage.result as var>
		<tr>
			<td>${var.code}</td>
			<td>${var.user.username}</td>
			<td>${var.user.name}</td>
			<td>${var.status.displayName}</td>
			<td>${var.createDate}</td>
			<td><a href="<@url value="/account/order/view/${var.code}"/>">view</a>
			<#if var.status.getName()=='INITIAL'>
				<a href="<@url value="/account/order/view/${var.code}"/>">pay</a>
				<a href="<@url value="/account/order/cancel/${var.code}"/>">cancel</a>
			</#if><#if var.status.getName()=='CANCELLED'>
				<a href="<@url value="/account/order/delete/${var.code}"/>">delete</a>
			</#if></td>
		</tr>
	</#list>
</table>
</div>
<@pagination class="ajax view" replacement="list"/>
</body>
</html></#escape>

