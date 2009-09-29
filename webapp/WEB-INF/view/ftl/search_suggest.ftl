<#list suggestions?keys! as key>
${key}|${suggestions[key]+action.getText('results')}
</#list>