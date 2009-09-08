<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>List Pages</title>
</head>
<body>
<#assign config={"path":{},"title":{},"createDate":{},"modifyDate":{}}>
<@richtable entityName="page" config=config/>
</body>
</html></#escape>
