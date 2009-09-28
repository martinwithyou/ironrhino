<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<form action="delete" class="ajax view">
<table class="sortable">
	<thead>
		<tr>
			<th class="nosort"><input type="checkbox" /></th>
			<th>${action.getText('productName')}</th>
			<th>${action.getText('addDate')}</th>
			<th class="nosort">${action.getText('actions')}</th>
		</tr>
	</thead>
	<tbody id="list">
		<#list resultPage.result as var>
			<tr>
				<td><input type="checkbox" name="id" value="${var.id}" /></td>
				<td><a href="${base}/product/view/${var.productCode}">${var.productName}</a></td>
				<td>${var.addDate}</td>
				<td><a href="delete/${var.id}" class="ajax view">delete</a></td>
			</tr>
		</#list>
		<tr>
			<td colspan="4"><@pagination class="ajax view" options="{'replacement':'list'}"/></td>
		</tr>
	</tbody>
	<tfoot>
		<tr>
			<td colspan="4"><@s.submit value="%{getText('delete')}" theme="simple"/></td>
		</tr>
	</tfoot>
</table>
</form>
</html></#escape>