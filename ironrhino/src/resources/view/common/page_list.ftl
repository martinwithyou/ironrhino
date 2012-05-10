<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('page')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"path":{"width":"150px","template":"<a href=\"${getUrl(cmsPath)}$"+"{value}\" target=\"_blank\">$"+"{value}</a>"},"title":{},"displayOrder":{"width":"90px"},"tag":{"template":r"<#list entity.tags as tag><a href='<@url value='/common/page?keyword=tags:${tag}'/>'>${tag}</a>&nbsp;&nbsp;</#list>"},"createDate":{"width":"150px","template":r"<#if entity.createDate??>${value?string('yyyy-MM-dd HH:mm:ss')}</#if>"},"modifyDate":{"width":"150px","template":r"<#if entity.modifyDate??>${value?string('yyyy-MM-dd HH:mm:ss')}</#if>"}}>
<#assign actionColumnButtons='
<button type="button" class="btn" data-view="input" data-windowoptions="{\'iframe\':true,\'width\':\'900px\'}">${action.getText("edit")}</button><#t>
'>
<#assign bottomButtons='
<button type="button" class="btn" data-view="input" data-windowoptions="{\'iframe\':true,\'width\':\'900px\'}">${action.getText("create")}</button><#t>
<button type="button" class="btn" data-action="delete">${action.getText("delete")}</button><#t>
<button type="button" class="btn" data-action="reload">${action.getText("reload")}</button><#t>
'>
<@richtable entityName="page" columns=columns actionColumnButtons=actionColumnButtons bottomButtons=bottomButtons celleditable=false searchable=true/>
</body>
</html></#escape>
