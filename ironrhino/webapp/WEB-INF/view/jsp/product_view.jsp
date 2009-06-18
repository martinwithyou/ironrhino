<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="authz"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<div id="detail">
<table>
	<tr>
		<td>code</td>
		<td><s:property value="product.code" /></td>
	</tr>
	<tr>
		<td>name</td>
		<td><s:property value="product.name" /></td>
	</tr>
</table>

<a href="<s:url value="%{'/cart/add/'+product.code}"/>"
	class="ajax view" options="{replacement:'cart_items'}">放入购物车</a> <a
	href="<s:url value="%{'/product/favorite/'+product.code}"/>"
	class="ajax" options="{onerror:'login()'}">加入收藏夹</a></div>

<div id="score">
<div>当前平均分:<span id="score_average"><s:property
	value="scoreResult.average" /></span>(<span id="score_count"><s:property
	value="scoreResult.count" /></span>次打分)</div>
<ul class="unit-rating">
	<li class="current-rating" style="width: 150px;"></li>
	<s:iterator value="new int[5]" status="status">
		<li><a
			href="<s:url value="%{'/product/score/'+[1].product.code+'?score='+#status.count}"/>"
			title="<s:property value="#status.count" />"
			class="ajax r<s:property value="#status.count" />-unit"
			options="{onsuccess:'updateScore()',onerror:'login()'}"><s:property
			value="#status.count" /></a></li>
	</s:iterator>
</ul>

<ul style="clear:both;">
	<s:iterator value="scoreResult.details">
		<li><s:property value="key" />分:<s:property value="value" />次</li>
	</s:iterator>
</ul>
</div>

<ul class="related_products" style="clear:both;">
	<s:iterator value="product.relatedProducts">
		<li><img src="<s:url value="%{'/pic/' + code + '.small.jpg'}"/>"
			alt="<s:property value="code" />" class="product_list" /> <a
			href="<s:url value="%{'/product/' + code + '.html'}"/>"><s:property
			value="name" /></a> <a href="<s:url value="%{'/cart/add/'+code}"/>"
			class="ajax view" options="{replacement:'cart_items'}">放入购物车</a></li>
	</s:iterator>
</ul>

<div id="comments">
<ul>
	<s:iterator value="resultPage.result">
		<li><s:property value="displayName" /> says:<s:property
			value="content" /></li>
	</s:iterator>
</ul>
<s:if test="resultPage.totalPage>1">
	<div align="left" style="clear:both;">total records:<s:property
		value="resultPage.totalRecord" /> | <s:if
		test="%{!resultPage.isFirst()}">
		<a
			href="<s:property value="#request['struts.request_uri']+resultPage.renderUrl(1)"/>"
			class="ajax view" options="{replacement:'comments'}">First</a>|
	<a
			href="<s:property value="#request['struts.request_uri']+resultPage.renderUrl(resultPage.previousPage)"/>"
			class="ajax view" options="{replacement:'comments'}">Previous</a>
	</s:if><s:else>First|
				Previous
				</s:else>| <s:if test="%{!resultPage.isLast()}">
		<a
			href="<s:property value="#request['struts.request_uri']+resultPage.renderUrl(resultPage.nextPage)"/>"
			class="ajax view" options="{replacement:'comments'}">Next</a>|
	<a
			href="<s:property value="#request['struts.request_uri']+resultPage.renderUrl(resultPage.totalPage)"/>"
			class="ajax view" options="{replacement:'comments'}">Last</a>
	</s:if> <s:else>
				Next|Last
				</s:else> |<s:property value="resultPage.pageNo" /> / <s:property
		value="resultPage.totalPage" /></div>
</s:if></div>


<s:form id="comment" action="product!comment" namespace="/"
	method="post" cssClass="ajax reset">
	<s:hidden name="id" value="%{product.code}" />
	<s:textfield id="comment.displayName"
		label="%{getText('comment.displayName')}" name="comment.displayName" />
	<s:textfield id="comment.email" label="%{getText('comment.email')}"
		name="comment.email" />
	<s:textarea id="comment.content" label="%{getText('comment.content')}"
		name="comment.content" cols="50" rows="4" />
	<authz:authorize ifNotGranted="ROLE_BUILTIN_ACCOUNT">
		<s:textfield label="%{getText('captcha')}" name="captcha" size="6"
			cssClass="autocomplete_off required captcha" />
	</authz:authorize>
	<s:submit value="submit" />
</s:form>


<s:form action="product!send" namespace="/" method="post"
	cssClass="ajax reset">
	<s:hidden name="id" value="%{product.code}" />
	<s:textfield label="%{getText('send.name')}" name="send.name" />
	<s:textfield label="%{getText('send.email')}" name="send.email" />
	<s:textfield label="%{getText('send.destination')}"
		name="send.destination" />
	<s:textfield label="%{getText('send.message')}" name="send.message"
		size="50" />
	<authz:authorize ifNotGranted="ROLE_BUILTIN_ACCOUNT">
		<s:textfield label="%{getText('captcha')}" name="captcha" size="6"
			cssClass="autocomplete_off required captcha" />
	</authz:authorize>
	<s:submit value="submit" />
</s:form>
</body>
</html>

