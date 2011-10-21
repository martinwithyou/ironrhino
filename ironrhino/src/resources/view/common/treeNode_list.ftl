<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('treeNode')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"name":{"cellEdit":"click"},"description":{"cellEdit":"click"},"displayOrder":{"cellEdit":"click"}}>
<#assign actionColumnButtons=r"
<@button text='${action.getText(\'edit\')}' view='input'/>
<@button text='${action.getText(\'enter\')}' action='enter'/>
">
<#assign bottomButtons=r"
<@button text='${action.getText(\'create\')}' view='input'/>
<@button text='${action.getText(\'save\')}' action='save'/>
<@button text='${action.getText(\'delete\')}' action='delete'/>
<@button text='${action.getText(\'reload\')}' action='reload'/>
<#if treeNode?? && parentId??>
<#if treeNode.parent??>
<@button text='${action.getText(\'upward\')}' type='link' href='${getUrl(\'/common/treeNode?parentId=\'+treeNode.parent.id)}'/>
<#else>
<@button text='${action.getText(\'upward\')}' type='link' href='${getUrl(\'/common/treeNode\')}'/>
</#if>
</#if>
">
<@richtable entityName="treeNode" columns=columns actionColumnButtons=actionColumnButtons  actionColumnWidth="100px" bottomButtons=bottomButtons/>
</body>
</html></#escape>
