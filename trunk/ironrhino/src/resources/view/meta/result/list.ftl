<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText(entityName)}${action.getText('list')}</title>
</head>
<body>
<@rtstart entityName=entityName readonly=readonly/>
	<#assign index=0>
	<#list uiConfigs?keys as key>
			<#assign config=uiConfigs[key]>
			<#if !config.hiddenInList>
				<#assign label=key>
				<#if config.displayName??>
					<#assign label=config.displayName>
				</#if>
				<#if !(readonly||config.readonly) && !(naturalIds?keys?seq_contains(key)&&!naturalIdMutable)>
					<#assign cellEdit=config.cellEdit!/>
					<#if cellEdit==''>
						<#if config.type=='input'>
							<#assign cellEdit='click'/>
						<#elseif config.type=='textarea'>
							<#assign cellEdit='click,textarea'/>
						<#elseif config.type=='checkbox'>
							<#assign cellEdit='click,boolean'/>
						<#elseif config.type=='select'>
							<#assign cellEdit='click,select,rt_select_template_'+key/>
						<#elseif config.type=='dictionary'>
							<#if selectDictionary??>
								<#assign cellEdit='click,select,rt_select_template_'+key/>
							<#else>
								<#assign cellEdit='click'/>
							</#if>	
						</#if>
					</#if>
				<#else>
					<#assign cellEdit=''/>
				</#if>
				<#assign index=index+1>
				<@rttheadtd name=label width=config['width']! cellName=entityName+'.'+key cellEdit=cellEdit readonly=readonly excludeIfNotEdited=config.excludeIfNotEdited resizable=!(readonly&&index==uiConfigs?size)/>
			</#if>
	</#list>
<@rtmiddle readonly=readonly/>
<#assign index=0>
<#list resultPage.result as entity>
<#assign index=index+1>
<@rttbodytrstart entity=entity readonly=readonly/>
	<#list uiConfigs?keys as key>
		<#assign config=uiConfigs[key]>
		<#if !config.hiddenInList>
			<#assign value = entity[key]!>
			<#if config.type=='dictionary' && selectDictionary??>
							<#assign value=getDictionaryLabel(evalTemplate(config.templateName),value)/>	
			</#if>
			<@rttbodytd entity=entity value=value template=uiConfigs[key].template/>
		</#if>
	</#list>	
<@rttbodytrend entity=entity readonly=readonly/>
</#list>
<@rtend readonly=readonly searchable=searchable/>
<#if !readonly>
<div style="display: none">
<#list uiConfigs?keys as key>
	<#assign config=uiConfigs[key]>
	<#if !config.hiddenInList>
		<#if config.type=='select'>
		<textarea id="rt_select_template_${key}">
		<select onblur="Richtable.updateCell(this)"
				style="width: 100%;" name="${entityName}.${key}">
				<#if !config.required>
				<option value=""></option>
				</#if>
				<#list lists[key] as var>
				<option value="<#if config.listKey=='name'&&var.name()??>${var.name()}<#else>${var[config.listKey]}</#if>">${var[config.listValue]}</option>
				</#list>
		</select>
		</textarea>
		<#elseif config.type=='dictionary' && selectDictionary??>
		<textarea id="rt_select_template_${key}">
		<@selectDictionary dictionaryName=evalTemplate(config.templateName) id=key name="${entityName}.${key}" required=config.required  onblur="Richtable.updateCell(this)" style="width: 100%;"/>
		</textarea>
		</#if>
	</#if>
</#list></div>
</#if>
</body>
</html></#escape>
