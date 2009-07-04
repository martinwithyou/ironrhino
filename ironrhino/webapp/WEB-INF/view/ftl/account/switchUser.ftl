<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Switch User</title>
</head>
<body>
<h1>Switch to User</h1>
<form action="switch" method="POST">
<table>
	<tr>
		<td>User:</td>
		<td><input type='text' name='j_username'></td>
	</tr>
	<tr>
		<td colspan='2'><input name="switch" type="submit"
			value="Switch to User" class="button"></td>
	</tr>
</table>
</form>
</body>
</html>
