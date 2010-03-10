<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>List Products</title>
</head>
<body>
<#assign config={"code":{},"name":{},"tagsAsString":{"trimPrefix":true,"cellEdit":"click","class":"include_if_edited"}}>
<#assign actionColumnButtons=r"
<@button text='${action.getText(\'edit\')}' view='input'/>
<@button text='${action.getText(\'save\')}' action='save'/>
<@button text='${action.getText(\'picture\')}' view='picture'/>
<@button text='${action.getText(\'attribute\')}' view='attribute'/>
<@button text='${action.getText(\'delete\')}' action='delete'/>
<@button text='${action.getText(\'category\')}' onclick='Richtable.open(Richtable.getUrl(\'category\',\'${entity.id}\'),true,true)'/>
">
<@richtable entityName="product" config=config actionColumnWidth="250px" actionColumnButtons=actionColumnButtons/>
</body>
</html></#escape>
