<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>List Products</title>
</head>
<body>
<#assign config={"code":{},"name":{},"tagsAsString":{"trimPrefix":true,"cellEdit":"input","class":"include_if_edited"}}>
<#assign actionColumnButtons=btn(action.getText('save'),r"Richtable.save('${rowid}')")+btn(action.getText('edit'),r"Richtable.input('${rowid}')")+btn(action.getText('picture'),r"Richtable.open(Richtable.getUrl('picture','${rowid}'))")+btn(action.getText('attribute'),r"Richtable.open(Richtable.getUrl('attribute','${rowid}'))")+btn(action.getText('category'),r"Richtable.open(Richtable.getUrl('category','${rowid}'),true,true)")+btn(action.getText('delete'),r"Richtable.del('${rowid}')")>
<@richtable entityName="product" config=config actionColumnWidth="250px" actionColumnButtons=actionColumnButtons/>
</body>
</html></#escape>
