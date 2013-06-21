<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title><#if !entity??><#assign entity=entityName?eval></#if><#assign isnew = !entity??||entity.new/><#if isnew>${action.getText('create')}<#else>${action.getText('edit')}</#if>${action.getText(entityName)}</title>
</head>
<body>
<@s.form action="${getUrl(actionBaseUrl+'/save')}" method="post" cssClass="ajax form-horizontal">
	<#if !isnew>
	<@s.hidden name="${entityName}.id" />
	</#if>
	<#list uiConfigs?keys as key>
		<#assign config=uiConfigs[key]>
		<#assign templateName><@config.templateName?interpret /></#assign>
		<#if !config.hiddenInInput>
		<#assign label=key>
		<#if config.alias??>
			<#assign label=config.alias>
		</#if>
		<#assign value=(entity[key])!/>
		<#assign readonly=naturalIds?keys?seq_contains(key)&&!naturalIdMutable&&!isnew||config.readonly||config.readonlyExpression?has_content&&config.readonlyExpression?eval>
		<#if !(entity.new && readonly)>
			<#if config.type=='textarea'>
				<@s.textarea label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" cssStyle="${(config.cssClass?contains('span')||config.cssClass?contains('input-'))?string('','width:400px;')}height:150px;" readonly=readonly dynamicAttributes=config.dynamicAttributes/>
			<#elseif config.type=='checkbox'>
				<#if !readonly>
					<@s.checkbox label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass+config.cssClass?has_content?string(' ','')}custom" dynamicAttributes=config.dynamicAttributes />
				<#else>
					<@s.hidden name="${entityName}.${key}" />
					<@s.checkbox label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" disabled="true" dynamicAttributes=config.dynamicAttributes />
				</#if>
			<#elseif config.type=='select'>
				<#if !readonly>
					<@s.select label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" list="lists.${key}" listKey="${config.listKey}" listValue="${config.listValue}"  headerKey="" headerValue="" dynamicAttributes=config.dynamicAttributes/>
				<#else>
					<@s.hidden name="${entityName}.${key}" value="%{${entityName+'.'+key+'.id'}}"/>
					<@s.select label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" list="lists.${key}" listKey="${config.listKey}" listValue="${config.listValue}"  headerKey="" headerValue="" disabled="true" dynamicAttributes=config.dynamicAttributes />
				</#if>
			<#elseif config.type=='listpick'>
				<#if !readonly>
					<div class="control-group listpick" data-options="{'url':'<@url value="${config.pickUrl}"/>','name':'#${key}','id':'#${key}Id'}">
					<#if config.required>
						<@s.hidden id="${key}Id" name="${entityName}.${key}.id" cssClass="required"/>
					<#else>
						<@s.hidden id="${key}Id" name="${entityName}.${key}.id"/>
					</#if>
						<label class="control-label" for="${key}">${action.getText(label)}</label>
						<div class="controls">
						<span id="${key}"><#if entity[key]??><#if entity[key].fullname??>${entity[key].fullname!}<#else>${entity[key]!}</#if><a class="remove" href="#">&times;</a><#else>...</#if></span>
						</div>
					</div>
				<#else>
					<div class="control-group">
						<label class="control-label" for="${key}">${action.getText(label)}</label>
						<div class="controls">
						<span id="${key}">${entity[key]!}</span>
						</div>
					</div>
				</#if>
			<#elseif config.type=='dictionary' && selectDictionary??>
				<div class="control-group">
				<label class="control-label" for="${key}">${action.getText(label)}</label>
				<div class="controls">
				<#if !readonly>
					<@selectDictionary dictionaryName=templateName id=key name="${entityName}.${key}" value="${entity[key]!}" required=config.required class="${config.cssClass}" dynamicAttributes=config.dynamicAttributes/>
				<#else>
					<@s.hidden name="${entityName}.${key}"/>
					<span id="${key}"><@displayDictionaryLabel dictionaryName=templateName value="${entity[key]!}"/></span>
				</#if>
				</div>
				</div>
			<#elseif config.type=='schema'>
				<#if editAttributes??>
					<div id="editAttributes">
					<@editAttributes schemaName=templateName attributes=entity.attributes parameterNamePrefix=entityName+'.'/>
					</div>
				</#if>
			<#else>
				<@s.textfield label="%{getText('${label}')}" name="${entityName}.${key}" type="${(config.inputType!)}" cssClass="${config.cssClass}" maxlength="${(config.maxlength gt 0)?string(config.maxlength,'')}" readonly=readonly dynamicAttributes=config.dynamicAttributes />
			</#if>
		</#if>
		</#if>
	</#list>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>