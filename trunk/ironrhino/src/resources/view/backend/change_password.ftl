<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<@s.form action="change_password" method="post" cssClass="ajax">
	<@s.password label="%{getText('currentPassword')}"
		name="currentPassword" cssClass="required" />
	<@s.password label="%{getText('password')}" name="password"
		cssClass="required" />
	<@s.password label="%{getText('confirmPassword')}"
		name="confirmPassword" cssClass="required" />
	<@s.submit value="Save" />
</@s.form>
</body>
</html>


