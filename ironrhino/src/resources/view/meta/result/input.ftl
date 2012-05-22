<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title><#if !entity??><#assign entity=entityName?eval></#if><#if entity.new>${action.getText('create')}<#else>${action.getText('edit')}</#if>${action.getText(entityName)}</title>
</head>
<body>
<@s.form action="${getUrl(actionBaseUrl+'/save')}" method="post" cssClass="ajax">
	<#if !entity.new>
	<@s.hidden name="${entityName}.id" />
	</#if>
	<#list uiConfigs?keys as key>
		<#assign config=uiConfigs[key]>
		<#assign label=key>
		<#if config.displayName??>
			<#assign label=config.displayName>
		</#if>
		<#if naturalIds?keys?seq_contains(key)>
			<#assign readonly=!naturalIdMutable&&!action.isNew()>
		<#else>
			<#assign readonly=config.readonly>
		</#if>
		<#if config.type=='textarea'>
			<@s.textarea label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" cols="50" rows="5" readonly="${readonly?string}" dynamicAttributes=config.dynamicAttributes/>
		<#elseif config.type=='checkbox'>
			<#if !readonly>
				<@s.checkbox label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" dynamicAttributes=config.dynamicAttributes />
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
			<@s.hidden id="${key}Id" name="${entityName}.${key}.id" />
			<#if !readonly>
				<div class="control-group listpick" data-options="{'url':'<@url value="${config.pickUrl}"/>','name':'${key}','id':'${key}Id'}">
					<label class="control-label" for="${key}"><span style="cursor:pointer;">${action.getText(key)}</span></label>
					<div class="controls">
					<span id="${key}"><#if entity[key]??>${entity[key]!}<a class="remove" href="#">&times;</a><#else>...</#if></span>
					</div>
				</div>
			<#else>
				<div class="control-group">
					<label class="control-label" for="${key}"><span style="cursor:pointer;">${action.getText(key)}</span></label>
					<div class="controls">
					<span id="${key}">${entity[key]!}</span>
					</div>
				</div>
			</#if>
		<#elseif config.type=='dictionary' && selectDictionary??>
			<div class="control-group">
			<label class="control-label" for="${key}"><span style="cursor:pointer;">${action.getText(key)}</span></label>
			<div class="controls">
			<#if !readonly>
				<@selectDictionary dictionaryName=evalTemplate(config.templateName) id=key name="${entityName}.${key}" value="${entity[key]!}" required=config.required class="${config.cssClass}" dynamicAttributes=config.dynamicAttributes/>
			<#else>
				<@s.hidden name="${entityName}.${key}"/>
				<span id="${key}"><@displayDictionaryLabel dictionaryName=evalTemplate(config.templateName) value="${entity[key]!}"/></span>
			</#if>
			</div>
			</div>
		<#elseif config.type=='schema'>
			<#if editAttributes??>
				<div id="editAttributes">
				<@editAttributes schemaName=evalTemplate(config.templateName) attributes=entity.attributes parameterNamePrefix=entityName+'.'/>
				</div>
			</#if>
		<#else>
			<#if config.maxlength gt 0>
				<@s.textfield label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" size="${(config.size>0)?string(config.size,20)}" maxlength="${(config.maxlength)}" readonly="${readonly?string}" dynamicAttributes=config.dynamicAttributes />
			<#else>
				<@s.textfield label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" size="${(config.size>0)?string(config.size,20)}" readonly="${readonly?string}" dynamicAttributes=config.dynamicAttributes />
			</#if>
		</#if>
	</#list>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>