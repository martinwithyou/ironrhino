<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>
<#if preview>[${action.getText('preview')}]</#if><#if page.title??><#assign title=page.title?interpret><@title/></#if></title>
</head>
<body>
<@includePage path=page.path />
</body>
</html>
