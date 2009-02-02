<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
<link href="<c:url value="/styles/jquery.treeview.css"/>" media="screen"
	rel="stylesheet" type="text/css" />
<script type="text/javascript"
	src="<c:url value="/scripts/jquery.treeview.js"/>"></script>
<script type="text/javascript"
	src="<c:url value="/scripts/jquery.treeview.async.my.js"/>"></script>
</head>
<body>
<s:form id="addressee" namespace="/account" action="order!addressee"
	method="post">
	<s:textfield label="%{getText('order.addressee.name')}"
		name="order.addressee.name" cssClass="required" />
	<s:textfield label="%{getText('order.addressee.address')}"
		name="order.addressee.address" id="order.addressee.address"
		cssClass="required">
		<s:param name="after">
			<span class="link" onclick="Region.select('order.addressee.address')">select</span>
		</s:param>
	</s:textfield>
	<s:textfield label="%{getText('order.addressee.zip')}"
		name="order.addressee.zip" cssClass="required" />
	<s:textfield label="%{getText('order.addressee.telephone')}"
		name="order.addressee.telephone" cssClass="required" />
	<s:textarea label="%{getText('order.description')}"
		name="order.description" />
	<s:submit value="Save" />
</s:form>
</body>
</html>


