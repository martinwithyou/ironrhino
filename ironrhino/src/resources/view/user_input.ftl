<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title><#if user.new>${action.getText('create')}<#else>${action.getText('edit')}</#if>${action.getText('user')}</title>
</head>
<body>
<@s.form action="save2" method="post" cssClass="ajax">
	<#if !user.new>
		<@s.hidden name="user.id" />
		<@s.textfield label="%{getText('username')}" name="user.username"
			required="true" readonly="true"/>
	<#else>
		<@s.textfield label="%{getText('username')}" name="user.username"
			required="true" cssClass="required"/>
		<@s.password label="%{getText('password')}" name="password"
			required="true" cssClass="required"/>
		<@s.password label="%{getText('confirmPassword')}"
			name="confirmPassword" required="true" cssClass="required"/>
	</#if>
	<@s.textfield label="%{getText('name')}" name="user.name" required="true" cssClass="required"/>
	<@s.checkbox label="%{getText('enabled')}" name="user.enabled" />
	<@s.checkboxlist label="%{getText('role')}" name="roleId" list="roles" listKey="key" listValue="value"/>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>


