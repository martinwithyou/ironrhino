<#macro selectDictionary dictionary={} dictionaryName="" id="" name="" value="" required=false strict=true>
	<#if dictionaryName!="" && !dictionary.name??>
		<#local dictionary=statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('dictionaryControl').getDictionary(dictionaryName)!>
	</#if>
	<select<#if id!=""> id="${id}"</#if> name="${name}" class="<#if required> required</#if><#if !strict> combox</#if>">
		<option value="">${action.getText('select')}</option>
		<#if dictionary?? && dictionary.items??>
		<#list dictionary.items as lv>
		<option value="${lv.value}"<#if value=lv.value> selected="selected"</#if>>${lv.label}</option>
		</#list>
		</#if>
	</select>
</#macro>

<#macro displayDictionary dictionary={} dictionaryName="" value="">
	<#if dictionaryName!="" && !dictionary.name??>
		<#local dictionary=statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('dictionaryControl').getDictionary(dictionaryName)!>
	</#if>
	<#if dictionary?? && dictionary.items??>
		<#list dictionary.items as lv>
		<#if value==lv.value>${lv.label}<#break></#if>
		</#list>
	</#if>
</#macro>