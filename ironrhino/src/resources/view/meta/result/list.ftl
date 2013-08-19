<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText(entityName)}${action.getText('list')}</title>
</head>
<body>
<#assign readonly=readonlyConfig.value>
<#if richtableConfig.listHeader?has_content>
<@richtableConfig.listHeader?interpret/>
</#if>
<#if richtableConfig.formid?has_content>
<#assign formid><@richtableConfig.formid?interpret/></#assign>
</#if>
<#if richtableConfig.formHeader?has_content>
<#assign formHeader><@richtableConfig.formHeader?interpret/></#assign>
</#if>
<#if richtableConfig.formFooter?has_content>
<#assign formFooter><@richtableConfig.formFooter?interpret/></#assign>
</#if>
<@rtstart formid=formid! entityName=entityName formHeader=formHeader!/>
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
<@rtmiddle showActionColumn=richtableConfig.actionColumnButtons?has_content||!readonly||viewable/>
<#assign index=0>
<#list resultPage.result as entity>
<#assign index=index+1>
<#assign entityReadonly = readonly/>
<#if !entityReadonly && readonlyConfig.expression?has_content><#assign entityReadonly=readonlyConfig.expression?eval></#if>
<#assign rowDynamicAttributes={}>
<#if richtableConfig.rowDynamicAttributes?has_content>
<#assign rowDynamicAttributes><@richtableConfig.rowDynamicAttributes?interpret /></#assign>
<#if rowDynamicAttributes?has_content>
<#assign rowDynamicAttributes=rowDynamicAttributes?eval>
<#else>
<#assign rowDynamicAttributes={}>
</#if>
</#if>
<#if !readonly&&entityReadonly>
<#assign rowDynamicAttributes=rowDynamicAttributes+{"data-readonly":"true"}>
<#if !readonlyConfig.deletable>
<#assign rowDynamicAttributes=rowDynamicAttributes+{"data-deletable":"false"}>
</#if>
</#if>
<@rttbodytrstart entity=entity dynamicAttributes=rowDynamicAttributes/>
<#list uiConfigs?keys as key>
	<#assign config=uiConfigs[key]>
	<#if !config.hiddenInList>
		<#assign value = entity[key]!>
		<#if config.type=='dictionary' && selectDictionary??>
			<#assign templateName><@config.templateName?interpret /></#assign>
			<#assign value=getDictionaryLabel(templateName,value)/>	
		</#if>
		<#assign dynamicAttributes={}>
		<#if config.type=='listpick'&&!entityReadonly&&!config.readonly&&!(config.readonlyExpression?has_content&&config.readonlyExpression?eval)>
			<#assign dynamicAttributes={"class":"listpick","data-cellvalue":(value.id?string)!,"data-options":"{'url':'"+uiConfigs[key].pickUrl+"','name':'this','id':'this@data-cellvalue'}"}>
		</#if>
		<#if config.readonlyExpression?has_content && config.readonlyExpression?eval>
		<#assign dynamicAttributes=dynamicAttributes+{'data-readonly':'true'}/>
		</#if>
		<@rttbodytd entity=entity value=value template=uiConfigs[key].template cellDynamicAttributes=config.cellDynamicAttributes dynamicAttributes=dynamicAttributes/>
	</#if>
</#list>	
<@rttbodytrend entity=entity buttons=richtableConfig.actionColumnButtons! editable=!readonly viewable=viewable entityReadonly=entityReadonly/>
</#list>
<@rtend readonly=readonly deletable=!readonly||readonlyConfig.deletable searchable=searchable filterable=richtableConfig.filterable showPageSize=richtableConfig.showPageSize! buttons=richtableConfig.bottomButtons! enableable=enableable formFooter=formFooter!/>
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
				<option value="<#if config.listKey=='top'>${var?string}<#elseif config.listKey=='name' && var.name?is_method>${var.name()}<#else>${var[config.listKey]}</#if>"><#if config.listValue=='top'>${var?string}<#elseif config.listValue=='name' && var.name?is_method>${var.name()}<#else>${var[config.listValue]}</#if></option>
				</#list>
		</select>
		</textarea>
		<#elseif config.type=='dictionary' && selectDictionary??>
		<textarea id="rt_select_template_${key}">
		<#assign templateName><@config.templateName?interpret /></#assign>
		<@selectDictionary dictionaryName=templateName id=key name="${entityName}.${key}" required=config.required/>
		</textarea>
		</#if>
	</#if>
</#list></div>
</#if>
<#if richtableConfig.listFooter?has_content>
<@richtableConfig.listFooter?interpret/>
</#if>
</body>
</html></#escape>
