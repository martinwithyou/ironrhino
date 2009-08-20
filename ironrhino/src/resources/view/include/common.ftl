<#function btn id="" text="" onclick="" type="" class="" href="">
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

<#macro pagination class="" options="">
<#if resultPage.totalPage gt 1>
<div class="pagination" style="clear:both;">
<#if resultPage.first>
<span class="disabled">${action.getText('firstpage')}</span>
<span class="disabled">${action.getText('previouspage')}</span>
<#else>
<a href="${resultPage.renderUrl(1)}"<#if class!=''> class="${class}"</#if><#if options!=''> options="${options}"</#if>>${action.getText('firstpage')}</a>
<a href="${resultPage.renderUrl(resultPage.previousPage)}"<#if class!=''> class="${class}"</#if><#if options!=''> options="${options}"</#if>>${action.getText('previouspage')}</a>
</#if>
<#list 1..resultPage.totalPage as index>
<a href="${resultPage.renderUrl(index)}"<#if index==resultPage.pageNo||class!=''> class="${class}<#if index==resultPage.pageNo> selected</#if>"</#if><#if options!=''> options="${options}"</#if>>${index}</a>
</#list>
<#if resultPage.last>
<span class="disabled">${action.getText('nextpage')}</span>
<span class="disabled">${action.getText('lastpage')}</span>
<#else>
<a href="${resultPage.renderUrl(resultPage.nextPage)}"<#if class!=''> class="${class}"</#if><#if options!=''> options="${options}"</#if>>${action.getText('nextpage')}</a>
<a href="${resultPage.renderUrl(resultPage.totalPage)}"<#if class!=''> class="${class}"</#if><#if options!=''> options="${options}"</#if>>${action.getText('lastpage')}</a>
</#if>
</div>
</#if>
</#macro>

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