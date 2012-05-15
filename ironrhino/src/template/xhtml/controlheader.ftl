<div class="control-group<#if (parameters.labelposition!)=='top'> top</#if>">
<#if parameters.label??>
<label class="control-label"<#if parameters.id??> for="${parameters.id?html}"</#if>>${parameters.label?html}</label>
</#if>
<div class="controls">