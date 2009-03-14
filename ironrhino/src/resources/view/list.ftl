<#include "richtable-macro.ftl"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>List ${entityName?cap_first}</title>
</head>
<body>
<#assign readonly="${readonly?string}"/>
<@rtstart action="${entityName}" readonly="${readonly}"/>
	<#list naturalIds?keys as name>
		<@rttheadtd name="${name}" editable=(readonly=='false')&&!naturalIdsImmatuable/>
	</#list>
	<#list formElements?keys as name>
		<#if !(naturalIds?keys?seq_contains(name))>
			<@rttheadtd name="${name}" editable=(readonly=='false')&&!formElements[name].readonly/>
		</#if>
	</#list>
<@rtmiddle readonly="${readonly}"/>
<#assign index=0>
<#list resultPage.result as entity>
<#assign index=index+1>
<@rttbodytrstart rowid="${entity.id}" odd=(index%2==1) readonly="${readonly}"/>
	<#list naturalIds?keys as name>
		<#if (readonly=='false')&&!naturalIdsImmatuable>
		<@rttbodytd cellName="${entityName}.${name}" value="${entity[name]?if_exists?string}" cellEdit="input"/>
		<#else>
		<@rttbodytd cellName="${entityName}.${name}" value="${entity[name]?if_exists?string}"/>
		</#if>
	</#list>
	<#list formElements?keys as name>
		<#if !(naturalIds?keys?seq_contains(name))>
			<#if (readonly=='false')&&!formElements[name].readonly>
				<#if formElements[name].type=='input'||formElements[name].type=='textarea'>
					<@rttbodytd cellName="${entityName}.${name}" value="${entity[name]?if_exists?string}" cellEdit="input"/>
				</#if>
				<#if formElements[name].type=='checkbox'>
					<@rttbodytd cellName="${entityName}.${name}" value="${entity[name]?if_exists?string}" cellEdit="select" cellEditTemplate="select_template_boolean"/>
				</#if>
				<#if formElements[name].type=='select'>
					<@rttbodytd cellName="${entityName}.${name}" value="${entity[name]?if_exists?string}" cellEdit="select" cellEditTemplate="select_template_${name}"/>
				</#if>
			<#else>
				<@rttbodytd cellName="${entityName}.${name}" value="${entity[name]?if_exists?string}"/>
			</#if>
		</#if>
	</#list>	
<@rttbodytrend  rowid="${entity.id}" readonly="${readonly}"/>
</#list>
<@rtend readonly="${readonly}"/>
<#if readonly=='false'>
<div style="display: none">
<#list formElements?keys as key>
	<#if formElements[key].type=='select'>
		<textarea id="select_template_${key}">
	<select onblur="ECSideUtil.updateCell(this,'select')"
			style="width: 100%;" name="${entityName}.${key}">
			<#list formElements[key].enumValues as en>
			<option value="${en}">${en}</option>
			</#list>
	</select>
	</textarea>
	</#if>
</#list></div>
</#if>
</body>
</html>
