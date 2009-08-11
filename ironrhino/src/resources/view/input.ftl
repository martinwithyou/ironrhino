<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Create/Edit ${entityName?cap_first}</title>
</head>
<body>
<@s.form action="save" method="post" cssClass="ajax">
	<@s.hidden name="${entityName}.id" />
	<#list naturalIds?keys as name>
		<#assign config=formElements[name]>
		<@s.textfield label="%{getText('${name}')}" name="${entityName}.${name}" 
		readonly="${(naturalIdsImmatuable&&!action.isNew())?string}"
		 cssClass="${config.cssClass}" 
		 size="${(config.size>0)?string(config.size,20)}"/>
	</#list>

	<#list formElements?keys as key>
		<#if !naturalIds?keys?seq_contains(key)>
			<#assign config=formElements[key]>
			<#if config.type=='input'>
				<@s.textfield label="%{getText('${key}')}" name="${entityName}.${key}"
					readonly="config.readonly" cssClass="${config.cssClass}"
					size="${(config.size>0)?string(config.size,20)}" />
			</#if>
			<#if config.type=='textarea'>
				<@s.textarea label="%{getText('${key}')}" name="${entityName}.${key}"
					readonly="${config.readonly?string}" cssClass="${config.cssClass}"
					cols="50" rows="10" />
			</#if>
			<#if config.type=='checkbox'>
				<@s.checkbox label="%{getText('${key}')}" name="${entityName}.${key}"
					cssClass="${config.cssClass}" />
			</#if>
			<#if config.type=='select'>
				<@s.select label="%{getText('${key}')}" name="${entityName}.${key}"
					list="@${config.enumClass}@values()" listKey="name" listValue="displayName" />
			</#if>
		</#if>
	</#list>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>