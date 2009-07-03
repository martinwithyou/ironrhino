<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="http://code.google.com/p/ironrhino" prefix="ir"%>
<c:if
	test="${(pageContext.request.scheme!='https')&&fn:indexOf(pageContext.request.servletPath,'cart')<0}">
	<s:action name="cart!facade" namespace="/" executeResult="true" />
</c:if>

<c:if
	test="${(pageContext.request.scheme!='https')&&fn:indexOf(pageContext.request.servletPath,'cart')<0}">
	<ir:cache key="product" scope="session" timeToLive="300">
		<div><img
			src="<s:url value="%{'/pic/' + relatedProduct.code + '.small.jpg'}"/>"
			alt="<s:property
			value="relatedProduct.code" />"
			class="product_list" /></div>
		<div><a
			href="<s:url value="%{'/product/' + relatedProduct.code + '.html'}"/>"><s:property
			value="relatedProduct.name" /></a></div>
		<div><a
			href="<s:url value="%{'/cart/add/'+relatedProduct.code}"/>"
			class="ajax view" options="{replacement:'cart_items'}">放入购物车</a></div>
	</ir:cache>
</c:if>

