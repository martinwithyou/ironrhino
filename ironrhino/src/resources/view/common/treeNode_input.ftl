<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title><#if treeNode.new>${action.getText('create')}<#else>${action.getText('edit')}</#if>${action.getText('treeNode')}</title>
</head>
<body>
<@s.form action="${getUrl(actionBaseUrl+'/save')}" method="post" cssClass="ajax form-horizontal">
	<#if !treeNode.new>
		<@s.hidden name="treeNode.id" />
	</#if>
	<@s.hidden name="parentId" />
	<div class="row-fluid">
		<div class="span4"><span>${action.getText('name')}: </span><@s.textfield theme="simple" name="treeNode.name" cssClass="required" /></div>
		<div class="span4"><span>${action.getText('description')}: </span><@s.textfield theme="simple" name="treeNode.description" /></div>
		<div class="span4"><span>${action.getText('displayOrder')}: </span><@s.textfield theme="simple" name="treeNode.displayOrder" cssClass="integer"/></div>
	</div>
	<table class="datagrid table table-condensed nullable" style="margin-top:10px;">
		<thead>
			<tr>
				<td>${action.getText('name')}</td>
				<td>${action.getText('value')}</td>
				<td class="manipulate"></td>
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
				<td class="manipulate"></td>
			</tr>
			</#list>
		</tbody>
	</table>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>


