<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>List Users</title>
</head>
<body>
<#assign config={"username":{},"name":{},"email":{},"password":{"value":"********","trimPrefix":true,"cellEdit":"click,ec_edit_template_password","class":"include_if_edited"},"rolesAsString":{"trimPrefix":true,"cellEdit":"click","class":"include_if_edited"}}>
<@richtable entityName="user" config=config/>
<div style="display: none;">
<textarea id="ec_edit_template_password">
	<input type="password" class="inputtext" value=""
	onblur="Richtable.updatePasswordCell(this)" style="width: 100%;" name="" />
</textarea></div>
</body>
</html></#escape>
