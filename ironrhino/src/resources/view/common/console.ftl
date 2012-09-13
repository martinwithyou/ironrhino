<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('console')}</title>
</head>
<body>
<@s.form id="form" action="console" method="post" cssClass="ajax focus form-inline">
	<span>${action.getText('expression')}:<@s.textfield theme="simple" id="expression" name="expression" cssStyle="width:400px;"/></span>
	<span style="margin: 0 10px;">${action.getText('global')}:<@s.checkbox theme="simple" id="global" name="global"/></span>
	<@s.submit id="submit" theme="simple" value="%{getText('confirm')}" />
</@s.form>
<div id="dashboard">
	<button type="button" class="btn" onclick="$('#expression').val($(this).text());$('#global').attr('checked',false);$('#form').submit()">indexManager.rebuild()</button>
	<button type="button" class="btn" onclick="$('#expression').val($(this).text());$('#global').attr('checked',true);$('#form').submit()">freemarkerConfiguration.clearTemplateCache()</button>
</div>
</body>
</html></#escape>