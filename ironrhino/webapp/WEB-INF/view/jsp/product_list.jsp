<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>

<div id="<s:property value="%{list}" />_list_detail">
<div class="product_list"><s:iterator value="resultPage.result">
	<p><img src="<s:url value="%{'/pic/' + code + '.small.jpg'}"/>"
		alt="<s:property value="code" />" class="product_list" /> <a
		href="<s:url value="%{'/product/' + code + '.html'}"/>"><s:property
		value="name" /></a> <a href="<s:url value="%{'/cart/add/'+code}"/>"
		class="ajax view" options="{replacement:'cart_items'}">放入购物车</a></p>
</s:iterator></div>

<s:if test="resultPage.totalPage>1">
	<div align="left" style="clear:both;">total records:<s:property
		value="resultPage.totalRecord" /> | <s:if
		test="%{!resultPage.isFirst()}">
		<a
			href="<s:property value="#request['struts.request_uri']+resultPage.renderUrl(1)"/>"
			class="ajax view"
			options="{replacement:'<s:property value="%{list}" />_list_detail',cache:true}">First</a>|
	<a
			href="<s:property value="#request['struts.request_uri']+resultPage.renderUrl(resultPage.previousPage)"/>"
			class="ajax view"
			options="{replacement:'<s:property value="%{list}" />_list_detail',cache:true}">Previous</a>
	</s:if><s:else>First|
				Previous
				</s:else>| <s:if test="%{!resultPage.isLast()}">
		<a
			href="<s:property value="#request['struts.request_uri']+resultPage.renderUrl(resultPage.nextPage)"/>"
			class="ajax view"
			options="{replacement:'<s:property value="%{list}" />_list_detail',cache:true}">Next</a>|
	<a
			href="<s:property value="#request['struts.request_uri']+resultPage.renderUrl(resultPage.totalPage)"/>"
			class="ajax view"
			options="{replacement:'<s:property value="%{list}" />_list_detail',cache:true}">Last</a>
	</s:if> <s:else>
				Next|Last
				</s:else> |<s:property value="resultPage.pageNo" /> / <s:property
		value="resultPage.totalPage" /></div>
</s:if></div>


</body>
</html>

