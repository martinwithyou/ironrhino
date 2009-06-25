<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<s:form action="signup" method="post" cssClass="ajax">
	<s:textfield label="%{getText('email')}" name="account.email"
		size="24" cssClass="required email" />
	<s:textfield label="%{getText('username')}"
		name="account.username" size="24" />
	<s:password label="%{getText('password')}" name="password" size="24" />
	<s:password label="%{getText('confirmPassword')}"
		name="confirmPassword" size="24" />
	<s:submit value="注册" />
</s:form>

<s:if test="%{#session.openid!=null}">
	<s:form action="openid" method="post" cssClass="ajax">
		<s:textfield label="username or email" name="username" size="24"
			cssClass="required" />
		<s:password label="%{getText('password')}" name="password" size="24"
			cssClass="required" />
		<s:submit value="绑定" />
	</s:form>
</s:if>
<s:else>
	<div><a href="<s:url value="/account/login?tab=openid"/>">使用openid?</a></div>
</s:else>
</body>
</html>
