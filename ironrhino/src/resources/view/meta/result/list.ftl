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
<#list uiConfigs.entrySet() as entry>
	<#assign hidden=entry.value.hiddenInList.value>
	<#if !hidden && entry.value.hiddenInList.expression?has_content>
	<#assign hidden=entry.value.hiddenInList.expression?eval/>
	</#if>
	<#if !hidden>
		<#assign size=size+1>
	</#if>
</#list>
<#assign index=0>
<#assign viewable=false>
<#assign hasSelect=false>
<#list uiConfigs.entrySet() as entry>
		<#assign key=entry.key>
		<#assign config=entry.value>
		<#assign hidden=config.hiddenInList.value>
		<#if !hidden && config.hiddenInList.expression?has_content>
		<#assign hidden=config.hiddenInList.expression?eval>
		</#if>
		<#if !hidden>
			<#assign label=key>
			<#if !(readonly||config.readonly.value) && !(naturalIds?keys?seq_contains(key)&&!naturalIdMutable)>
				<#assign cellEdit=config.cellEdit!/>
				<#if cellEdit==''>
					<#if config.type=='input'>
						<#assign cellEdit='click'/>
					<#elseif config.type=='textarea'>
						<#assign cellEdit='click,textarea'/>
					<#elseif config.type=='checkbox'>
						<#assign cellEdit='click,boolean'/>
					<#elseif config.type=='enum'>
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
<#list uiConfigs.entrySet() as entry>
	<#assign key=entry.key>
	<#assign config=entry.value>
	<#assign hidden=config.hiddenInList.value>
	<#if !hidden && config.hiddenInList.expression?has_content>
	<#assign hidden=config.hiddenInList.expression?eval>
	</#if>
	<#if !hidden>
		<#assign value = entity[key]!>
		<#if config.type=='dictionary' && selectDictionary??>
			<#assign templateName><@config.templateName?interpret /></#assign>
			<#assign value=getDictionaryLabel(templateName,value)/>	
		</#if>
		<#assign dynamicAttributes={}>
		<#if config.type=='listpick'&&!entityReadonly&&!config.readonly.value&&!(config.readonly.expression?has_content&&config.readonly.expression?eval)>
			<#assign dynamicAttributes={"class":"listpick","data-cellvalue":(value.id?string)!,"data-options":"{'url':'"+config.pickUrl+"','name':'this','id':'this@data-cellvalue'}"}>
		</#if>
		<#if config.readonly.expression?has_content && config.readonly.expression?eval>
		<#assign dynamicAttributes=dynamicAttributes+{'data-readonly':'true'}/>
		</#if>
		<#assign template=config.template/>
		<#if config.listTemplate!=''>
		<#assign template=config.listTemplate/>
		</#if>
		<@rttbodytd entity=entity value=value template=template cellDynamicAttributes=config.cellDynamicAttributes dynamicAttributes=dynamicAttributes/>
	</#if>
</#list>
<@rttbodytrend entity=entity buttons=richtableConfig.actionColumnButtons! editable=!readonly viewable=viewable entityReadonly=entityReadonly/>
</#list>
<@rtend readonly=readonly deletable=!readonly||readonlyConfig.deletable searchable=searchable filterable=richtableConfig.filterable showPageSize=richtableConfig.showPageSize! buttons=richtableConfig.bottomButtons! enableable=enableable formFooter=formFooter!/>
<#if !readonly && hasSelect>
<div style="display: none;">
<#list uiConfigs.entrySet() as entry>
	<#assign key=entry.key>
	<#assign config=entry.value>
	<#assign hidden=config.hiddenInList.value>
	<#if !hidden && config.hiddenInList.expression?has_content>
	<#assign hidden=config.hiddenInList.expression?eval/>
	</#if>
	<#if !hidden>
		<#if config.type=='enum'>
		<textarea id="rt_select_template_${key}">
		<#if config.required>
		<@s.select theme="simple" name=entityName+"."+key list="@${config.propertyType.name}@values()" listKey=config.listKey listValue=config.listValue/>
		<#else>
		<@s.select theme="simple" name=entityName+"."+key list="@${config.propertyType.name}@values()" listKey=config.listKey listValue=config.listValue headerKey="" headerValue=""/>
		</#if>
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
