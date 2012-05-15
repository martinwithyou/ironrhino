<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('install')}</title>
</head>
<body>
<#assign columns={"id":{"width":"250px"},"vendor":{"width":"150px"},"version":{"width":"100px"},"dependence":{}}>
<#assign actionColumnButtons=r'
<button type="button" class="btn" data-action="uninstall">${action.getText("uninstall")}</button><#t>
<#if entity.rollbackable>
<button type="button" class="btn" data-action="rollback">${action.getText("rollback")}</button><#t>
</#if>
'>
<#assign bottomButtons='
<button type="button" class="btn" onclick="$(\'#install\').toggle()">${action.getText("install")}</button><#t>
'>
<@richtable entityName="install" columns=columns actionColumnButtons=actionColumnButtons actionColumnWidth="180px" bottomButtons=bottomButtons showCheckColumn=false/>
<form id="install" action="install/install" method="post" enctype="multipart/form-data" style="display:none;text-align:center;padding-top:20px;" class="inline">
<input type="file" name="file" style="width:194px;"/><@s.submit theme="simple" value="${action.getText('upload')}"/>
</form>
</body>
</html></#escape>