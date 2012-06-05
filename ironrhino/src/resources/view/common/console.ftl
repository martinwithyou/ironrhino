<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('console')}</title>
</head>
<body>
<@s.form id="form" action="console" method="post" cssClass="ajax focus">
	<span>${action.getText('expression')}:</span><@s.textfield theme="simple" id="expression" name="expression" cssStyle="width:400px;"/>
	<span>${action.getText('global')}:</span><@s.checkbox theme="simple" id="global" name="global"/>
	<@s.submit id="submit" theme="simple" value="%{getText('confirm')}" />
</@s.form>
<div id="dashboard" style="margin:10px;">
	<button type="button" class="btn" onclick="$('#expression').val($(this).text());$('#global').attr('checked',false);$('#form').submit()">compassGps.index()</button>
	<button type="button" class="btn" onclick="$('#expression').val($(this).text());$('#global').attr('checked',true);$('#form').submit()">freemarkerConfiguration.clearTemplateCache()</button>
</div>
</body>
</html></#escape>