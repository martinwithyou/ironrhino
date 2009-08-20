<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>List Products</title>
</head>
<body>
<#assign config={"code":{},"name":{},"tagsAsString":{"trimPrefix":true,"cellEdit":"input","class":"include_if_edited"}}>
<#assign actionColumnButtons=btn(action.getText('save'),"Richtable.save('#id')")+btn(action.getText('edit'),"Richtable.input('#id')")+btn(action.getText('picture'),"Richtable.open(Richtable.getUrl('picture','#id'))")+btn(action.getText('attribute'),"Richtable.open(Richtable.getUrl('attribute','#id'))")+btn(action.getText('category'),"Richtable.open(Richtable.getUrl('category','#id'),true,true)")+btn(action.getText('delete'),"Richtable.del('#id')")>
<@richtable entityName="product" config=config actionColumnWidth="250px" actionColumnButtons=actionColumnButtons/>
</body>
</html></#escape>
