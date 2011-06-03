<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title><#if dictionary.new>${action.getText('create')}<#else>${action.getText('edit')}</#if>${action.getText('dictionary')}</title>
</head>
<body>
<@s.form action="save" method="post" cssClass="ajax">
	<@s.hidden name="dictionary.id" />
	<@s.textfield label="%{getText('name')}" name="dictionary.name" />
	<div class="field">
		<label class="field" for="items">${action.getText('items')}</label>
		<div id="items">
			<table border="0" class="datagrid" style="width:90%;text-align:center;">
				<thead>
					<tr>
						<td>${action.getText('label')}</td>
						<td>${action.getText('value')}</td>
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
						<td><@s.textfield theme="simple" name="dictionary.items[${index}].label"/></td>
						<td><@s.textfield theme="simple" name="dictionary.items[${index}].value"/></td>
						<td><@button text="+" class="add"/><@button text="-" class="remove"/><@button text="↑" class="moveup"/><@button text="↓" class="movedown"/></td>
					</tr>
					</#list>
				</tbody>
			</table>
		</div>
	</div>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>


