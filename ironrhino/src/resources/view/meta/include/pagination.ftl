<#macro pagination extra...>
<#if resultPage.totalPage gt 1>
<div class="pagination">
<#if resultPage.first>
<span class="disabled" title="${action.getText('firstpage')}">&lt;&lt;</span>
<span class="disabled" title="${action.getText('previouspage')}">&lt;</span>
<#else>
<a href="${resultPage.renderUrl(1)}" title="${action.getText('firstpage')}"<#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list>>&lt;&lt;</a>
<a href="${resultPage.renderUrl(resultPage.previousPage)}" title="${action.getText('previouspage')}"<#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list>>&lt;</a>
</#if>
<#if resultPage.totalPage lt 11>
<#list 1..resultPage.totalPage as index>
<a href="${resultPage.renderUrl(index)}"<#if index==resultPage.pageNo||class!=''> class="<#if index==resultPage.pageNo>selected </#if>${(extra['class']?html)!}"</#if><#list extra?keys as attr><#if attr!=class> ${attr}="${extra[attr]?html}"</#if></#list>>${index}</a>
</#list>
<#else>
<#if resultPage.pageNo lt 6>
<#list 1..(resultPage.pageNo+2) as index>
<a href="${resultPage.renderUrl(index)}"<#if index==resultPage.pageNo||class!=''> class="<#if index==resultPage.pageNo>selected </#if>${(extra['class']?html)!}"</#if><#list extra?keys as attr><#if attr!=class> ${attr}="${extra[attr]?html}"</#if></#list>>${index}</a>
</#list>
...
<#list (resultPage.totalPage-1)..resultPage.totalPage as index>
<a href="${resultPage.renderUrl(index)}"<#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list>>${index}</a>
</#list>
<#elseif resultPage.pageNo gt resultPage.totalPage-5>
<#list 1..2 as index>
<a href="${resultPage.renderUrl(index)}"<#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list>>${index}</a>
</#list>
...
<#list (resultPage.pageNo-2)..resultPage.totalPage as index>
<a href="${resultPage.renderUrl(index)}"<#if index==resultPage.pageNo||class!=''> class="<#if index==resultPage.pageNo>selected </#if>${(extra['class']?html)!}"</#if><#list extra?keys as attr><#if attr!=class> ${attr}="${extra[attr]?html}"</#if></#list>>${index}</a>
</#list>        
<#else>
<#list 1..2 as index>
<a href="${resultPage.renderUrl(index)}"<#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list>>${index}</a>
</#list>
...
<#list (resultPage.pageNo-2)..(resultPage.pageNo+2) as index>
<a href="${resultPage.renderUrl(index)}"<#if index==resultPage.pageNo||class!=''> class="<#if index==resultPage.pageNo>selected </#if>${(extra['class']?html)!}"</#if><#list extra?keys as attr><#if attr!=class> ${attr}="${extra[attr]?html}"</#if></#list>>${index}</a>
</#list>
...
<#list (resultPage.totalPage-1)..resultPage.totalPage as index>
<a href="${resultPage.renderUrl(index)}"<#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list>>${index}</a>
</#list>
</#if>
</#if>
<#if resultPage.last>
<span class="disabled" title="${action.getText('nextpage')}">&gt;</span>
<span class="disabled" title="${action.getText('lastpage')}">&gt;&gt;</span>
<#else>
<a href="${resultPage.renderUrl(resultPage.nextPage)}" title="${action.getText('nextpage')}"<#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list>>&gt;</a>
<a href="${resultPage.renderUrl(resultPage.totalPage)}" title="${action.getText('lastpage')}"<#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list>>&gt;&gt;</a>
</#if>
</div>
</#if>
</#macro>