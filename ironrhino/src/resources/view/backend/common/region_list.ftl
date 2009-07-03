<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>List Regions</title>
</head>
<body>
<#assign config={"name":{"cellEdit":"input"},"displayOrder":{"cellEdit":"input"},"rolesAsString":{"trimPrefix":true,"cellEdit":"input","class":"include_if_edited"}}>
<#assign actionColumnButtons=btn("Richtable.enter('#id')",action.getText('enter'))+btn("Richtable.save('#id')",action.getText('save'))+btn("Richtable.del('#id')",action.getText('delete'))>
<@richtable entityName="region" config=config actionColumnButtons=actionColumnButtons/>
</body>
</html>
