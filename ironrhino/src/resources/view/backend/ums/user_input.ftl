<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Create/Edit User</title>
<body>
<@s.form action="save2" method="post" cssClass="ajax">
	<@s.if test="%{!user.isNew()}">
		<@s.hidden name="user.id" />
		<@s.textfield label="%{getText('username')}" name="user.username"
			required="true" readonly="true" />
	</@s.if>
	<@s.else>
		<@s.textfield label="%{getText('username')}" name="user.username"
			required="true" />
		<@s.password label="%{getText('password')}" name="password"
			required="true" />
		<@s.password label="%{getText('confirmPassword')}"
			name="confirmPassword" required="true" />
	</@s.else>
	<@s.textfield label="%{getText('name')}" name="user.name"
		required="true" />
	<@s.textfield label="%{getText('email')}" name="user.email"
		required="true" />
	<@s.textarea label="%{getText('description')}"
		name="user.description" />
	<@s.checkbox label="%{getText('enabled')}" name="user.enabled" />
	<@s.checkbox label="%{getText('locked')}" name="user.locked" />
	<@s.textfield label="%{getText('accountExpireDate')}"
		name="user.accountExpireDate" cssClass="date" />
	<@s.textfield label="%{getText('passwordExpireDate')}"
		name="user.passwordExpireDate" cssClass="date" />
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>


