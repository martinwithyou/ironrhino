<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('pick')}${action.getText('user')}</title>
</head>
<body>
<#assign columns={}>
<#if !Parameters.columns??>
<#assign columns={"username":{},"name":{}}>
<#else>
<#list Parameters.columns?split(',') as column>
<#assign columns=columns+{column:{}}/>
</#list>
</#if>
<#assign bottomButtons=r"
<@button text='${action.getText(\'confirm\')}' class='confirm'/>
">
<@richtable entityName="user" formid="user_pick_form" action="${getUrl('/user/pick')}" columns=columns bottomButtons=bottomButtons searchable=true readonly=true showCheckbox=true columnfilterable=false resizable=false sortable=false showPageSize=false/>
</body>
</html></#escape>