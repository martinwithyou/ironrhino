<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title><#if setting.new>${action.getText('create')}<#else>${action.getText('edit')}</#if>${action.getText('setting')}</title>

</head>
<body>
<@s.form action="save" method="post" cssClass="ajax">
	<@s.hidden name="setting.id" />
	<@s.textfield label="%{getText('key')}" name="setting.key" />
	<@s.textarea label="%{getText('value')}" name="setting.value" style="width:50%;height:100px;"/>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>


