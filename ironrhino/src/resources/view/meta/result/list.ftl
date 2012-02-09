<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText(entityName)}${action.getText('list')}</title>
</head>
<body>
<@rtstart entityName=entityName action=entityName readonly=readonly/>
	<#assign index=0>
	<#list uiConfigs?keys as key>
			<#assign config=uiConfigs[key]>
			<#if !config.hideInList>
				<#assign label=key>
				<#if config.displayName??>
					<#assign label=config.displayName>
				</#if>
				<#if !(readonly||config.readonly) && !(naturalIds?keys?seq_contains(key)&&!naturalIdMutable)>
					<#assign cellEdit=config.cellEdit!/>
					<#if cellEdit==''>
						<#if config.type=='input'>
							<#assign cellEdit='click'/>
						</#if>
						<#if config.type=='textarea'>
							<#assign cellEdit='click,textarea'/>
						</#if>
						<#if config.type=='checkbox'>
							<#assign cellEdit='click,boolean'/>
						</#if>
						<#if config.type=='select'>
							<#assign cellEdit='click,select,rt_select_template_'+key/>
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
<@rttbodytrstart entity=entity odd=(index%2==1) readonly=readonly/>
	<#list uiConfigs?keys as key>
		<#assign config=uiConfigs[key]>
		<#if !config.hideInList>
			<@rttbodytd entity=entity value=entity[key]! template=uiConfigs[key].template/>
		</#if>
	</#list>	
<@rttbodytrend entity=entity readonly=readonly/>
</#list>
<@rtend readonly=readonly searchable=searchable/>
<#if !readonly>
<div style="display: none">
<#list uiConfigs?keys as key>
	<#assign config=uiConfigs[key]>
	<#if !config.hideInList>
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
		</#if>
	</#if>
</#list></div>
</#if>
</body>
</html></#escape>
