<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('page')}${action.getText('list')}</title>
</head>
<body>
<#assign config={"path":{"template":"<a href=\"${getUrl(cmsPath)}$"+"{value}\" target=\"_blank\">$"+"{value}</a>"},"title":{},"createDate":{},"modifyDate":{}}>
<#assign actionColumnButtons=r"
<@button text='${action.getText(\'edit\')}' view='input' windowoptions='{\'iframe\':true,\'width\':\'900px\'}'/>
<@button text='${action.getText(\'delete\')}' action='delete'/>
">
<#assign bottomButtons=r"
<@button text='${action.getText(\'create\')}' view='input' windowoptions='{\'iframe\':true,\'width\':\'900px\'}'/>
<@button text='${action.getText(\'delete\')}' action='delete'/>
<@button text='${action.getText(\'reload\')}' action='reload'/>
">
<@richtable entityName="page" config=config actionColumnWidth="100px" actionColumnButtons=actionColumnButtons bottomButtons=bottomButtons celleditable=false/>
</body>
</html></#escape>
