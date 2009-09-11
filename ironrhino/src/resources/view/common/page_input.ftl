<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Create/Edit Page</title>
<script type="text/javascript" src="${base}/components/tinymce/jscripts/tiny_mce/jquery.tinymce.js"></script> 
<script type="text/javascript">
$(function() {
		$('#page_content').tinymce({
			script_url : '${base}/components/tinymce/jscripts/tiny_mce/tiny_mce.js',
			theme : "advanced",
			plugins : "safari,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template",
			theme_advanced_buttons1 : "save,newdocument,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,styleselect,formatselect,fontselect,fontsizeselect",
			theme_advanced_buttons2 : "cut,copy,paste,pastetext,pasteword,|,search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,cleanup,help,code,|,insertdate,inserttime,preview,|,forecolor,backcolor",
			theme_advanced_buttons3 : "tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,emotions,iespell,media,advhr,|,print,|,ltr,rtl,|,fullscreen",
			theme_advanced_buttons4 : "insertlayer,moveforward,movebackward,absolute,|,styleprops,|,cite,abbr,acronym,del,ins,attribs,|,visualchars,nonbreaking,template,pagebreak",
			theme_advanced_toolbar_location : "top",
			theme_advanced_toolbar_align : "left",
			theme_advanced_statusbar_location : "bottom",
			theme_advanced_resizing : true,
			content_css : "${base}/styles/all-min.css"
		});
});
</script>
</head>
<body>
<@s.form action="draft" method="post">
	<@s.if test="%{!page.isNew()}">
		<@s.hidden name="page.id" />
	</@s.if>
	<@s.textfield label="%{getText('path')}" name="page.path" cssClass="required" size="50"/>
	<@s.textfield label="%{getText('title')}" name="page.title" size="50"/>
	<@s.textarea id="page_content" label="%{getText('content')}" labelposition="top" name="page.content" cols="100" rows="30"/>
	<p>
	<@s.submit value="%{getText('draft')}" theme="simple"/>
	<#if draft>
	${action.getText('draftDate')}:${page.draftDate?datetime}
	<@s.submit value="%{getText('drop')}" theme="simple" onclick="$(this).closest('form').attr('action','drop')"/>
	<@button type="link" text="${action.getText('preview')}" href="${base+cmsPath+page.path}?preview=true" target="_blank"/>
	</#if>
	</p>
	<p>
	<@s.submit value="%{getText('save')}" theme="simple" onclick="$(this).closest('form').attr('action','save')"/>
	<#if page.path?exists>
	<@button type="link" text="${action.getText('view')}" href="${base+cmsPath+page.path}" target="_blank"/>
	</#if>
	</p>
</@s.form>
</body>
</html></#escape>


