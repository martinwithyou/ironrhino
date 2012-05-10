<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('treeNode')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"name":{"cellEdit":"click"},"description":{"cellEdit":"click"},"displayOrder":{"cellEdit":"click"}}>
<#assign actionColumnButtons='
<button type="button" class="btn" data-view="input">${action.getText("edit")}</button><#t>
<button type="button" class="btn" data-action="enter">${action.getText("enter")}</button><#t>
'>
<#assign bottomButtons=r'
<button type="button" class="btn" data-view="input">${action.getText("create")}</button><#t>
<button type="button" class="btn" data-action="save">${action.getText("save")}</button><#t>
<button type="button" class="btn" data-action="delete">${action.getText("delete")}</button><#t>
<button type="button" class="btn" data-action="reload">${action.getText("reload")}</button><#t>
<#if treeNode?? && parentId??>
<#if treeNode.parent??>
<a class="btn" href="${getUrl("/common/treeNode?parentId="+treeNode.parent.id)}">${action.getText("upward")}</a><#t>
<#else>
<a class="btn" href="${getUrl("/common/treeNode")}"/>${action.getText("upward")}</a><#t>
</#if>
</#if>
'>
<@richtable entityName="treeNode" columns=columns actionColumnButtons=actionColumnButtons  actionColumnWidth="100px" bottomButtons=bottomButtons/>
</body>
</html></#escape>
