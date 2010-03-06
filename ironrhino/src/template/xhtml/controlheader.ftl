<div<#if (parameters.labelposition!'')=='top'> class="top"</#if>>
<#if parameters.label??>
<label<#if parameters.id??> for="${parameters.id?html}"</#if>>${parameters.label?html}</label>
</#if>