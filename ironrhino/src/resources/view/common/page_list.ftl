<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('page')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"path":{"width":"150px","template":"<a href=\"${getUrl(cmsPath)}$"+"{value}\" target=\"_blank\">$"+"{value}</a>"},"title":{},"displayOrder":{"width":"90px"},"tag":{"template":r"<#list entity.tags as tag><a href='<@url value='/common/page?keyword=tags:${tag}'/>'>${tag}</a> </#list>"},"createDate":{"width":"150px","template":r"<#if entity.createDate??>${value?string('yyyy-MM-dd HH:mm:ss')}</#if>"},"modifyDate":{"width":"150px","template":r"<#if entity.modifyDate??>${value?string('yyyy-MM-dd HH:mm:ss')}</#if>"}}>
<#assign actionColumnButtons=r"
<@button text='${action.getText(\'edit\')}' view='input' windowoptions='{\'iframe\':true,\'width\':\'900px\'}'/>
">
<#assign bottomButtons=r"
<@button text='${action.getText(\'create\')}' view='input' windowoptions='{\'iframe\':true,\'width\':\'900px\'}'/>
<@button text='${action.getText(\'delete\')}' action='delete'/>
<@button text='${action.getText(\'reload\')}' action='reload'/>
">
<@richtable entityName="page" columns=columns actionColumnButtons=actionColumnButtons bottomButtons=bottomButtons celleditable=false searchable=true/>
</body>
</html></#escape>
