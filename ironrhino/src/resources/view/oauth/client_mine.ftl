<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('client')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"name":{},"id":{},"secret":{},"enabled":{}}>
<#assign actionColumnButtons='<button type="button" class="btn" data-view="show">${action.getText("show")}</button><button type="button" class="btn" data-action="disable">${action.getText("disable")}</button>'>
<#assign bottomButtons='<button type="button" class="btn" data-action="reload">${action.getText("reload")}</button>'>
<@richtable action="mine" entityName="client" columns=columns actionColumnButtons=actionColumnButtons bottomButtons=bottomButtons celleditable=false/>
</body>
</html></#escape>