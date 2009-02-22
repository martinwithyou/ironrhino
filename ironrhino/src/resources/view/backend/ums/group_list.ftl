<#include "../../richtable-macro.ftl"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>List Groups</title>
</head>
<body>
<#assign config={"name":{},"enabled":{"cellEdit":"select,select_template_boolean"},"description":{"cellEdit":"input"},"rolesAsString":{"trimPrefix":true,"cellEdit":"input","class":"include_if_edited"}}>
<@richtable entityName="group" config=config/>
</body>
</html>
