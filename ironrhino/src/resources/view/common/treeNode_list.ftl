<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title><#if treeNode.name??>${treeNode.name}-</#if>${action.getText('treeNode')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"name":{"cellEdit":"click"},"description":{"cellEdit":"click"},"displayOrder":{"cellEdit":"click"}}>
<#assign actionColumnButtons='
<button type="button" class="btn" data-view="input">${action.getText("edit")}</button>
<button type="button" class="btn" data-action="enter">${action.getText("enter")}</button>
'>
<#assign bottomButtons=r'
<button type="button" class="btn" data-view="input">${action.getText("create")}</button>
<button type="button" class="btn" data-action="save">${action.getText("save")}</button>
<button type="button" class="btn" data-action="delete" data-shown="selected">${action.getText("delete")}</button>
<button type="button" class="btn" data-action="reload">${action.getText("reload")}</button>
<#if treeNode?? && parentId??>
<#if treeNode.parent??>
<a class="btn" href="${getUrl(actionBaseUrl+"?parentId="+treeNode.parent.id)}">${action.getText("upward")}</a>
<#else>
<a class="btn" href="${getUrl(actionBaseUrl)}">${action.getText("upward")}</a>
</#if>
</#if>
'>
<@richtable entityName="treeNode" columns=columns actionColumnButtons=actionColumnButtons  bottomButtons=bottomButtons/>
</body>
</html></#escape>
