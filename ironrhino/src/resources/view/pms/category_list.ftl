<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>List Categories</title>
</head>
<body>
<#assign config={"code":{},"name":{"cellEdit":"input"},"description":{"cellEdit":"input"},"displayOrder":{"cellEdit":"input"},"rolesAsString":{"trimPrefix":true,"cellEdit":"input","class":"include_if_edited"}}>
<#assign actionColumnButtons=btn(action.getText('enter'),r"Richtable.enter('${rowid}')")+btn(action.getText('save'),r"Richtable.save('${rowid}')")+btn(action.getText('delete'),r"Richtable.del('${rowid}')")+btn(action.getText('move'),r"Richtable.open(Richtable.getUrl('tree','${rowid}'),true,true)")+btn(action.getText('product'),r"Richtable.enter('${rowid}','product?categoryId={parentId}')")>
<@richtable entityName="category" config=config actionColumnWidth="220px" actionColumnButtons=actionColumnButtons/>
</body>
</html></#escape>
