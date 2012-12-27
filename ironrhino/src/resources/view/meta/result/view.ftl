<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('view')}${action.getText(entityName)}</title>
</head>
<body>
	<div class="form-horizontal">
	<#list uiConfigs?keys as key>
		<#assign config=uiConfigs[key]>
		<#assign label=key>
		<#if config.alias??>
			<#assign label=config.alias>
		</#if>
		<div class="control-group">
			<label class="control-label">${action.getText(label)}</label>
			<div class="controls">
			<#if config.type=='textarea' >
				<pre>${entity[key]!}</pre>
			<#elseif config.type=='dictionary' >
				<#if displayDictionaryLabel??>
					<@displayDictionaryLabel dictionaryName=evalTemplate(config.templateName) value="${entity[key]!}"/>
				</#if>
			<#elseif config.type=='schema'>
				<#if printAttributes??>
					<@printAttributes attributes=entity.attributes grouping=true/>
				</#if>
			<#else>
					<#if !config.template?has_content>
						<#if entity[key]??>
							<#assign value=entity[key]>
							<#if value?is_boolean>
							${action.getText(value?string)}
							<#elseif value?is_hash&&value.displayName??>
							${value.displayName}
							<#else>
							${value?xhtml}
							</#if>
						</#if>
					<#else>
						<#assign temp=config.template?interpret>
						<@temp/>
					</#if>
			</#if>
			</div>
		</div>
	</#list>
	</div>
</body>
</html></#escape>