<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('setting')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"key":{"cellEdit":"click"},"value":{"cellEdit":"click,textarea"}}>
<@richtable entityName="setting" columns=columns/>
</body>
</html></#escape>
