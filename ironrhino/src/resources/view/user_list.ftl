<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('user')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"username":{},"name":{},"password":{"value":"********","trimPrefix":true,"cellEdit":"click,rt_edit_template_password","class":"include_if_edited"}}>
<@richtable entityName="user" columns=columns searchable=true/>
<div style="display: none;">
<textarea id="rt_edit_template_password">
	<input type="password" class="text" value=""
	onblur="Richtable.updatePasswordCell(this)" style="width: 100%;" name="" />
</textarea></div>
</body>
</html></#escape>