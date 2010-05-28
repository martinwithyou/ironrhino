<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
</head>
<body>
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


<a href="<@url value="/cart/add/${product.code}"/>" class="ajax view" replacement="cart_items">放入购物车</a>
<a href="<@url value="/product/favorite/${product.code}"/>" class="ajax" onerror="login()">加入收藏夹</a>

</div>


<div id="score">
<#if scoreResult.details??>
<div>当前平均分:<span id="score_average">${scoreResult.average}</span>(<span id="score_count">${scoreResult.count}</span>次打分)</div>
<ul style="clear:both;">
	<#list scoreResult.details.entrySet() as var>
		<li>${var.key}分:${var.value}次</li>
	</#list>
</ul>
<#else>
<div>当前还没有评分</div>
</#if>
<ul>
	<li class="current-rating" style="width: 150px;"></li>
	<#list 1..5 as index>
		<li><a href="<@url value="/product/score/${product.code}?score=${index}"/>" title="${index}" class="ajax r${index}-unit" onsuccess="updateScore()" onerror="login()">${index}</a></li>
	</#list>
</ul>
</div>

<div id="comments" style="clear:both;">
<ul>
	<#list resultPage.result as var>
		<li>${var.displayName} says:${var.content}</li>
	</#list>
</ul>
<@pagination class="ajax view" replacement="comments"/>
</div>
<@s.form id="comment_form" action="comment" namespace="/"
	method="post" cssClass="ajax view reset">
	<@s.hidden name="id" value="%{product.code}" />
	<@s.textfield id="comment.displayName"
		label="%{getText('displayName')}" name="comment.displayName" />
	<@s.textfield id="comment.email" label="%{getText('email')}"
		name="comment.email" />
	<@s.textarea id="comment.content" label="%{getText('content')}"
		name="comment.content" cols="50" rows="5" />
	<@captcha/>
	<@s.submit value="submit" />
</@s.form>


<@s.form action="send" namespace="/" method="post"
	cssClass="ajax reset">
	<@s.hidden name="id" value="%{product.code}" />
	<@s.textfield label="%{getText('name')}" name="send.name" />
	<@s.textfield label="%{getText('email')}" name="send.email" />
	<@s.textfield label="%{getText('destination')}"
		name="send.destination" />
	<@s.textfield label="%{getText('message')}" name="send.message"
		size="50" />
	<@captcha/>
	<@s.submit value="submit" />
</@s.form>
</body>
</html></#escape>

