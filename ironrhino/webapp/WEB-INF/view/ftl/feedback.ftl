<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<@s.form id="feedback" action="feedback" method="post" cssClass="ajax reset">
	<@s.textfield label="%{getText('name')}" name="feedback.name"
		cssClass="required" />
	<@s.textfield label="%{getText('phone')}"
		name="feedback.phone" />
	<@s.textfield label="%{getText('email')}" name="feedback.email"
		cssClass="email" />
	<@s.textfield label="%{getText('title')}"
		name="feedback.title" size="50" cssClass="required" />
	<@s.textarea label="%{getText('content')}" cols="50" rows="5"
		name="feedback.content" />
	<@captcha/>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>
