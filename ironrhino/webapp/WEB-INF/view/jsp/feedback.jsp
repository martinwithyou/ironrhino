<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="authz"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<s:form id="feedback" action="feedback" method="post" cssClass="ajax">
	<s:textfield label="%{getText('feedback.name')}" name="feedback.name"
		cssClass="required" />
	<s:textfield label="%{getText('feedback.telephone')}"
		name="feedback.telephone" />
	<s:textfield label="%{getText('feedback.email')}" name="feedback.email"
		cssClass="email" />
	<s:textfield label="%{getText('feedback.subject')}"
		name="feedback.subject" size="50" cssClass="required" />
	<s:textarea label="%{getText('feedback.content')}" cols="50" rows="4"
		name="feedback.content" />
	<authz:authorize ifNotGranted="ROLE_BUILTIN_ACCOUNT">
		<s:textfield label="%{getText('captcha')}" name="captcha" size="6"
			cssClass="autocomplete_off required captcha"/>
	</authz:authorize>
	<s:submit value="%{getText('save')}" />
</s:form>
</body>
</html>
