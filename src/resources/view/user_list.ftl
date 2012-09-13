<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('user')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"username":{},"name":{},"email":{}}>
<@richtable entityName="user" columns=columns searchable=true celleditable=false/>
</body>
</html></#escape>