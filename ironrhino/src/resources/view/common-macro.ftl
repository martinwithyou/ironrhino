<#macro pagination class="" options="">
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
</#macro>