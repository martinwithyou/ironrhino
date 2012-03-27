<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title><#if treeNode.new>${action.getText('create')}<#else>${action.getText('edit')}</#if>${action.getText('treeNode')}</title>
</head>
<body>
<@s.form action="${getUrl(actionBaseUrl+'/save')}" method="post" cssClass="ajax">
	<#if !treeNode.new>
		<@s.hidden name="treeNode.id" />
	</#if>
	<@s.hidden name="parentId" />
	<@s.textfield label="%{getText('name')}" name="treeNode.name" />
	<@s.textfield label="%{getText('description')}" name="treeNode.description" />
	<@s.textfield label="%{getText('displayOrder')}" name="treeNode.displayOrder" cssClass="integer"/>
	<table border="0" class="datagrid nullable" style="width:100%;padding-top:10px;">
		<thead>
			<tr>
				<td>${action.getText('name')}</td>
				<td>${action.getText('value')}</td>
				<td><@button text="+" class="add"/></td>
			</tr>
		</thead>
		<tbody>
			<#assign size = 0>
			<#if treeNode.attributes?? && treeNode.attributes?size gt 0>
				<#assign size = treeNode.attributes?size-1>
			</#if>
			<#list 0..size as index>
			<tr>
				<td><@s.textfield theme="simple" name="treeNode.attributes[${index}].name"/></td>
				<td><@s.textfield theme="simple" name="treeNode.attributes[${index}].value"/></td>
				<td><@button text="+" class="add"/><@button text="-" class="remove"/><@button text="↑" class="moveup"/><@button text="↓" class="movedown"/></td>
			</tr>
			</#list>
		</tbody>
	</table>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>


