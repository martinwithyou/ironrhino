<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('apply')}${action.getText('client')}</title>
</head>
<body>
	<p>client_id:<span style="margin-left:5px;font-weight:bold;">${client.id}</span></p>
	<p>client_secret:<span style="margin-left:5px;font-weight:bold;">${client.secret}</span></p>
	<p>${action.getText('description')}:<span style="margin-left:5px;font-weight:bold;">${client.description}</span></p>
</body>
</html></#escape>