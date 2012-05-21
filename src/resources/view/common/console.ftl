<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('console')}</title>
</head>
<body>
<@s.form id="form" action="console" method="post" cssClass="ajax focus">
	<span>${action.getText('expression')}:</span><@s.textfield theme="simple" id="expression" name="expression" size="50"/>
	<span>${action.getText('global')}:</span><@s.checkbox theme="simple" id="global" name="global"/>
	<@s.submit id="submit" theme="simple" value="%{getText('confirm')}" />
</@s.form>
<div id="dashboard" style="margin:10px;">
	<button type="button" class="btn" onclick="$('#expression').val($(this).text());$('#global').attr('checked',false);$('#form').submit()">compassGps.index()</button>
	<button type="button" class="btn" onclick="$('#expression').val($(this).text());$('#global').attr('checked',true);$('#form').submit()">freemarkerConfiguration.clearTemplateCache()</button>
</div>
</body>
</html></#escape>