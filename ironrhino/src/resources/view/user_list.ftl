<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('user')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"username":{},"name":{},"email":{},"enabled":{"width":"80px"}}>
<#assign bottomButtons='
<button type="button" class="btn" data-view="input">${action.getText("create")}</button>
<button type="button" class="btn" data-action="delete" data-shown="selected" data-filterselector="[data-enabled=\'false\']">${action.getText("delete")}</button>
<button type="button" class="btn reload">${action.getText("reload")}</button>
'>
<@richtable entityName="user" columns=columns searchable=true celleditable=false bottomButtons=bottomButtons/>
</body>
</html></#escape>