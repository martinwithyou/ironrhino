<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title><#if page.new>${action.getText('create')}<#else>${action.getText('edit')}</#if>${action.getText('page')}</title>
<meta name="cms_path" content="${cmsPath}" />
<#if Parameters.decorator??>
<style>
body {
	background:#FFFFFF;
}
#content {
	padding: 10px;
	margin-bottom: 10px;
	background:#fff;
	border-radius: 0;
	-webkit-border-radius:  0;
	-moz-border-radius:  0 ;
	box-shadow: none;
	-webkit-box-shadow: none;
	-moz-box-shadow: none;
}
</#if>
</style>
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
			content_css : '<#if Parameters.content_css?has_content>${Parameters.content_css}<#else><@url value="/assets/styles/ironrhino-min.css"/></#if>',
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
		if($('#form').attr('action').indexOf('save')>0 && window.parent!=window){
				$('.ui-dialog-titlebar-close',$('#_window_ ',window.parent.document).closest('.ui-dialog')).click();
				return;
			}
		$('#form').removeAttr('dirty')
		ed.isNotDirty = 1;
		var page = Ajax.jsonResult.page;
		if(page){
			var date = page.draftDate;
			var path = page.pagepath;
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
			if(($('#form').attr('dirty')||ed.isDirty())&&$('#page_path').val()&&$('#page_content').val()){
				var action = $('#form').attr('action');
				$('#form').attr('action',action.substring(0,action.lastIndexOf('/')+1)+'draft');
				ed.save();
				form.submit();
				}
		},60000);
		},1500);
		$('input[name="page.pagepath"],input[name="page.title"]').keyup(function(){
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
<@s.form id="form" action="${getUrl(actionBaseUrl+'/draft')}" method="post" cssClass="ajax form-horizontal" cssStyle="padding-top:13px;">
	<#if Parameters.brief??>
	<@s.hidden name="page.pagepath"/>
	<@s.hidden name="page.displayOrder"/>
	<@s.hidden name="page.tagsAsString"/>
	<@s.hidden name="page.head"/>
	<@s.textfield label="%{getText('title')}" name="page.title" size="50" cssStyle="width:400px;"/>
	<@s.textarea theme="simple" id="page_content" label="%{getText('content')}" labelposition="top" name="page.content" cssStyle="width:800px;height:320px;"/>
	<#else>
	<ul class="nav nav-tabs">
		<li class="active"><a href="#_page_base" data-toggle="tab">${action.getText('base')}</a></li>
		<li><a href="#_page_content" data-toggle="tab">${action.getText('content')}</a></li>
		<li><a href="#_page_head" data-toggle="tab">${action.getText('head')}</a></li>
	</ul>
	<div class="tab-content">
	<div id="_page_base" class="tab-pane active">
	<@s.hidden id="page_id" name="page.id" />
	<@s.textfield id="page_path" label="%{getText('path')}" name="page.pagepath" cssClass="required" size="50" cssStyle="width:400px;"/>
	<@s.textfield label="%{getText('displayOrder')}" name="page.displayOrder" type="number" cssClass="integer"/>
	<@s.textfield label="%{getText('tag')}" name="page.tagsAsString" size="50" cssClass="tags" dynamicAttributes={"data-source":"${getUrl(actionBaseUrl+'/suggest')}"} cssStyle="width:400px;"/>
	<@s.textfield label="%{getText('title')}" name="page.title" size="50" cssStyle="width:400px;"/>
	</div>
	<div id="_page_content" class="tab-pane">
	<@s.textarea theme="simple" id="page_content" label="%{getText('content')}" labelposition="top" name="page.content" cssStyle="width:800px;height:260px;"/>
	</div>
	<div id="_page_head" class="tab-pane">
	<@s.textarea theme="simple" id="page_head" name="page.head" cssStyle="width:800px;height:300px;"/>
	</div>
	</div>
	</#if>
	<div class="form-actions">
	<@s.submit id="save" value="%{getText('save')}" theme="simple"/>
	
	<@s.submit id="draft" value="%{getText('draft')}" theme="simple"/>
	<span class="draft" <#if !draft>style="display: none;"</#if>>
	${action.getText('draftDate')}:<span class="draftDate"><#if page.draftDate??>${page.draftDate?datetime}</#if></span>
	<#if page.id??>
	<a class="btn" id="preview" href="${getUrl(cmsPath+page.pagepath)}?preview=true" target="_blank">${action.getText('preview')}</a>
	<#else>
	<a class="btn" id="preview" target="_blank">${action.getText('preview')}</a>
	</#if>
	<@s.submit id="drop" value="%{getText('drop')}" theme="simple"/>
	</span>
	<#if page.id??>
	<a class="btn" id="view" href="${getUrl(cmsPath+page.pagepath)}" target="_blank">${action.getText('view')}</a>
	<#else>
	<a class="btn" id="view" target="_blank">${action.getText('view')}</a>
	</#if>
	</div>
	
</@s.form>
</body>
</html></#escape>


