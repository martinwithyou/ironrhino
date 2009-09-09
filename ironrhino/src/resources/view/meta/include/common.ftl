<#macro authorize ifAllGranted="" ifAnyGranted="" ifNotGranted="" expression="">
<#if statics['org.ironrhino.common.util.AuthzUtils'].authorize(ifAllGranted,ifAnyGranted,ifNotGranted,expression)>
<#nested>
</#if>
</#macro>

<#function authentication property>
  <#return statics['org.ironrhino.common.util.AuthzUtils'].authentication(property)>
</#function>

<#macro cache key scope="application" timeToLive="900" timeToIdle="900">
<#assign keyExists=statics['org.ironrhino.core.cache.CacheContext'].eval(key)?exists>
<#assign content=statics['org.ironrhino.core.cache.CacheContext'].getPageFragment(key,scope)?if_exists>
<#if keyExists&&content?exists&&content?length gt 0>${content}<#else>
<#assign content><#nested/></#assign>  
${content}
${statics['org.ironrhino.core.cache.CacheContext'].putPageFragment(key,content,scope,timeToLive,timeToIdle)}
</#if>
</#macro>

<#macro includePage path>
<#local pageManager=statics['org.ironrhino.common.util.ApplicationContextUtils'].getBean('pageManager')>
<#if Parameters.preview?if_exists=='true'>
<#local page=pageManager.getDraftByPath(path)?if_exists>
<#else>
<#local page=pageManager.getByPath(path)?if_exists>
</#if>
<#if page?exists>
<#local content=page.content?interpret>
<@content/>
</#if>
</#macro>

<#macro captcha theme="">
<#if captchaRequired?if_exists>
	<@s.textfield label="%{getText('captcha')}" name="captcha" size="6" cssClass="autocomplete_off required captcha"/>
</#if>
</#macro>

<#function btn text="" onclick="" type="" id="" class="" href="">
	<#if id!=''>
	<#local _id=' id="'+id+'"'>
	<#if text==''>
		<#local text=id?replace('_', ' ')>
	</#if>
	</#if>
	<#if type!=''>
	<#local _type=' type="'+type+'"'>
	<#else>
	<#local _type=' type="button"'>
	</#if>
	<#if onclick!=''>
	<#local _onclick=' onclick="'+onclick+'"'>
	</#if>
	<#if class!=''>
	<#local _class=' class="btn '+class+'"'>
	<#else>
	<#local _class=' class="btn"'>
	</#if>
<#if type=='link'>
  <#return '<a'+_id?if_exists+_onclick?if_exists+_class+' href="'+href+'"><span><span>'+text+'</span></span></a>'>
</#if>
  <#return '<button'+_id?if_exists+_type?if_exists+_onclick?if_exists+_class+'><span><span>'+text+'</span></span></button>'>
</#function>

<#macro button text="" type="" class="" extra...>
<#if text==''>
	<#local text=extra['id']?default('')?replace('_', ' ')>
</#if>
<#if class!=''>
	<#local class='btn '+class>
<#else>
	<#local class='btn'>
</#if>
<#local tag='button'>
<#if type=='link'>
<#local tag='a'>
<#else>
<#if type==''>
<#local _type=' type="button"'>
<#else>
<#local _type=' type="'+type+'"'>
</#if>
</#if>
<${tag} <#list extra?keys as attr>${attr}="${extra[attr]?html}" </#list>${_type?if_exists} class="${class}"><span><span>${text}</span></span></${tag}>
</#macro>
