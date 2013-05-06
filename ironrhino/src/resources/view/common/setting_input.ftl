<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title><#if setting.new>${action.getText('create')}<#else>${action.getText('edit')}</#if>${action.getText('setting')}</title>
</head>
<body>
<@s.form action="${getUrl(actionBaseUrl+'/save')}" method="post" cssClass="ajax form-horizontal">
	<#if !setting.new>
		<@s.hidden name="setting.id" />
	</#if>
	<#if Parameters.brief??>
		<@s.hidden name="setting.key"/>
		<@s.textarea label="%{getText('description')}" name="setting.description" tabindex="-1" readonly=true cssStyle="width:400px;height:20px;border:none;resize:none;outline:none;"/>
	<#else>
		<@s.textfield label="%{getText('key')}" name="setting.key" cssClass="required checkavailable span4"/>
	</#if>
	<@s.textarea label="%{getText('value')}" name="setting.value" cssClass="span4" cssStyle="height:150px;"/>
	<#if !Parameters.brief??>
		<@s.textarea label="%{getText('description')}" name="setting.description" cssClass="span4" cssStyle="height:150px;" />
	</#if>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>


