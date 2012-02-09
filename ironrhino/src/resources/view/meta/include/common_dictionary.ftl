<#macro selectDictionary dictionaryName id="" name="" value="" required=false  headerKey="" headerValue="" strict=true extra...>
	<#local dictionary=statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('dictionaryControl').getDictionary(dictionaryName)!>
	<select<#if id!=""> id="${id}"</#if> name="${name}" class="<#if required> required</#if><#if !strict> combox</#if>"<#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list>>
		<option value="${headerKey}">${headerValue}</option>
		<#if dictionary?? && dictionary.items??>
		<#list dictionary.items as lv>
		<option value="${lv.value}"<#if value=lv.value> selected="selected"</#if>>${lv.label}</option>
		</#list>
		</#if>
	</select>
</#macro>

<#macro displayDictionaryLabel dictionaryName value="">
	<#local dictionary=statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('dictionaryControl').getDictionary(dictionaryName)!>
	<#if dictionary?? && dictionary.items??>
		<#list dictionary.items as lv>
		<#if value==lv.value>${lv.label}<#break></#if>
		</#list>
	</#if>
</#macro>

<#function getDictionaryLabel dictionaryName value="">
	<#local dictionary=statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('dictionaryControl').getDictionary(dictionaryName)!>
	<#if dictionary?? && dictionary.items??>
		<#list dictionary.items as lv>
		<#if value==lv.value><#return lv.label></#if>
		</#list>
	</#if>
  	<#return "">
</#function>