<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>List Categories</title>
</head>
<body>
<#assign config={"code":{},"name":{"cellEdit":"input"},"description":{"cellEdit":"input"},"displayOrder":{"cellEdit":"input"},"rolesAsString":{"trimPrefix":true,"cellEdit":"input","class":"include_if_edited"}}>
<#assign actionColumnButtons=btn(action.getText('enter'),"Richtable.enter('#id')")+btn(action.getText('save'),"Richtable.save('#id')")+btn(action.getText('delete'),"Richtable.del('#id')")+btn(action.getText('move'),"Richtable.open(Richtable.getUrl('tree','#id'),true,true)")+btn(action.getText('product'),"Richtable.enter('#id','product?categoryId={parentId}')")>
<@richtable entityName="category" config=config actionColumnWidth="220px" actionColumnButtons=actionColumnButtons/>
</body>
</html></#escape>
