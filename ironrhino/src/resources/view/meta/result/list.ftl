<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText(entityName)}${action.getText('list')}</title>
</head>
<body>
<#assign rtconfig = richtableConfig!>
<#if rtconfig.listHeader?has_content>
<#assign listHeader=rtconfig.listHeader?interpret>
<@listHeader/>
</#if>
<@rtstart entityName=entityName readonly=readonly/>
<#assign size=0>
<#list uiConfigs?keys as key>
	<#if !uiConfigs[key].hiddenInList>
		<#assign size=size+1>
	</#if>
</#list>
<#assign index=0>
<#assign viewable=false>
<#assign hasSelect=false>
<#list uiConfigs?keys as key>
		<#assign config=uiConfigs[key]>
		<#if !config.hiddenInList>
			<#assign label=key>
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
						<#assign hasSelect=true>
						<#assign cellEdit='click,select,rt_select_template_'+key/>
					<#elseif config.type=='dictionary'>
						<#if selectDictionary??>
							<#assign hasSelect=true>
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
			<@rttheadtd name=label alias=config['alias']! width=config['width']! title=config['title']! class=config['cssClass']! cellName=entityName+'.'+key cellEdit=cellEdit readonly=readonly excludeIfNotEdited=config.excludeIfNotEdited resizable=viewable||!(readonly&&index==size)/>
		<#else>
			<#assign viewable=true>
		</#if>
</#list>
<@rtmiddle showActionColumn=rtconfig.actionColumnButtons?has_content||!readonly||viewable/>
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
			<#assign dynamicAttributes={}>
			<#if config.type=='listpick' && !readonly>
				<#assign dynamicAttributes={"class":"listpick","data-options":"{'url':'"+uiConfigs[key].pickUrl+"','name':'this','id':'this@data-cellvalue'}"}>
			</#if>
			<@rttbodytd entity=entity value=value template=uiConfigs[key].template dynamicAttributes=dynamicAttributes/>
		</#if>
	</#list>	
<@rttbodytrend entity=entity buttons=rtconfig.actionColumnButtons! editable=!readonly viewable=viewable/>
</#list>
<@rtend readonly=readonly searchable=searchable showPageSize=rtconfig.showPageSize! buttons=rtconfig.bottomButtons! enableable=enableable/>
<#if !readonly && hasSelect>
<div style="display: none;">
<#list uiConfigs?keys as key>
	<#assign config=uiConfigs[key]>
	<#if !config.hiddenInList>
		<#if config.type=='select'>
		<textarea id="rt_select_template_${key}">
		<select name="${entityName}.${key}">
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
<#if rtconfig.listFooter?has_content>
<#assign listFooter=rtconfig.listFooter?interpret>
<@listFooter/>
</#if>
</body>
</html></#escape>
