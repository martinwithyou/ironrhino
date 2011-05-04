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
		<#if config.type=='input'>
				<#if !readonly>
					<@s.textfield label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" size="${(config.size>0)?string(config.size,20)}" />
				<#else>
					<@s.textfield label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" size="${(config.size>0)?string(config.size,20)}" readonly="true" />
				</#if>
			</#if>
			<#if config.type=='textarea'>
				<#if !readonly>
					<@s.textarea label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" cols="50" rows="5" />
				<#else>
					<@s.textarea label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" cols="50" rows="5" readonly="true" />
				</#if>
			</#if>
			<#if config.type=='checkbox'>
				<#if !readonly>
					<@s.checkbox label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" />
				<#else>
					<@s.hidden name="${entityName}.${key}" />
					<@s.checkbox label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" disabled="true" />
				</#if>
			</#if>
			<#if config.type=='select'>
				<#if !readonly>
					<@s.select label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" list="lists.${key}" listKey="${config.listKey}" listValue="${config.listValue}"  headerKey="" headerValue=""/>
				<#else>
					<@s.hidden name="${entityName}.${key}" />
					<@s.select label="%{getText('${label}')}" name="${entityName}.${key}" cssClass="${config.cssClass}" list="lists.${key}" listKey="${config.listKey}" listValue="${config.listValue}"  headerKey="" headerValue="" disabled="true" />
				</#if>
			</#if>
	</#list>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>