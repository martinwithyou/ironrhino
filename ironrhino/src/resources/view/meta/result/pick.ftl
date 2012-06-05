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
<#assign columns={}>
<#if columnNames??>
	<#list columnNames as column>
		<#if !uiConfigs?? || !uiConfigs[column].hiddenInList>
			<#if treeable && column == 'name'||column == 'fullname'>
				<#assign href=requestURI>
				<#assign index=0>
				<#list Parameters?keys as name>
				<#if name!='_'&&name!='parentId'&&name!='keyword'>
				<#assign href=href+(index==0)?string('?','&')+name+'='+Parameters[name]>
				<#assign index=index+1>
				</#if>
				</#list>
				<#assign columns=columns+{column:{'template':r'<#if !entity.leaf><a href="${href}${href?contains("?")?string("&","?")+"parentId="+entity.id}" class="ajax view" replacement="${entityName}_pick_form">${value}</a><#else>${value}</#if>'}}/>
			<#else>
				<#assign columns=columns+{column:{}}/>
			</#if>
		</#if>
	</#list>
</#if>
<#if !multiple>
<#assign bottomButtons="<span></span>">
<#else>
<#assign bottomButtons=r'
<button type="button" class="btn confirm">${action.getText("confirm")}</button>
'>
</#if>
<@richtable entityName=entityName formid=entityName+"_pick_form" action=requestURI columns=columns bottomButtons=bottomButtons searchable=true readonly=true showCheckColumn=true multipleCheck=multiple columnfilterable=false resizable=false sortable=false showPageSize=false/>
</body>
</html></#escape>
