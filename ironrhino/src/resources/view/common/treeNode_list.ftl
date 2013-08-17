<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title><#if treeNode.name??>${treeNode.name}-</#if>${action.getText('treeNode')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"name":{"cellEdit":"click"},"description":{"cellEdit":"click"},"displayOrder":{"cellEdit":"click"}}>
<#assign actionColumnButtons=r'
<button type="button" class="btn" data-view="input">${action.getText("edit")}</button>
<a class="btn ajax view" href="${actionBaseUrl+"?parentId="+entity.id}">${action.getText("enter")}</a>
'>
<#assign bottomButtons=r'
<button type="button" class="btn" data-view="input">${action.getText("create")}</button>
<button type="button" class="btn confirm" data-action="save">${action.getText("save")}</button>
<button type="button" class="btn" data-action="delete" data-shown="selected">${action.getText("delete")}</button>
<button type="button" class="btn reload">${action.getText("reload")}</button>
<#if treeNode?? && parentId??>
<#if treeNode.parent??>
<a class="btn ajax view" href="${actionBaseUrl+"?parentId="+treeNode.parent.id}">${action.getText("upward")}</a>
<#else>
<a class="btn ajax view" href="${actionBaseUrl}">${action.getText("upward")}</a>
</#if>
</#if>
'>
<#if treeNode?? && treeNode.id?? && treeNode.id gt 0>
<ul class="breadcrumb">
	<li>
    	<a href="${actionBaseUrl}" class="ajax view">${action.getText('treeNode')}</a> <span class="divider">/</span>
	</li>
	<#if treeNode.level gt 1>
	<#list 1..treeNode.level-1 as level>
	<#assign ancestor=treeNode.getAncestor(level)>
	<li>
    	<a href="${actionBaseUrl}?parentId=${ancestor.id?string}" class="ajax view">${ancestor.name}</a> <span class="divider">/</span>
	</li>
	</#list>
	</#if>
	<li class="active">${treeNode.name}</li>
</ul>
</#if>
<@richtable entityName="treeNode" columns=columns actionColumnButtons=actionColumnButtons  bottomButtons=bottomButtons/>
</body>
</html></#escape>
