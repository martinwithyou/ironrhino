<div class="field<#if (parameters.labelposition!)=='top'> top</#if>">
<#if parameters.label??>
<label class="field"<#if parameters.id??> for="${parameters.id?html}"</#if>>${parameters.label?html}</label>
</#if>