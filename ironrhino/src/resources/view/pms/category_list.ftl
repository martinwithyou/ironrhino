<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>List Categories</title>
</head>
<body>
<#assign config={"code":{},"name":{"cellEdit":"click"},"description":{"cellEdit":"click"},"displayOrder":{"cellEdit":"click"},"rolesAsString":{"trimPrefix":true,"cellEdit":"click","class":"include_if_edited"}}>
<#assign actionColumnButtons=r"
<@button text='${action.getText(\'enter\')}' onclick='Richtable.enter(\'${entity.id}\')'/>
<@button text='${action.getText(\'save\')}' action='save'/>
<@button text='${action.getText(\'delete\')}' action='delete'/>
<@button text='${action.getText(\'move\')}' onclick='Richtable.open(Richtable.getUrl(\'tree\',\'${entity.id}\'),true,true)'/>
<@button text='${action.getText(\'product\')}' type='link' href='product?categoryId=${entity.id}'/>
">
<@richtable entityName="category" config=config actionColumnWidth="220px" actionColumnButtons=actionColumnButtons/>
</body>
</html></#escape>
