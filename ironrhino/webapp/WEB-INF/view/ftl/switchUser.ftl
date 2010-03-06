<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('switchUser')}</title>
</head>
<body>
<h1>${action.getText('switchUser')}</h1>
<form action="<@url value="switch"/>" method="POST">
<div>
<input type='text' name='j_username'>
<@button type="submit" text="${action.getText('switchUser')}"/>
</div>
</form>
</body>
</html></#escape>
