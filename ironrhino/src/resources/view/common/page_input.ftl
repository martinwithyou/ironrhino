<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Create/Edit Page</title>
</head>
<body>

<@s.form action="save" method="post" cssClass="ajax view keepopen">
	<@s.if test="%{!page.isNew()}">
		<@s.hidden name="page.id" />
	</@s.if>
	<@s.textfield label="%{getText('path')}" name="page.path" cssClass="required"/>
	<@s.textfield label="%{getText('title')}" name="page.title" />
	<@s.textarea label="%{getText('content')}" name="page.content" cols="100" rows="30"/>
	<p>
	<@s.submit value="%{getText('save')}" theme="simple"/>
	<@button type="link" text="${action.getText('view')}" href="${base+cmsPath+page.path}" target="_blank"/>
	<@s.submit value="%{getText('draft')}" theme="simple" onclick="$(this).closest('form').attr('action',$(this).closest('form').attr('action').replace('save','draft'))"/>
	<#if draft>
	${action.getText('draftDate')}:${page.draftDate?datetime}
	<@s.submit value="%{getText('drop')}" theme="simple" onclick="$(this).closest('form').attr('action',$(this).closest('form').attr('action').replace('save','drop'))"/>
	<@button type="link" text="${action.getText('preview')}" href="${base+cmsPath+page.path}?preview=true" target="_blank"/>
	</#if>
	</p>
</@s.form>
</body>
</html></#escape>


