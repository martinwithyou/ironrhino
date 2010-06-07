<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText(entityName)}${action.getText('list')}</title>
</head>
<body>
<@rtstart action=entityName readonly=readonly/>
	<#list naturalIds?keys as key>
		<#assign columns=uiConfigs[key]>
		<#assign label=key>
		<#if columns.displayName??>
			<#assign label=columns.displayName>
		</#if>
		<@rttheadtd name=label cellName=entityName+'.'+key width=columns['width']! cellEdit=(readonly||naturalIdsImmatuable)?string('','click') readonly=readonly/>
	</#list>
	<#list uiConfigs?keys as key>
		<#if !(naturalIds?keys?seq_contains(key))>
			<#assign columns=uiConfigs[key]>
			<#assign label=key>
			<#if columns.displayName??>
				<#assign label=columns.displayName>
			</#if>
			<#if !(readonly||uiConfigs[key].readonly)>
				<#if uiConfigs[key].type=='input'||uiConfigs[key].type=='textarea'>
					<#assign cellEdit='click'/>
				</#if>
				<#if uiConfigs[key].type=='checkbox'>
					<#assign cellEdit='click,rt_select_template_boolean'/>
				</#if>
				<#if uiConfigs[key].type=='select'>
				<#assign cellEdit='click,rt_select_template_'+key/>
				</#if>
			<#else>
				<#assign cellEdit=''/>
			</#if>
			<@rttheadtd name=label width=uiConfigs[key]['width']! cellName=entityName+'.'+key cellEdit=cellEdit readonly=readonly/>
		</#if>
	</#list>
<@rtmiddle readonly=readonly/>
<#assign index=0>
<#list resultPage.result as entity>
<#assign index=index+1>
<@rttbodytrstart entity=entity odd=(index%2==1) readonly=readonly/>
	<#list naturalIds?keys as key>
		<@rttbodytd entity=entity value=entity[key] template=uiConfigs[key].template/>
	</#list>
	<#list uiConfigs?keys as key>
		<#if !(naturalIds?keys?seq_contains(key))>
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
</#list></div>
</#if>
</body>
</html></#escape>
