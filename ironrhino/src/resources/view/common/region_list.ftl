<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>List Regions</title>
</head>
<body>
<#assign config={"name":{"cellEdit":"click"},"displayOrder":{"cellEdit":"click"}}>
<#assign actionColumnButtons=btn(action.getText('enter'),r"Richtable.enter('${entity.id}')")+btn(action.getText('save'),null,'save')+btn(action.getText('delete'),null,'del')>
<@richtable entityName="region" config=config actionColumnButtons=actionColumnButtons/>
</body>
</html></#escape>
