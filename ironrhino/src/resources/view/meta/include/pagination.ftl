<#macro pagination extra...>
<#if resultPage.totalPage gt 1>
<div class="pagination" style="clear:both;">
<#if resultPage.first>
<span class="disabled">${action.getText('firstpage')}</span>
<span class="disabled">${action.getText('previouspage')}</span>
<#else>
<a href="${resultPage.renderUrl(1)}"<#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list>>${action.getText('firstpage')}</a>
<a href="${resultPage.renderUrl(resultPage.previousPage)}"<#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list>>${action.getText('previouspage')}</a>
</#if>
<#if resultPage.totalPage lt 11>
<#list 1..resultPage.totalPage as index>
<a href="${resultPage.renderUrl(index)}"<#if index==resultPage.pageNo||class!=''> class="<#if index==resultPage.pageNo>selected </#if>${extra['class']?if_exists?html}"</#if><#list extra?keys as attr><#if attr!=class> ${attr}="${extra[attr]?html}"</#if></#list>>${index}</a>
</#list>
<#else>
<#if resultPage.pageNo lt 6>
<#list 1..(resultPage.pageNo+2) as index>
<a href="${resultPage.renderUrl(index)}"<#if index==resultPage.pageNo||class!=''> class="<#if index==resultPage.pageNo>selected </#if>${extra['class']?if_exists?html}"</#if><#list extra?keys as attr><#if attr!=class> ${attr}="${extra[attr]?html}"</#if></#list>>${index}</a>
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
<a href="${resultPage.renderUrl(index)}"<#if index==resultPage.pageNo||class!=''> class="<#if index==resultPage.pageNo>selected </#if>${extra['class']?if_exists?html}"</#if><#list extra?keys as attr><#if attr!=class> ${attr}="${extra[attr]?html}"</#if></#list>>${index}</a>
</#list>        
<#else>
<#list 1..2 as index>
<a href="${resultPage.renderUrl(index)}"<#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list>>${index}</a>
</#list>
...
<#list (resultPage.pageNo-2)..(resultPage.pageNo+2) as index>
<a href="${resultPage.renderUrl(index)}"<#if index==resultPage.pageNo||class!=''> class="<#if index==resultPage.pageNo>selected </#if>${extra['class']?if_exists?html}"</#if><#list extra?keys as attr><#if attr!=class> ${attr}="${extra[attr]?html}"</#if></#list>>${index}</a>
</#list>
...
<#list (resultPage.totalPage-1)..resultPage.totalPage as index>
<a href="${resultPage.renderUrl(index)}"<#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list>>${index}</a>
</#list>
</#if>
</#if>
<#if resultPage.last>
<span class="disabled">${action.getText('nextpage')}</span>
<span class="disabled">${action.getText('lastpage')}</span>
<#else>
<a href="${resultPage.renderUrl(resultPage.nextPage)}"<#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list>>${action.getText('nextpage')}</a>
<a href="${resultPage.renderUrl(resultPage.totalPage)}"<#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list>>${action.getText('lastpage')}</a>
</#if>
</div>
</#if>
</#macro>