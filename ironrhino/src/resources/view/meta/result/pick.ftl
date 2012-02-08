<#if !entityName??>
<#assign entityName=action.class.simpleName?uncap_first/>
<#if entityName?ends_with('Action')>
<#assign entityName=entityName?substring(0,entityName?length-6)/>
</#if>
</#if>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('pick')}${action.getText(entityName)}</title>
</head>
<body>
<#assign multiple=false>
<#if Parameters.multiple?? && Parameters.multiple=='true'>
<#assign multiple=true>
</#if>
<#assign columns={}>
<#if Parameters.columns??>
<#list Parameters.columns?split(',') as column>
<#assign columns=columns+{column:{}}/>
</#list>
<#elseif uiConfigs??>
<#list uiConfigs?keys as column>
<#assign columns=columns+{column:{}}/>
</#list>
</#if>
<#if !multiple>
<#assign bottomButtons="<span></span>">
<#else>
<#assign bottomButtons=r"
<@button text='${action.getText(\'confirm\')}' class='confirm'/>
">
</#if>
<@richtable entityName=entityName formid=entityName+"_pick_form" action=request.requestURI?substring(request.contextPath?length) columns=columns bottomButtons=bottomButtons searchable=true readonly=true showCheckColumn=true multipleCheck=multiple columnfilterable=false resizable=false sortable=false showPageSize=false/>
</body>
</html></#escape>
