<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>List Pages</title>
</head>
<body>
<#assign config={"path":{"template":"<a href=\"${base+cmsPath}$"+"{value}\" target=\"_blank\">$"+"{value}</a>"},"title":{},"createDate":{},"modifyDate":{}}>
<#assign actionColumnButtons=btn(action.getText('edit'),r"window.open(Richtable.getUrl('input','${rowid}'))")+btn(action.getText('delete'),r"Richtable.del('${rowid}')")>
<@richtable entityName="page" config=config actionColumnWidth="100px" actionColumnButtons=actionColumnButtons celleditable=false/>
</body>
</html></#escape>
