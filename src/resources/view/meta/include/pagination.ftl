<#macro pagination dynamicAttributes...>
<#if !dynamicAttributes?is_hash_ex><#local dynamicAttributes={}></#if>
<#if resultPage.totalPage gt 1>
<div class="pagination">
<ul>
<#if resultPage.first>
<li class="disabled"><a title="${action.getText('firstpage')}">&lt;&lt;</a></li>
<li class="disabled"><a title="${action.getText('previouspage')}">&lt;</a></li>
<#else>
<li><a href="${resultPage.renderUrl(1)}" title="${action.getText('firstpage')}"<#list dynamicAttributes?keys as attr> ${attr}="${dynamicAttributes[attr]?html}"</#list>>&lt;&lt;</a></li>
<li><a href="${resultPage.renderUrl(resultPage.previousPage)}" title="${action.getText('previouspage')}"<#list dynamicAttributes?keys as attr> ${attr}="${dynamicAttributes[attr]?html}"</#list>>&lt;</a></li>
</#if>
<#if resultPage.totalPage lt 11>
<#list 1..resultPage.totalPage as index>
<li<#if index==resultPage.pageNo> class="active"</#if>><a href="${resultPage.renderUrl(index)}"<#list dynamicAttributes?keys as attr> ${attr}="${dynamicAttributes[attr]?html}"</#list>>${index}</a></li>
</#list>
<#else>
<#if resultPage.pageNo lt 6>
<#list 1..(resultPage.pageNo+2) as index>
<li<#if index==resultPage.pageNo> class="active"</#if>><a href="${resultPage.renderUrl(index)}"<#list dynamicAttributes?keys as attr> ${attr}="${dynamicAttributes[attr]?html}"</#list>>${index}</a></li>
</#list>
<li class="disabled"><a>...</a></li>
<#list (resultPage.totalPage-1)..resultPage.totalPage as index>
<li><a href="${resultPage.renderUrl(index)}"<#list dynamicAttributes?keys as attr> ${attr}="${dynamicAttributes[attr]?html}"</#list>>${index}</a></li>
</#list>
<#elseif resultPage.pageNo gt resultPage.totalPage-5>
<#list 1..2 as index>
<li><a href="${resultPage.renderUrl(index)}"<#list dynamicAttributes?keys as attr> ${attr}="${dynamicAttributes[attr]?html}"</#list>>${index}</a></li>
</#list>
<li class="disabled"><a>...</a></li>
<#list (resultPage.pageNo-2)..resultPage.totalPage as index>
<li<#if index==resultPage.pageNo> class="active"</#if>><a href="${resultPage.renderUrl(index)}"<#list dynamicAttributes?keys as attr> ${attr}="${dynamicAttributes[attr]?html}"</#list>>${index}</a></li>
</#list>        
<#else>
<#list 1..2 as index>
<li><a href="${resultPage.renderUrl(index)}"<#list dynamicAttributes?keys as attr> ${attr}="${dynamicAttributes[attr]?html}"</#list>>${index}</a></li>
</#list>
<li class="disabled"><a>...</a></li>
<#list (resultPage.pageNo-2)..(resultPage.pageNo+2) as index>
<li<#if index==resultPage.pageNo> class="active"</#if>><a href="${resultPage.renderUrl(index)}"<#list dynamicAttributes?keys as attr> ${attr}="${dynamicAttributes[attr]?html}"</#list>>${index}</a></li>
</#list>
<li class="disabled"><a>...</a></li>
<#list (resultPage.totalPage-1)..resultPage.totalPage as index>
<li><a href="${resultPage.renderUrl(index)}"<#list dynamicAttributes?keys as attr> ${attr}="${dynamicAttributes[attr]?html}"</#list>>${index}</a></li>
</#list>
</#if>
</#if>
<#if resultPage.last>
<li class="disabled"><a title="${action.getText('nextpage')}">&gt;</a><li>
<li class="disabled"><a title="${action.getText('lastpage')}">&gt;&gt;</a></li>
<#else>
<li><a href="${resultPage.renderUrl(resultPage.nextPage)}" title="${action.getText('nextpage')}"<#list dynamicAttributes?keys as attr> ${attr}="${dynamicAttributes[attr]?html}"</#list>>&gt;</a></li>
<li><a href="${resultPage.renderUrl(resultPage.totalPage)}" title="${action.getText('lastpage')}"<#list dynamicAttributes?keys as attr> ${attr}="${dynamicAttributes[attr]?html}"</#list>>&gt;&gt;</a></li>
</#if>
</ul>
</div>
</#if>
</#macro>