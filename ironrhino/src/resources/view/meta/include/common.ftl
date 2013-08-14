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

<#function getUrl value secure=false includeQueryString=false>
<#if value?starts_with('/assets/')>
	<#if assetsBase??>
		<#local value=assetsBase+value>
	<#else>
		<#local value=base+value>
	</#if>
	<#return value>
<#elseif value?starts_with('/')>
	<#if request??>
		<#if !request.isSecure() && secure>
			<#local value=statics['org.ironrhino.core.util.RequestUtils'].getBaseUrl(request,true)+value>
		<#elseif request.isSecure() && !secure>
			<#local value=statics['org.ironrhino.core.util.RequestUtils'].getBaseUrl(request,false)+value>
		<#else>
			<#local value=base+value>
		</#if>
	<#else>
		<#if value?starts_with('http://') && secure>
			<#local value=value?replace('http://','https://')?replace('8080','8443')>
		<#elseif value?starts_with('https://') && !secure>
			<#local value=value?replace('https://','http://')?replace('8443','8080')>
		</#if>
	</#if>
</#if>
<#local value=response.encodeURL(value)/>
<#if includeQueryString && request.queryString?has_content && !request.queryString?matches('^_=\\d+$')>
	<#local value=value+(value?index_of('?') gt 0)?string('&','?')/>
	<#if request.queryString?index_of('&_=') gt 0>
		<#local value=value+request.queryString?substring(0,request.queryString?index_of('&_='))/>
	<#else>
		<#local value=value+request.queryString/>
	</#if>
</#if>
<#return value>
</#function>

<#macro url value secure=false includeQueryString=false>
${getUrl(value,secure,includeQueryString)}<#t>
</#macro>