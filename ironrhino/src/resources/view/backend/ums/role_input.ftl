<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Create/Edit Role</title>

</head>
<body>
<@s.form id="role_save" action="save" method="post" cssClass="ajax">
	<@s.if test="%{!role.isNew()}">
		<@s.hidden name="role.id" />
		<@s.textfield label="%{getText('name')}" name="role.name"
			readonly="true" />
	</@s.if>
	<@s.else>
		<@s.textfield label="%{getText('name')}" name="role.name" />
	</@s.else>
	<@s.textfield label="%{getText('description')}"
		name="role.description" />
	<@s.checkbox label="%{getText('enabled')}" name="role.enabled" />
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html>


