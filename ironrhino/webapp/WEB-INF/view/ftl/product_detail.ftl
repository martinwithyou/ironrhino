<div id="detail">
<table>
	<tr>
		<td>code</td>
		<td>${product.code}</td>
	</tr>
	<tr>
		<td>name</td>
		<td>${product.name}</td>
	</tr>
</table>

<a href="<#if base?exists>${base}<#else>${base}</#if>/cart/add/${product.code}" class="ajax view" replacement="cart_items">add to cart</a>
<a href="<#if base?exists>${base}<#else>${base}</#if>/product/favorite/${product.code}" class="ajax" onerror="login()">add to favorite</a>
</div>

