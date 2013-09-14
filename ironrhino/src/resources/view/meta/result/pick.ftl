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
	<#assign propertyNames=Parameters.columns?split(',')>
	<#assign columnNames=[]>
	<#if uiConfigs??>
	<#list uiConfigs.entrySet() as entry>
		<#assign column=entry.key>
		<#if propertyNames?seq_contains(column)>
		<#assign config=entry.value>
		<#assign shown=!config.hiddenInList.value>
		<#if shown && config.hiddenInList.expression?has_content>
		<#assign shown=!config.hiddenInList.expression?eval/>
		</#if>
		<#if shown>
			<#assign columnNames=columnNames+[column]>
		</#if>
		</#if>
	</#list>
	<#else>
	<#assign columnNames=propertyNames>
	</#if>
<#elseif uiConfigs??>
	<#assign propertyNames=uiConfigs?keys>
	<#assign columnNames=[]>
	<#list uiConfigs.entrySet() as entry>
		<#assign column=entry.key>
		<#if (column=='name' && !propertyNames?seq_contains('fullname') || column=='fullname' || column=='code' || entry.value.shownInPick || naturalIds?? && naturalIds?keys?seq_contains(column))>
			<#assign columnNames=columnNames+[column]>
		</#if>
	</#list>
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
		<#if treeable && column == 'name'||column == 'fullname'>
			<#assign columns=columns+{column:{'template':r'<#if entity.leaf??&&!entity.leaf><a href="${href}${href?contains("?")?string("&","?")+"parentId="+entity.id}" class="ajax view" data-replacement="${entityName}_pick_form">${value}</a><#else>${value}</#if>'}}/>
		<#else>
			<#assign columns=columns+{column:{}}/>
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
<#else>
<#if filterable><button type="button" class="btn filter">${action.getText("filter")}</button></#if>
</#if>
'>
<#else>
<#assign bottomButtons=r'
<button type="button" class="btn pick" data-shown="selected">${action.getText("confirm")}</button>
<#if filterable><button type="button" class="btn filter">${action.getText("filter")}</button></#if>
<#if treeable&&Parameters.parentId??>
<a href="${href}<#if _parentId?? && _parentId gt 0>${href?contains("?")?string("&","?")+"parentId="+_parentId}</#if>" class="btn ajax view" data-replacement="${entityName}_pick_form">${action.getText("upward")}</a>
</#if>
'>
</#if>
<@richtable entityName=entityName formid=entityName+"_pick_form" action=requestURI columns=columns bottomButtons=bottomButtons searchable=true readonly=true showCheckColumn=true multipleCheck=multiple columnfilterable=false resizable=false sortable=false showPageSize=false/>
</body>
</html></#escape>