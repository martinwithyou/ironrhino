<#macro selectDictionary dictionaryName value="" required=false  headerKey="" headerValue="" strict=true dynamicAttributes...>
	<#local dictionary=statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('dictionaryControl').getDictionary(dictionaryName)!>
	<select class="<#if required> required</#if><#if !strict> combox</#if> ${dynamicAttributes['class']!}"<#list dynamicAttributes?keys as attr><#if attr!='class' && attr!='dynamicAttributes'> ${attr}="${dynamicAttributes[attr]?html}"</#if><#if attr=='dynamicAttributes'><#list dynamicAttributes['dynamicAttributes']?keys as attr> ${attr}="${dynamicAttributes['dynamicAttributes'][attr]?html}"</#list></#if></#list>>
		<option value="${headerKey}">${headerValue}</option>
		<#if dictionary?? && dictionary.groupedItems?? && dictionary.groupedItems?keys?size gt 0>
		<#local groupedItemsMap = dictionary.groupedItems>
		<#local groups = groupedItemsMap.keySet()>
		<#if groups?size == 1>
			<#list groupedItemsMap[groups.iterator().next()] as lv>
			<option value="${lv.value}"<#if value=lv.value> selected="selected"</#if>>${lv.label}</option>
			</#list>
		<#else>
			<#list groups as group>
			<#local groupedItemsList = groupedItemsMap[group]>
			<optgroup label="${group}">
				<#list groupedItemsList as lv>
				<option value="${lv.value}"<#if value=lv.value> selected="selected"</#if>>${lv.label}</option>
				</#list>
			</optgroup>
			</#list>
		</#if>
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