<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('profile')}</title>
</head>
<body>
<@s.form action="profile" method="post" cssClass="form-horizontal ajax">
	<@s.hidden name="user.username"/>
	<@s.textfield label="%{getText('name')}" name="user.name" cssClass="required"/>
	<@s.textfield label="%{getText('email')}" name="user.email" cssClass="email checkavailable"/>
	<@s.textfield label="%{getText('phone')}" name="user.phone"/>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>


