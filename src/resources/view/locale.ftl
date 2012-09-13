<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('locale')}</title>
</head>
<body>
<form action="<@url value="/locale"/>">
<@s.select theme="simple" name="lang" onchange="this.form.submit()" list="availableLocales" listKey="top" listValue="top.getDisplayLanguage(top)" headerKey="" headerValue=""/>	
</form>
</body>
</html></#escape>


