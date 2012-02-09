<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title><#if entity.new>${action.getText('create')}<#else>${action.getText('edit')}</#if>${action.getText(entityName)}</title>
</head>
<body>
<@s.form action="save" method="post" cssClass="ajax">
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
			<@s.textarea label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" cols="50" rows="5" readonly="${readonly?string}" />
		<#elseif config.type=='checkbox'>
			<#if !readonly>
				<@s.checkbox label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" />
			<#else>
				<@s.hidden name="${entityName}.${key}" />
				<@s.checkbox label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" disabled="true" />
			</#if>
		<#elseif config.type=='select'>
			<#if !readonly>
				<@s.select label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" list="lists.${key}" listKey="${config.listKey}" listValue="${config.listValue}"  headerKey="" headerValue=""/>
			<#else>
				<@s.hidden name="${entityName}.${key}" value="%{${entityName+'.'+key+'.id'}}"/>
				<@s.select label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" list="lists.${key}" listKey="${config.listKey}" listValue="${config.listValue}"  headerKey="" headerValue="" disabled="true" />
			</#if>
		<#elseif config.type=='listpick'>
			<@s.hidden id="${key}Id" name="${entityName}.${key}.id" />
			<#if !readonly>
				<div class="field listpick" pickoptions="{'url':'<@url value="${config.pickUrl}"/>','name':'${key}','id':'${key}Id'}">
					<label class="field" for="${key}"><span style="cursor:pointer;">${action.getText(key)}</span></label>
					<#if entity[key]??><span id="${key}">${entity[key]!}</span><a class="close">x</a><#else><span id="${key}">...</span></#if>
				</div>
			<#else>
				<div class="field">
					<label class="field" for="${key}"><span style="cursor:pointer;">${action.getText(key)}</span></label>
					<span id="${key}">${entity[key]!}</span>
				</div>
			</#if>
		<#elseif config.type=='dictionary' && selectDictionary??>
			<div class="field">
			<label class="field" for="${key}"><span style="cursor:pointer;">${action.getText(key)}</span></label>
			<#if !readonly>
				<@selectDictionary dictionaryName=config.dictionaryName id=key name="${entityName}.${key}" value="${entity[key]!}" required=config.required/>
			<#else>
				<@s.hidden name="${entityName}.${key}"/>
				<span id="${key}"><@displayDictionaryLabel dictionaryName=config.dictionaryName value="${entity[key]!}"/></span>
			</#if>
			</div>
		<#else>	
			<@s.textfield label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" size="${(config.size>0)?string(config.size,20)}" readonly="${readonly?string}" />
		</#if>
	</#list>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>