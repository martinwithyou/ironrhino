<#list suggestions?keys?if_exists as key>
${key}|${suggestions[key]+action.getText('results')}
</#list>