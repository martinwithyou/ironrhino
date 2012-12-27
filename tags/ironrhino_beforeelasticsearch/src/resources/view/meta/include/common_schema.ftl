<#macro editAttributes schemaName attributes=[] parameterNamePrefix=""  headerKey="" headerValue="">
	<input type="hidden" name="__datagrid_${parameterNamePrefix}attributes"/>
	<#if schemaName?index_of(",") gt 0>
		<#local schemaNames = schemaName?split(",")>
		<#list schemaNames as name>
			<#local temp=statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('schemaManager').findByNaturalId(true,[name])!>
			<#local tempisnotnull=statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('schemaManager').findByNaturalId(true,[name])??>
			<#if tempisnotnull>
				<#if !schema.name?? && temp??>
					<#local schema=temp>
				<#elseif schema.name?? && temp??>
					<#local schema=schema.merge(temp)>
				</#if>
			</#if>
		</#list>
	<#else>
		<#local schema=statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('schemaManager').findByNaturalId(true,[schemaName])!>
	</#if>
	<table class="datagrid table table-condensed nullable" style="width:100%;">
		<thead>
			<tr>
				<td>${action.getText('name')}</td>
				<td>${action.getText('value')}</td>
				<#if !schema.strict?? || !schema.strict>
				<td class="manipulate"></td>
				</#if>
			</tr>
		</thead>
		<tbody>
			<#if schema?? && schema.name??>
				<#local index = 0>
				<#list schema.fields as field>
				<#if field.type??>
					<#local type=field.type.name()/>
				<#else>
					<#local type='SELECT'/>
				</#if>
				<#if type=='GROUP'>
				<tr class="nontemplate" style="background-color:#F0F0F0;height:1em;">
					<td colspan="2">${field.name}<@s.hidden theme="simple" name="${parameterNamePrefix}attributes[${index}].name" value="${field.name}"/></td>
					<#if !schema.strict>
					<td class="manipulate"></td>
					</#if>
				</tr>
				<#else>
				<#local persistValueExists=false>
				<#list attributes as attr>
					<#if attr.name==field.name && attr.value??>
						<#local persistValue=attr.value>
						<#local persistValueExists=true>
						<#break/>
					</#if>
				</#list>
				<tr>
					<td><@s.textfield theme="simple" name="${parameterNamePrefix}attributes[${index}].name" value="${field.name}" readonly=field.strict?string/></td>
					<td>
						<#if type=='SELECT'>
							<select name="${parameterNamePrefix}attributes[${index}].value" class="textonadd<#if field.required> required</#if><#if !field.strict> combox</#if>">
								<option value="${headerKey}">${headerValue}</option>
								<#list field.values as value>
								<option value="${value}"<#if persistValueExists && persistValue=value> selected="selected"</#if>>${value}</option>
								</#list>
								<#if !field.strict && persistValueExists && persistValue!='' && !field.values?seq_contains(persistValue)>
								<option value="${persistValue}" selected="ed">${persistValue}</option>
								</#if>
							</select>
						<#elseif type=='CHECKBOX'>
							<#if persistValueExists>
								<#local persistValueArray=persistValue?split(',')/>
							<#else>
								<#local persistValueArray=[]/>
							</#if>
							<#list field.values as value>
								<input type="checkbox" name="${parameterNamePrefix}attributes[${index}].value" value="${value}" class="textonadd"<#list persistValueArray as tempValue><#if tempValue=value> checked="checked"<#break/></#if></#list>/><span class="removeonadd">${value}</span>
							</#list>
						<#elseif type=='INPUT'>
							<input type="text" name="${parameterNamePrefix}attributes[${index}].value"<#if persistValueExists> value="${persistValue}"</#if><#if field.required> class="required"</#if>/>
						</#if>
					</td>
					<#if !schema.strict>
					<td class="manipulate"></td>
					</#if>
				</tr>
				</#if>
				<#local index = index+1>
				</#list>
				<#if !schema.strict>
				<#list attributes as attr>
					<#local inschema=false>
					<#list schema.fields as field>
						<#if attr.name==field.name>
							<#local inschema=true>
							<#break/>
						</#if>
					</#list>
					<#if !inschema && attr.value?? && attr.value?has_content>
					<tr>
						<td><input type="text" name="${parameterNamePrefix}attributes[${index}].name" value="${attr.name!}"/></td>
						<td><input type="text" name="${parameterNamePrefix}attributes[${index}].value" value="${attr.value!}"/></td>
						<td class="manipulate"></td>
					</tr>
					<#local index = index+1>
					</#if>
				</#list>
				</#if>
				<#else>
					<#local size = 0>
					<#local isnew=true>
					<#if attributes?? && attributes?size gt 0>
						<#local size = attributes?size-1>
						<#local isnew=false>
					</#if>
					<#local index = 0>
					<#list 0..size as var>
						<#if isnew || attributes[var].value?? && attributes[var].value?has_content>
							<tr>
								<td><input type="text" name="${parameterNamePrefix}attributes[${index}].name"<#if !isnew> value="${attributes[var].name!}</#if>"/></td>
								<td><input type="text" name="${parameterNamePrefix}attributes[${index}].value"<#if !isnew> value="${attributes[var].value!}</#if>"/></td>
								<td class="manipulate"></td>
							</tr>
							<#local index = index+1>
						</#if>
					</#list>
				</#if>
		</tbody>
	</table>
</#macro>

<#macro printAttributes attributes grouping=true>
	<#if attributes?? && attributes?size gt 0>
		<#if !grouping>
			<ul class="attributes">
			<#list attributes as attr>
				<#if attr.value?? && attr.value?has_content>
					<li><span class="name">${attr.name}:<span><span class="value">${attr.value}</span></li>
				</#if>
			</#list>
			</ul>
		<#else>
			<ul class="attributes">
			<#local group = ""/>
			<#local index = 0/>
			<#list attributes as attr>
				<#if !attr.value?? || !attr.value?has_content>
					<#local name = attr.name/>
					<#if (!name?has_content)>
						<#local group = ""/>
						</ul></li>
					<#else>
						<#if group?has_content>
							</ul></li>
						</#if>
						<#local group = name/>
						<#if group?has_content>
							<li class="group"><span class="group">${group}</span>
							<ul>
						</#if>
					</#if>
				<#else>
					<li><span class="name">${attr.name}:<span><span class="value">${attr.value}</span></li>
					<#if group?has_content && index==attributes?size-1>
						</ul></li>
					</#if>
				</#if>
				<#local index = index+1/>
			</#list>
			</ul>
		</#if>
	</#if>
</#macro>