<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title><#if dictionary.new>${action.getText('create')}<#else>${action.getText('edit')}</#if>${action.getText('dictionary')}</title>
</head>
<body>
<@s.form action="${getUrl('/common/dictionary/save')}" method="post" cssClass="ajax" cssStyle="text-align:center;">
	<@s.hidden name="dictionary.id" />
	<div style="float:left;width:40%;"><span>${action.getText('name')}: </pan><@s.textfield theme="simple" label="%{getText('name')}" name="dictionary.name" /></div>
	<div style="float:left;width:40%;"><span>${action.getText('description')}: </pan><@s.textfield theme="simple" label="%{getText('description')}" name="dictionary.description" /></div>
	<table border="0" class="datagrid" style="width:100%;padding-top:10px;">
		<thead>
			<tr>
				<td>${action.getText('value')}</td>
				<td>${action.getText('label')}</td>
				<td></td>
			</tr>
		</thead>
		<tbody>
			<#assign size = 0>
			<#if dictionary.items?? && dictionary.items?size gt 0>
				<#assign size = dictionary.items?size-1>
			</#if>
			<#list 0..size as index>
			<tr>
				<td><@s.textfield theme="simple" name="dictionary.items[${index}].value"/></td>
				<td><@s.textfield theme="simple" name="dictionary.items[${index}].label"/></td>
				<td><@button text="+" class="add"/><@button text="-" class="remove"/><@button text="↑" class="moveup"/><@button text="↓" class="movedown"/></td>
			</tr>
			</#list>
		</tbody>
	</table>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>


