<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Console</title>
</head>
<body>
<@s.form id="form" action="console" method="post" cssClass="ajax focus">
	<@s.textfield theme="simple" id="cmd" name="cmd" size="50"/>
	<@s.submit id="submit" theme="simple" value="%{getText('confirm')}" />
</@s.form>
<div id="dashboard" style="margin:10px;">
	<@button text="compassGps.index()" onclick="$('#cmd').val($(this).text());$('#form').submit()"/>
</div>
</body>
</html></#escape>