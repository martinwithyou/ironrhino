<#include "../../richtable-macro.ftl"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>List Products</title>
</head>
<body>
<#assign config={"code":{},"name":{},"tagsAsString":{"trimPrefix":true,"cellEdit":"input","class":"include_if_edited"},"relatedProductsAsString":{"trimPrefix":true,"cellEdit":"input","class":"include_if_edited"},"rolesAsString":{"trimPrefix":true,"cellEdit":"input","class":"include_if_edited"}}>
<@richtable entityName="product" config=config actionColumnWidth="380px" actionColumnButtons='<button type="button" onclick="Richtable.save(\'#id\')">保存</button><button type="button" onclick="Richtable.input(\'#id\')">编辑</button><button type="button" onclick="Richtable.open(Richtable.getUrl(\'picture\',\'#id\'))">图片</button><button type="button" onclick="Richtable.open(Richtable.getUrl(\'attribute\',\'#id\'))">属性</button><button type="button" onclick="Richtable.open(Richtable.getUrl(\'category\',\'#id\'),true)">目录</button><button type="button" onclick="Richtable.del(\'#id\')">删除</button>'/>
</body>
</html>
