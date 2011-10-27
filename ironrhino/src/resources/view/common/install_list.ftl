<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('install')}</title>
</head>
<body>
<#assign columns={"id":{"width":"250px"},"vendor":{"width":"150px"},"version":{"width":"100px"},"dependence":{}}>
<#assign actionColumnButtons=r"
<@button text='${action.getText(\'uninstall\')}' action='uninstall'/>
<#if entity.rollbackable>
<@button text='${action.getText(\'rollback\')}' action='rollback'/>
</#if>
">
<#assign bottomButtons=r"
<@button text='${action.getText(\'install\')}' onclick='$(\'#install\').toggle()'/>
">
<@richtable entityName="install" columns=columns actionColumnButtons=actionColumnButtons actionColumnWidth="180px" bottomButtons=bottomButtons showCheckbox=false/>
<form id="install" action="install/install" method="post" enctype="multipart/form-data" style="display:none;text-align:center;padding-top:20px;" class="line">
<input type="file" name="file" style="width:194px;"/><@s.submit theme="simple" value="${action.getText('upload')}"/>
</form>
</body>
</html></#escape>


