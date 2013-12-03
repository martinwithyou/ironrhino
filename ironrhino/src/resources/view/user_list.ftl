<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('user')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"username":{},"name":{},"email":{},"enabled":{"width":"80px"}}>
<@richtable entityName="user" columns=columns searchable=true celleditable=false enableable=true filterable=true bottomButtons=bottomButtons/>
</body>
</html></#escape>