<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Product Attribute</title>
</head>
<body>
<form action="<@url value="attribute"/>" method="post" class="ajax view">
<@s.hidden name="id" />
<table class="datagrid" style="margin-left:50px;">
	<thead>
		<tr>
			<th>name</th>
			<th>value</th>
			<th></th>
		</tr>
	</thead>
	<tbody>
		<#if attributes?? && attributes.size() gt 0>
		<#assign index=0>
		<#list attributes as attr>
			<tr>
				<td><@s.textfield name="attributes[${index}].name" theme="simple" /></td>
				<td><@s.textfield name="attributes[${index}].value" theme="simple" /></td>
				<td>
				<@button class="add" text="${action.getText('add')}"/>
				<@button class="remove" text="${action.getText('remove')}"/>
				</td>
			</tr>
			<#assign index=index+1>
		</#list>
		<#else>
			<tr>
				<td><@s.textfield name="attributes[0].name" theme="simple" /></td>
				<td><@s.textfield name="attributes[0].value" theme="simple" /></td>
				<td>
				<@button class="add" text="${action.getText('add')}"/>
				<@button class="remove" text="${action.getText('remove')}"/>
				</td>
			</tr>
		</#if>
	</tbody>
	<tfoot>
		<tr>
			<td colspan="3">
			<@button type="submit" text="${action.getText('save')}"/>
			</td>
		</tr>
	</tfoot>
</table>

</form>
</body>
</html></#escape>


