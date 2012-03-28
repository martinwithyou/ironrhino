<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title><#if page.new>${action.getText('create')}<#else>${action.getText('edit')}</#if>${action.getText('page')}</title>
<meta name="cms_path" content="${cmsPath}" />
<script type="text/javascript" src="<@url value="/assets/components/tiny_mce/jquery.tinymce.js"/>"></script>
<script type="text/javascript">
$(function() {
		window.pageid = function(){		//for tinymce browseimage.js
			return $('#page_id').val();
		}
		$('#display_page_head').click(function(){$('#page_head').toggle()});

		var cmsPath= $('meta[name="cms_path"]').attr('content') || '';
		var options = {
			language : MessageBundle.lang(),
			script_url : '<@url value="/assets/components/tiny_mce/tiny_mce.js"/>',
			theme : "advanced",
			plugins : "safari,pagebreak,layer,table,advimage,advlink,emotions,inlinepopups,preview,media,searchreplace,print,contextmenu,paste,fullscreen,noneditable,visualchars,xhtmlxtras,autosave",
			theme_advanced_buttons1 : "cut,copy,paste,pastetext,pasteword,|,search,replace,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,formatselect,fontselect,fontsizeselect,|,fullscreen,preview,code",
			theme_advanced_buttons2 : "blockquote,|,undo,redo,|,link,unlink,anchor,image,media,cleanup,|,forecolor,backcolor,tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,emotions,|,print",
			theme_advanced_buttons3 : "",
        	theme_advanced_buttons4 : "",
			theme_advanced_toolbar_location : "top",
			theme_advanced_toolbar_align : "left",
			theme_advanced_statusbar_location : "bottom",
			theme_advanced_resizing : true,
			remove_script_host : false,
        	convert_urls : false,
			mode : "textareas"
		};
		$('#page_content').tinymce(options);
		setTimeout(function(){
		var ed = tinymce.EditorManager.get('page_content');
		$('#draft').click(function(){
			if(!$('#form').attr('dirty')&&!ed.isDirty()) return false;
			var action = $('#form').attr('action');
			$('#form').attr('action',action.substring(0,action.lastIndexOf('/')+1)+'draft');
			ed.save();
		});
		$('#save').click(function(){
			var action = $('#form').attr('action');
			$('#form').attr('action',action.substring(0,action.lastIndexOf('/')+1)+'save');
			ed.save();
		});
		ed.onKeyUp.add(function(ed, e) {
          ed.isNotDirty = 0;
      	});
		var form = $('#form');
		form[0].onsuccess = function(){	
		$('#form').removeAttr('dirty')
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
			if(($('#form').attr('dirty')||ed.isDirty())&&$('#page_path').val()){
				var action = $('#form').attr('action');
				$('#form').attr('action',action.substring(0,action.lastIndexOf('/')+1)+'draft');
				ed.save();
				form.submit();
				}
		},60000);
		},1500);
		$('input[name="page.path"],input[name="page.title"]').keyup(function(){
			$('#form').attr('dirty',true);
		});
		$('#drop').click(function(){
			var form = $('#form');
			var action = $('#form').attr('action');
			$('#form').attr('action',action.substring(0,action.lastIndexOf('/')+1)+'drop');
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
<@s.form id="form" action="${getUrl(actionBaseUrl+'/draft')}" method="post" cssClass="ajax" cssStyle="padding-top:13px;">
	<@s.hidden id="page_id" name="page.id" />
	<#if Parameters.brief??>
		<@s.hidden name="page.path"/>
		<@s.hidden name="page.displayOrder"/>
		<@s.hidden name="page.tagsAsString"/>
	<#else>
		<@s.textfield id="page_path" label="%{getText('path')}" name="page.path" cssClass="required" size="50"/>
		<@s.textfield label="%{getText('displayOrder')}" name="page.displayOrder" cssClass="integer"/>
		<@s.textfield label="%{getText('tag')}" name="page.tagsAsString" size="50" cssClass="tags" source="${getUrl('/common/page/suggest')}"/>
	</#if>
	<@s.textfield label="%{getText('title')}" name="page.title" size="50"/>
	<@s.textarea id="page_content" label="%{getText('content')}" labelposition="top" name="page.content" cols="50" rows="16"/>
	<#if Parameters.brief??>
		<@s.hidden name="page.head"/>
	<#else>
		<@s.textarea id="page_head" name="page.head" cols="100" rows="3" cssStyle="display:none;"/>
		<div class="field">
			<@button id="display_page_head" text="${action.getText('edit')}${action.getText('head')}"/>
		</div>
	</#if>
	<div class="field">
	<@s.submit id="draft" value="%{getText('draft')}" theme="simple"/>
	<span class="draft" <#if !draft>style="display: none;"</#if>>
	${action.getText('draftDate')}:<span class="draftDate"><#if page.draftDate??>${page.draftDate?datetime}</#if></span>
	<#if page.id??>
	<@button id="preview" type="link" text="${action.getText('preview')}" href="${getUrl(cmsPath+page.path)}?preview=true" target="_blank"/>
	<#else>
	<@button id="preview" type="link" text="${action.getText('preview')}" target="_blank"/>
	</#if>
	<@s.submit id="drop" value="%{getText('drop')}" theme="simple"/>
	</span>
	</div>
	<div class="field">
	<@s.submit id="save" value="%{getText('save')}" theme="simple"/>
	<#if page.id??>
	<@button id="view" type="link" text="${action.getText('view')}" href="${getUrl(cmsPath+page.path)}" target="_blank"/>
	<#else>
	<@button id="view" type="link" text="${action.getText('view')}" target="_blank"/>
	</#if>
	</div>
</@s.form>
</body>
</html></#escape>


