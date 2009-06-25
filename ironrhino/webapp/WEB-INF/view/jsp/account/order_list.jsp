<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<table>
	<tr>
		<th><s:property value="getText('code')" /></th>
		<th><s:property value="getText('username')" /></th>
		<th><s:property value="getText('name')" /></th>
		<th><s:property value="getText('status')" /></th>
		<th><s:property value="getText('orderDate')" /></th>
		<th>Actions</th>
	</tr>
	<s:iterator value="resultPage.result">
		<tr>
			<td><s:property value="code" /></td>
			<td><s:property value="account.username" /></td>
			<td><s:property value="account.name" /></td>
			<td><s:property value="status.displayName" /></td>
			<td><s:property value="orderDate" /></td>
			<td><a href="<s:url value="%{'/account/order/view/'+code}"/>">view</a>
			<s:if test="%{status.name=='INITIAL'}">
				<a href="<s:url value="%{'/account/order/view/'+code+'#payment'}"/>">pay</a>
				<a href="<s:url value="%{'/account/order/cancel/'+code}"/>">cancel</a>
			</s:if> <s:if test="%{status.name=='CANCELLED'}">
				<a href="<s:url value="%{'/account/order/delete/'+code}"/>">delete</a>
			</s:if></td>
		</tr>
	</s:iterator>
</table>
<div align="left">total records:<s:property
	value="resultPage.totalRecord" /> | <s:if
	test="%{!resultPage.isFirst()}">
	<a
		href="<s:property value="#request['struts.request_uri']+resultPage.renderUrl(1)"/>"
		class="ajax view">First</a>|
	<a
		href="<s:property value="#request['struts.request_uri']+resultPage.renderUrl(resultPage.previousPage)"/>"
		class="ajax view">Previous</a>
</s:if><s:else>First|
				Previous
				</s:else>| <s:if test="%{!resultPage.isLast()}">
	<a
		href="<s:property value="#request['struts.request_uri']+resultPage.renderUrl(resultPage.nextPage)"/>"
		class="ajax view">Next</a>|
	<a
		href="<s:property value="#request['struts.request_uri']+resultPage.renderUrl(resultPage.totalPage)"/>"
		class="ajax view">Last</a>
</s:if> <s:else>
				Next|Last
				</s:else> |<s:property value="resultPage.pageNo" /> / <s:property
	value="resultPage.totalPage" /></div>
</body>
</html>

