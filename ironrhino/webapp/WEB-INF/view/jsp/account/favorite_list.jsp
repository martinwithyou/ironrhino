<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<form action="<s:url value="/account/favorite/delete"/>"
	class="ajax view">
<table class="sortable">
	<thead>
		<tr>
			<th class="nosort"><input type="checkbox" /></th>
			<th><s:property value="getText('productName')" /></th>
			<th><s:property value="getText('addDate')" /></th>
			<th class="nosort">Actions</th>
		</tr>
	</thead>
	<tbody>
		<s:iterator value="list" status="status">
			<tr>
				<td><input type="checkbox" name="id"
					value="<s:property
					value="id" />" /></td>
				<td><a
					href="<s:url value="%{'/product/'+productCode+'.html'}"/>"><s:property
					value="productName" /></a></td>
				<td><s:property value="addDate" /></td>
				<td><a
					href="<s:url value="%{'/account/favorite/delete/'+id}"/>"
					class="ajax view">delete</a></td>
			</tr>
		</s:iterator>
	</tbody>
	<tfoot>
		<tr>
			<td colspan="4"><input type="submit" value="delete" /></td>
		</tr>
	</tfoot>
</table>
</form>
</body>
</html>

