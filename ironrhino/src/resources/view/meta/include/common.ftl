<#macro authorize ifAllGranted="" ifAnyGranted="" ifNotGranted="" authorizer="" resource="">
	<#if statics['org.ironrhino.core.util.AuthzUtils'].authorize(ifAllGranted,ifAnyGranted,ifNotGranted) || (authorizer!="" &&  statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('dynamicAuthorizerManager').authorize(authorizer,statics['org.ironrhino.core.util.AuthzUtils'].getUserDetails(),resource))>
		<#nested>
	</#if>
</#macro>

<#function authentication property>
  <#return statics['org.ironrhino.core.util.AuthzUtils'].authentication(property)>
</#function>

<#macro cache key scope="application" timeToIdle="-1" timeToLive="3600">
<#local keyExists=statics['org.ironrhino.core.cache.CacheContext'].eval(key)??>
<#local content=statics['org.ironrhino.core.cache.CacheContext'].getPageFragment(key,scope)!>
<#if keyExists&&content??&&content?length gt 0>${content}<#else>
<#local content><#nested/></#local>  
${content}
${statics['org.ironrhino.core.cache.CacheContext'].putPageFragment(key,content,scope,timeToIdle,timeToLive)}
</#if>
</#macro>

<#macro captcha theme="">
<#if captchaRequired!>
	<@s.textfield label="%{getText('captcha')}" name="captcha" cssClass="required captcha input-small" id="${base}/captcha.jpg?token=${session.id}"/>
</#if>
</#macro>

<#function getUrl value secure=''>
<#if value?starts_with('/assets/')>
	<#if assetsBase??>
		<#local value=assetsBase+value>
	<#else>
		<#local value=base+value>
	</#if>
	<#return value>
<#elseif value?starts_with('/')>
	<#if request??>
		<#if !request.isSecure() && secure=='true'>
			<#local value=statics['org.ironrhino.core.util.RequestUtils'].getBaseUrl(request,true)+value>
		<#elseif request.isSecure() && secure=='false'>
			<#local value=statics['org.ironrhino.core.util.RequestUtils'].getBaseUrl(request,false)+value>
		<#else>
			<#local value=base+value>
		</#if>
	<#else>
		<#if value?starts_with('http://') && secure=='true'>
			<#local value=value?replace('http://','https://')?replace('8080','8443')>
		<#elseif value?starts_with('https://') && secure=='false'>
			<#local value=value?replace('https://','http://')?replace('8443','8080')>
		</#if>
	</#if>
</#if>
<#return response.encodeURL(value)>
</#function>

<#macro url value secure=''>
${getUrl(value,secure)}<#t>
</#macro>