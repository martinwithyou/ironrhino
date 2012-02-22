<#function evalTemplate template>
	<#local i=template?index_of('${')/>
	<#if i lt 0>
		<#return template>
	<#else>
		<#local str1=template?substring(0,i)/>
		<#local str2=template?substring(i+2)/>
		<#local j=str2?index_of('}')/>
		<#local expression=str2?substring(0,j)/>
		<#local str2=str2?substring(j+1)/>
		<#local template=str1+expression?eval?string+str2/>
		<#return evalTemplate(template)>
	</#if>
</#function>

<#macro authorize ifAllGranted="" ifAnyGranted="" ifNotGranted="" authorizer="" resource="">
	<#if statics['org.ironrhino.core.util.AuthzUtils'].authorize(ifAllGranted,ifAnyGranted,ifNotGranted) || (authorizer!="" &&  statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('dynamicAuthorizerManager').authorize(authorizer,statics['org.ironrhino.core.util.AuthzUtils'].getUserDetails(),resource))>
		<#nested>
	</#if>
</#macro>

<#function authentication property>
  <#return statics['org.ironrhino.core.util.AuthzUtils'].authentication(property)>
</#function>

<#macro cache key scope="application" timeToIdle="-1" timeToLive="3600">
<#assign keyExists=statics['org.ironrhino.core.cache.CacheContext'].eval(key)??>
<#assign content=statics['org.ironrhino.core.cache.CacheContext'].getPageFragment(key,scope)!>
<#if keyExists&&content??&&content?length gt 0>${content}<#else>
<#assign content><#nested/></#assign>  
${content}
${statics['org.ironrhino.core.cache.CacheContext'].putPageFragment(key,content,scope,timeToIdle,timeToLive)}
</#if>
</#macro>

<#macro captcha theme="">
<#if captchaRequired!>
	<@s.textfield label="%{getText('captcha')}" name="captcha" size="6" cssClass="required captcha" id="${base}/captcha.jpg?token=${session.id}"/>
</#if>
</#macro>

<#function btn text="" onclick="" class="" type="" id="" href="">
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
  <#return '<a'+(_id!)+(_onclick!)+(_class)+' href="'+href+'"><span><span>'+text+'</span></span></a>'>
</#if>
  <#return '<button'+(_id!)+(_type!)+(_onclick!)+(_class)+'><span><span>'+text+'</span></span></button>'>
</#function>

<#macro button text="" type="" class="" dynamicAttributes...>
<#if !dynamicAttributes?is_hash_ex><#local dynamicAttributes={}></#if>
<#if text==''>
	<#local text=(dynamicAttributes['id']!'')?replace('_', ' ')>
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
<${tag} <#list dynamicAttributes?keys as attr>${attr}="${dynamicAttributes[attr]?html}" </#list>${_type!} class="${class}"><span><span>${text}</span></span></${tag}><#t>
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