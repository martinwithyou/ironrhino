<p<#if parameters.labelposition?default('')=='top'> class="top"</#if>>
<#if parameters.label?exists>
<label<#if parameters.id?exists> for="${parameters.id?html}"</#if>>${parameters.label?html}</label>
</#if>