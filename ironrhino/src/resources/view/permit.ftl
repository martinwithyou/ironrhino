<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('permit')}</title>
</head>
<body>
<div style="margin-bottom:20px;" class="switch">
<#list roles?keys as role>
<@button type="link" href="permit/input?role=${role}" class="ajax view" replacement="save" style="margin:0 5px;" text="${roles[role]}"/>
</#list>
<form action="${getUrl(actionBaseUrl+'/input')}" method="get" class="line ajax view" replacement="save" style="margin-right:5px;float:right;"><span>${action.getText('username')}:</span><input type="text" name="username"/><@button type="submit" text="${action.getText('confirm')}"/></form>
</div>
<div id="save">
</div>
</body>
</html></#escape>