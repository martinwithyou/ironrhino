<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${product.name}</title>
<#if siteBaseUrl?exists>
<#include "head.ftl">
</head>
<body>
<div id="wrapper">
<#include "product_header_loading.ftl">
</div>
<div id="message"></div>
<div id="main">
<#include "product_detail.ftl">
<#include "product_dynamic_loading.ftl">
</div>
<#include "footer.ftl">
<#else>
</head>
<body>
<#include "product_detail.ftl">
<#include "product_dynamic.ftl">
</#if>
</body>
</html>
