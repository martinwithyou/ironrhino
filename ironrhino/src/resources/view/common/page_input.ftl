<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Create/Edit Page</title>
<meta name="cms_path" content="${cmsPath}" />
<script type="text/javascript" src="${base}/assets/components/tinymce/jscripts/tiny_mce/jquery.tinymce.js"></script> 
<script type="text/javascript">
$(function() {
		var cmsPath= $('meta[name="cms_path"]').attr('content') || '';
		var options = {
			script_url : '${base}/assets/components/tinymce/jscripts/tiny_mce/tiny_mce.js',
			theme : "advanced",
			plugins : "safari,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,autosave",
			theme_advanced_buttons1 : "save,newdocument,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,styleselect,formatselect,fontselect,fontsizeselect",
			theme_advanced_buttons2 : "cut,copy,paste,pastetext,pasteword,|,search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,cleanup,help,code,|,insertdate,inserttime,preview,|,forecolor,backcolor",
			theme_advanced_buttons3 : "tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,emotions,iespell,media,advhr,|,print,|,ltr,rtl,|,fullscreen",
			theme_advanced_buttons4 : "insertlayer,moveforward,movebackward,absolute,|,styleprops,|,cite,abbr,acronym,del,ins,attribs,|,visualchars,nonbreaking,template,pagebreak",
			theme_advanced_toolbar_location : "top",
			theme_advanced_toolbar_align : "left",
			theme_advanced_statusbar_location : "bottom",
			theme_advanced_resizing : true,
			content_css : "${base}/assets/styles/all-min.css",
			mode : "textareas"
		};
		$('#page_content').tinymce(options);
		setTimeout(function(){
		var ed = tinymce.EditorManager.get('page_content');
		$('#draft').click(function(){
			if(!ed.isDirty()) return false;
			$('#form').attr('action','draft');
			ed.save();
		});
		$('#save').click(function(){
			$('#form').attr('action','save');
			ed.save();
		});
		ed.onKeyUp.add(function(ed, e) {
          ed.isNotDirty = 0;
      	});
		var form = $('#form');
		form[0].onsuccess = function(){				
		ed.isNotDirty = 1;
		var page = Ajax.jsonResult.page;
		if(page){
		var date = page.draftDate;
		var path = page.path;
		$('#page_id').val(page.id);
		$('#page_path').val(path);
		path = CONTEXT_PATH+cmsPath+path;
		if(date){
			//save draft 
			$('.draft').show();
			$('.draftDate').text(date);
			$('#preview').attr('href',path+'?preview=true');
		}else{
			//save
			$('.draft').hide();
			$('#view').attr('href',path);
		}
		}else{
			document.location.href=document.location.href;
		}
		};
		setInterval(function(){
			if(ed.isDirty()){
				$('#form').attr('action','draft');
				ed.save();
				form.submit();
				}
		},60000)},3000);

		$('#drop').click(
		function(){
		var form = $('#form');
		form.attr('action','drop');
		$('#page_content').text('');
		form[0].onsuccess=function(){
		document.location.href = document.location.href;
		};
		}
		);
});

</script>
</head>
<body>
<@s.form id="form" action="draft" method="post" cssClass="ajax">
	<@s.hidden id="page_id" name="page.id" />
	<@s.textfield id="page_path" label="%{getText('path')}" name="page.path" cssClass="required" size="50"/>
	<@s.textfield label="%{getText('title')}" name="page.title" size="50"/>
	<@s.textarea id="page_content" label="%{getText('content')}" labelposition="top" name="page.content" cols="100" rows="30"/>
	<p>
	<@s.submit id="draft" value="%{getText('draft')}" theme="simple"/>
	<span class="draft" <#if !draft>style="display: none;"</#if>>
	${action.getText('draftDate')}:<span class="draftDate"><#if page.draftDate??>${page.draftDate?datetime}</#if></span>
	<#if page.path??>
	<@button id="preview" type="link" text="${action.getText('preview')}" href="${base+cmsPath+page.path}?preview=true" target="_blank"/>
	<#else>
	<@button id="preview" type="link" text="${action.getText('preview')}" target="_blank"/>
	</#if>
	<@s.submit id="drop" value="%{getText('drop')}" theme="simple"/>
	</span>
	</p>
	<p>
	<@s.submit id="save" value="%{getText('save')}" theme="simple"/>
	<#if page.path??>
	<@button id="view" type="link" text="${action.getText('view')}" href="${base+cmsPath+page.path}" target="_blank"/>
	<#else>
	<@button id="view" type="link" text="${action.getText('view')}" target="_blank"/>
	</#if>
	</p>
</@s.form>
</body>
</html></#escape>


