<#macro selectDictionary dictionaryName value="" required=false  headerKey="" headerValue="" strict=true dynamicAttributes...>
	<#local dictionary=statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('dictionaryControl').getDictionary(dictionaryName)!>
	<select class="<#if required> required</#if><#if !strict> combobox</#if> ${dynamicAttributes['class']!}"<#list dynamicAttributes?keys as attr><#if attr!='class' && attr!='dynamicAttributes'> ${attr}="${dynamicAttributes[attr]?html}"</#if><#if attr=='dynamicAttributes'><#list dynamicAttributes['dynamicAttributes']?keys as attr> ${attr}="${dynamicAttributes['dynamicAttributes'][attr]?html}"</#list></#if></#list>>
		<option value="${headerKey}">${headerValue}</option>
		<#local exists=false>
		<#if dictionary?? && dictionary.items?? && dictionary.items?size gt 0>
			<#local items = dictionary.items/>
			<#if !dictionary.groupable>
				<#list items as lv>
				<option value="${lv.value}"<#if value=lv.value><#local exists=true> selected="selected"</#if>>${lv.label}</option>
				</#list>
			<#else>
				<#local group = ""/>
				<#local index = 0/>
				<#list items as lv>
					<#if !lv.value?? || !lv.value?has_content>
						<#local label = lv.label/>
						<#if (!label?has_content) && group?has_content>
							<#local group = ""/>
							</optgroup>
						<#else>
							<#if group?has_content>
								</optgroup>
							</#if>
							<#local group = label/>
							<#if group?has_content>
								<optgroup label="${group}">
							</#if>
						</#if>
					<#else>
						<option value="${lv.value}"<#if value=lv.value><#local exists=true> selected="selected"</#if>>${lv.label}</option>
						<#if group?has_content && index==items?size-1>
						</optgroup>
						</#if>
					</#if>
					<#local index = index+1/>
				</#list>
			</#if>
		</#if>
		<#if !exists && value?has_content>
			<option value="${value}"selected="selected">${value}</option>
		</#if>
	</select>
</#macro>

<#function getDictionaryLabel dictionaryName value="">
	<#return statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('dictionaryControl').getDictionaryLabel(dictionaryName,value)>
</#function>

<#macro displayDictionaryLabel dictionaryName value="">
${getDictionaryLabel(dictionaryName,value)}<#t>
</#macro>

<#macro checkDictionary dictionaryName value=[] dynamicAttributes...>
	<#local dictionary=statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('dictionaryControl').getDictionary(dictionaryName)!>
		<#local index = 0/>
		<#if dictionary?? && dictionary.items?? && dictionary.items?size gt 0>
			<#local items = dictionary.items/>
			<#if !dictionary.groupable>
				<#list items as lv>
				<label for="${dictionaryName}-${index}" class="checkbox inline"><#t>
				<input id="${dictionaryName}-${index}" type="checkbox" value="${lv.value}" class="custom"<#if value?seq_contains(lv.value)> checked="checked"</#if><#list dynamicAttributes?keys as attr><#if attr!='dynamicAttributes'> ${attr}="${dynamicAttributes[attr]?html}"</#if><#if attr=='dynamicAttributes'><#list dynamicAttributes['dynamicAttributes']?keys as attr> ${attr}="${dynamicAttributes['dynamicAttributes'][attr]?html}"</#list></#if></#list>/>${lv.label}<#t>
				</label><#t>
				<#local index=index+1>
				</#list>
			<#else>
				<#local group = ""/>
				<#list items as lv>
					<#if !lv.value?? || !lv.value?has_content>
						<#local label = lv.label/>
						<#if (!label?has_content) && group?has_content>
							<#local group = ""/>
							</span><#lt>
						<#else>
							<#if group?has_content>
								</span><#lt>
							</#if>
							<#local group = label/>
							<#if group?has_content>
								<span class="checkboxgroup"><label for="${dictionaryName}-${group}" class="group"><input id="${dictionaryName}-${group}" type="checkbox" class="custom"/>${group}</label><#t>
							</#if>
						</#if>
					<#else>
						<label for="${dictionaryName}-${index}" class="checkbox inline"><#t>
						<input id="${dictionaryName}-${index}" type="checkbox" class="custom" value="${lv.value}"<#if value?seq_contains(lv.value)> checked="checked"</#if><#list dynamicAttributes?keys as attr><#if attr!='dynamicAttributes'> ${attr}="${dynamicAttributes[attr]?html}"</#if><#if attr=='dynamicAttributes'><#list dynamicAttributes['dynamicAttributes']?keys as attr> ${attr}="${dynamicAttributes['dynamicAttributes'][attr]?html}"</#list></#if></#list>/>${lv.label}<#t>
						</label><#t>
						<#if group?has_content && index==items?size-1>
						</span><#lt>
						</#if>
					</#if>
					<#local index = index+1/>
				</#list>
			</#if>
		</#if>
		<#list value as v>
		<#if !dictionary?? || !(dictionary.itemsAsMap!)?keys?seq_contains(v)>
		<label for="${dictionaryName}-${index}" class="checkbox inline"><#t>
		<input id="${dictionaryName}-${index}" type="checkbox" class="custom" value="${v}"checked="checked"<#list dynamicAttributes?keys as attr><#if attr!='dynamicAttributes'> ${attr}="${dynamicAttributes[attr]?html}"</#if><#if attr=='dynamicAttributes'><#list dynamicAttributes['dynamicAttributes']?keys as attr> ${attr}="${dynamicAttributes['dynamicAttributes'][attr]?html}"</#list></#if></#list>/>${v}<#t>
		</label><#t>
		<#local index = index+1/>
		</#if>
		</#list>
</#macro>