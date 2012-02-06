<#macro selectDictionary dictionary={} dictionaryName="" value="" parameterName="" required=false strict=true>
	<#if dictionaryName!="" && !dictionary.name??>
		<#local dictionary=statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('dictionaryControl').getDictionary(dictionaryName)!>
	</#if>
	<select name="${parameterName}" class="<#if required> required</#if><#if !strict> combox</#if>">
		<option value="">${action.getText('select')}</option>
		<#if dictionary?? && dictionary.items??>
		<#list dictionary.items as lv>
		<option value="${lv.value}"<#if value=lv.value> selected="selected"</#if>>${lv.label}</option>
		</#list>
		</#if>
	</select>
</#macro>