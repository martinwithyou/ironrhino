<#list suggestions?keys?if_exists as key>
<span class="strong">${key}</span><span>${suggestions[key]}</span>
</#list>