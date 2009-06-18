<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="authz"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<div class="tabs">
<ul tab="#${param['tab']}">
	<li><a href="#profile"><span>基本信息</span></a></li>
	<li><a href="#password"><span>修改密码</span></a></li>
	<li><a href="#email"><span>修改email</span></a></li>
	<authz:authentication property="principal.openid" var="openid"
		scope="request" />
	<c:if test="${not empty openid}">
		<li><a href="#unbindopenid"><span>取消openid</span></a></li>
	</c:if>
	<li><a href="#invite"><span>邀请朋友</span></a></li>
</ul>
<div id="profile"><s:form id="profile_form" action="profile"
	method="post" cssClass="ajax">
	<s:textfield label="%{getText('account.nickname')}"
		name="account.nickname" cssClass="required" />
	<s:textfield label="%{getText('account.name')}" name="account.name" />
	<s:select label="%{getText('account.sex')}" name="account.sex"
		list="@org.ironrhino.online.model.Sex@values()" listKey="name"
		listValue="displayName" />
	<s:textfield label="%{getText('account.birthDate')}"
		name="account.birthDate" cssClass="date" />
	<s:textfield id="address" label="%{getText('account.address')}"
		name="account.address">
		<s:param name="after">
			<span class="link"
				onclick="Region.select('address',true)">select</span>
		</s:param>
	</s:textfield>
	<s:textfield label="%{getText('account.zip')}" name="account.zip" />
	<s:textfield label="%{getText('account.telephone')}"
		name="account.telephone" />
	<s:checkbox label="%{getText('account.subscribed')}"
		name="account.subscribed" />
	<s:submit value="Save" />
</s:form></div>
<div id="password"><s:form id="password_form" action="password"
	method="post" cssClass="ajax">
	<s:password label="%{getText('currentPassword')}"
		name="currentPassword" cssClass="required" />
	<s:password label="%{getText('password')}" name="password"
		cssClass="required" />
	<s:password label="%{getText('confirmPassword')}"
		name="confirmPassword" cssClass="required" />
	<s:submit value="Save" />
</s:form></div>
<div id="email"><s:form id="email_form" action="email"
	method="post" cssClass="ajax">
	<s:password label="%{getText('currentPassword')}"
		name="currentPassword" cssClass="required" />
	<s:textfield label="%{getText('account.email')}" name="account.email"
		cssClass="required email" />
	<s:submit value="Save" />
</s:form></div>
<c:if test="${not empty openid}">
	<div id="unbindopenid"><s:form id="unbindopenid_form"
		action="unbindopenid" method="post" cssClass="ajax">
		<s:password label="%{getText('currentPassword')}"
			name="currentPassword" cssClass="required" />
		<s:submit value="Save" />
	</s:form></div>
</c:if>
<div id="invite"><s:form id="invite_form" action="invite"
	method="post" cssClass="ajax">
	<s:textfield label="%{getText('account.email')}" name="account.email"
		value="" cssClass="required email" />
	<s:submit value="Save" />
</s:form></div>
</div>
</body>
</html>

