<#include "../../richtable-macro.ftl"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>List Products</title>
</head>
<body>
<#assign config={"code":{},"name":{},"tagsAsString":{"trimPrefix":true,"cellEdit":"input","class":"include_if_edited"},"relatedProductsAsString":{"trimPrefix":true,"cellEdit":"input","class":"include_if_edited"},"rolesAsString":{"trimPrefix":true,"cellEdit":"input","class":"include_if_edited"}}>
<#assign actionColumnButtons=btn("Richtable.save('#id')",action.getText('save'))+btn("Richtable.input('#id')",action.getText('edit'))+btn("Richtable.open(Richtable.getUrl('picture','#id'))",action.getText('picture'))+btn("Richtable.open(Richtable.getUrl('attribute','#id'))",action.getText('attribute'))+btn("Richtable.open(Richtable.getUrl('category','#id'),true,true)",action.getText('category'))+btn("Richtable.del('#id')",action.getText('delete'))>
<@richtable entityName="product" config=config actionColumnWidth="250px" actionColumnButtons=actionColumnButtons/>
</body>
</html>
