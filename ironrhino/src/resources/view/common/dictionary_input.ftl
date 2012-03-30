<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title><#if dictionary.new>${action.getText('create')}<#else>${action.getText('edit')}</#if>${action.getText('dictionary')}</title>
</head>
<body>
<@s.form action="${getUrl(actionBaseUrl+'/save')}" method="post" cssClass="ajax" cssStyle="text-align:center;">
	<#if !dictionary.new>
		<@s.hidden name="dictionary.id" />
	</#if>
	<#if Parameters.brief??>
		<@s.hidden name="dictionary.name"/>
		<@s.hidden name="dictionary.description" />
	<#else>
		<div style="float:left;width:40%;"><span>${action.getText('name')}: </span><@s.textfield theme="simple" name="dictionary.name" cssClass="required checkavailable"/></div>
		<div style="float:left;width:40%;"><span>${action.getText('description')}: </span><@s.textfield theme="simple" name="dictionary.description" /></div>
	</#if>
	<table border="0" class="datagrid highlightrow" style="width:100%;padding-top:10px;">
		<style scoped>
		tr.option{
			background-color:#F5F5F5;
		}
		tr.group{
			background-color:#E5E5E5;
		}
		</style>
		<thead>
			<tr>
				<td>${action.getText('label')}</td>
				<td>${action.getText('value')}</td>
				<td>${action.getText('type')}</td>
				<td class="manipulate"><@button text="+" class="add"/></td>
			</tr>
		</thead>
		<tbody>
			<#assign size = 0>
			<#if dictionary.items?? && dictionary.items?size gt 0>
				<#assign size = dictionary.items?size-1>
			</#if>
			<#list 0..size as index>
			<tr class="linkage">
				<td><@s.textfield theme="simple" name="dictionary.items[${index}].label"/></td>
				<td><@s.textfield theme="simple" name="dictionary.items[${index}].value" cssClass="required showonadd linkage_component option"/></td>
				<td><select class="linkage_switch" style="width:80px;">
						<option value="option">${action.getText('option')}</option>
						<option value="group"<#if dictionary.items[index]?? && dictionary.items[index].value?? && !dictionary.items[index].value?has_content>selected="selected"</#if>>${action.getText('group')}</option>
					</select></td>
				<td class="manipulate"><@button text="+" class="add"/><@button text="-" class="remove"/><@button text="↑" class="moveup"/><@button text="↓" class="movedown"/></td>
			</tr>
			</#list>
		</tbody>
	</table>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>


