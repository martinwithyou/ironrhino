<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Create/Edit Category</title>

</head>
<body>
<@s.form action="save" method="post" cssClass="ajax">
	<@s.hidden name="parentId" />
	<@s.hidden name="category.id" />
	<@s.textfield label="%{getText('code')}" name="category.code" />
	<@s.textfield label="%{getText('name')}" name="category.name" />
	<@s.textfield label="%{getText('description')}"	name="category.description" />
	<@s.textfield label="%{getText('displayOrder')}" name="category.displayOrder" cssClass="integer" />
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>


