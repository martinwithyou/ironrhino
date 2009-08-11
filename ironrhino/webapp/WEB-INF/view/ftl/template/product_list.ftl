<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>ironrhino</title>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
</head>
<body>
<table>
<tr>
	<th>code</th>
	<th>name</th>
</tr>
		
<#list productList as product>
<tr>
	<td>${product.code}</td>
	<td>${product.name}</td>
</tr>
</#list>
</table>
</body>
</html></#escape>
