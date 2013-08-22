<#if !entityName??>
<#assign entityName=action.class.simpleName?uncap_first/>
<#if entityName?ends_with('Action')>
<#assign entityName=entityName?substring(0,entityName?length-6)/>
</#if>
</#if>
<#assign requestURI=request.requestURI>
<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('pick')}${action.getText(entityName)}</title>
</head>
<body>

<#assign multiple=false>
<#if Parameters.multiple?? && Parameters.multiple=='true'>
	<#assign multiple=true>
</#if>
<#if Parameters.columns??>
	<#assign columnNames=Parameters.columns?split(',')>
<#elseif uiConfigs??>
	<#assign columnNames=uiConfigs?keys>
</#if>
<#assign treeable = action.getParentId??>
<#if treeable>
	<#assign href=requestURI>
	<#assign index=0>
	<#list Parameters?keys as name>
	<#if name!='_'&&name!='parentId'&&name!='keyword'>
	<#assign href=href+(index==0)?string('?','&')+name+'='+Parameters[name]>
	<#assign index=index+1>
	</#if>
	</#list>
</#if>
<#assign columns={}>
<#if columnNames??>
	<#list columnNames as column>
		<#assign hidden=false>
		<#if uiConfigs??>
		<#assign hidden=uiConfigs[column].hiddenInList.value>
		<#if !hidden && uiConfigs[column].hiddenInList.expression?has_content>
		<#assign hidden=uiConfigs[column].hiddenInList.expression?eval/>
		</#if>
		</#if>
		<#if !hidden>
			<#if treeable && column == 'name'||column == 'fullname'>
				<#assign columns=columns+{column:{'template':r'<#if entity.leaf??&&!entity.leaf><a href="${href}${href?contains("?")?string("&","?")+"parentId="+entity.id}" class="ajax view" data-replacement="${entityName}_pick_form">${value}</a><#else>${value}</#if>'}}/>
			<#else>
				<#assign columns=columns+{column:{}}/>
			</#if>
		</#if>
	</#list>
</#if>
<#if treeable>
<#assign _entity=entityName?eval!>
<#if _entity?? && _entity.parent??><#assign _parentId=_entity.parent.id><#else><#assign _parentId=0></#if>
</#if>
<#if !multiple>
<#assign bottomButtons=r'
<#if treeable&&Parameters.parentId??>
<a href="${href}<#if _parentId?? && _parentId gt 0>${href?contains("?")?string("&","?")+"parentId="+_parentId}</#if>" class="btn ajax view" data-replacement="${entityName}_pick_form">${action.getText("upward")}</a>
</#if>
'>
<#else>
<#assign bottomButtons=r'
<button type="button" class="btn confirm">${action.getText("confirm")}</button>
<#if treeable&&Parameters.parentId??>
<a href="${href}<#if _parentId?? && _parentId gt 0>${href?contains("?")?string("&","?")+"parentId="+_parentId}</#if>" class="btn ajax view" data-replacement="${entityName}_pick_form">${action.getText("upward")}</a>
</#if>
'>
</#if>
<@richtable entityName=entityName formid=entityName+"_pick_form" action=requestURI columns=columns bottomButtons=bottomButtons searchable=true readonly=true showCheckColumn=true multipleCheck=multiple columnfilterable=false resizable=false sortable=false showPageSize=false filterable=false/>
</body>
</html></#escape>
