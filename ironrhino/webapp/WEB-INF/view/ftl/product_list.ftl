<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<div id="${list?if_exists}_list_detail">
<div class="product_list">
<#list resultPage.result as var>
	<p><img src="${base}/pic/${var.code}.small.jpg"
		alt="${var.code}" class="product_list" /> <a
		href="${base}/product/${var.code}.html">${var.name}</a> 
		<a href="${base}/cart/add/${var.code}"
		class="ajax view" options="{replacement:'cart_items'}">放入购物车</a></p>
</#list>
</div>
<@pagination class="ajax view" options="{replacement:'${list?if_exists}_list_detail',cache:true}"/>
</div>
</body>
</html></#escape>