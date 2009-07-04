<#function btn onclick="" text="" type="button">
  <#return '<button type="'+type+'" class="btn" onclick="'+onclick+'"><span><span>'+text+'</span></span></button>'>
</#function>

<#macro button onclick="" text="" type="button">
${btn(onclick,text,type)}
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

<#macro authorize ifAllGranted="" ifAnyGranted="" ifNotGranted="">
<#if statics['org.ironrhino.common.util.AuthzUtils'].authorize(ifAllGranted,ifAnyGranted,ifNotGranted)>
<#nested>
</#if>
</#macro>

<#function authentication property>
  <#return statics['org.ironrhino.common.util.AuthzUtils'].authentication(property)>
</#function>

<#macro cache key scope="application" timeToLive=3600*24 timeToIdle=3600>
<#assign content=statics['org.ironrhino.core.cache.PageFragmentCacheContext'].get(key,scope)?if_exists>
<#if content?exists&&content?length gt 0>${content}<#else>
<#assign content><#nested/></#assign>  
${content}
${statics['org.ironrhino.core.cache.PageFragmentCacheContext'].put(key,content,scope,timeToLive,timeToIdle)}
</#if>
</#macro>