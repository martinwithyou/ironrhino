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
				<#if entity[key]?has_content>
				<pre>${entity[key]!}</pre>
				</#if>
			<#elseif config.type=='dictionary' >
				<#if displayDictionaryLabel??>
					<#assign templateName><@config.templateName?interpret /></#assign>
					<@displayDictionaryLabel dictionaryName=templateName value="${entity[key]!}"/>
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
							<#elseif value?is_sequence>
								<ol class="unstyled">
								<#list value as item>
									<li>
									<#if item?is_sequence>
											<ol class="unstyled" style="padding-bottom:10px;">
											<#list item as it>
												<li>${it}</li>
											</#list>
											</ol>
									<#elseif item?is_hash_ex>
											<ul class="unstyled" style="padding-bottom:10px;">
											<#list item?keys as k>
												<#if k!='class' && item[k]?? && !item[k]?is_method>
												<li>${k}: ${item[k]?string}</li>
												</#if>
											</#list>
											</ul>
									<#else>
											${item!}
									</#if>
									</li>
								</#list>
								</ol>
							<#elseif value?is_hash_ex && value.displayName??>
									${value.displayName!}
							<#else>
							${value?string!}
							</#if>
						</#if>
					<#else>
						<#assign value=entity[key]!>
						<@config.template?interpret/>
					</#if>
			</#if>
			</div>
		</div>
	</#list>
	</div>
</body>
</html></#escape>