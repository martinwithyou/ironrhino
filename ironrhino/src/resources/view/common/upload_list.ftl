<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('upload')}</title>
<style>
td.center {text-align:center;}
.hover { border: 2px dashed #333; }
</style>
<script>
	Observation.uploaditem = function(container){
		$('.selectthis').click(function(){select(this)}).dblclick(function(){select(this)});
		$('.uploaditem',container).attr('draggable',true).each(function(){
			var t = $(this);
			this.ondragstart = function(e){
			 e.dataTransfer.effectAllowed = 'copy';
      		 e.dataTransfer.setData('Text', $('input[type="checkbox"]',t.closest('tr')).attr('value'));
		};
		});
	};
	function select(el){
		try{
		 	var s = window.getSelection();
		 	if(s.rangeCount > 0) s.removeAllRanges();
			var range = document.createRange();
			range.selectNode(el);
			s.addRange(range);
		}catch(e){
		}
	}
	function del(){
		var deleteurl = $('#upload_form').attr('action')+'/delete';
		ajax({
			url:deleteurl,
			data:$('#upload_form').serialize(),
			onerror:reload,
			replacement:'files'
		});
	}
	function reload(){
		ajax({
			url:$('#upload_form').attr('action'),
			data:$('#upload_form').serialize(),
			replacement:'files'
		});
	}
	function mkdir(){
		$.alerts.prompt('', 'newfolder', '', function(t){
			if(t){
				var folder = $('#current_folder').text()+t;
				var url = CONTEXT_PATH+'/common/upload/mkdir'+folder;
				ajax({url:url,replacement:'files',success:function(){$('#folder').val(folder)}});
			}
		});
	}
	function addMore(n){
		var f = $('input[type="file"]:last').parent();
		var r;
		for(var i=0;i<n;i++){
			r = f.clone(true);
			f.after(r);
			f = r;
		}
	}
	function upload(files){
		if(files&&files.length)
			return $.ajaxupload(files,{
		        		url:$('#upload_form').attr('action')+'?'+$('#upload_form').serialize(),
		        		name:$('#upload_form input[type="file"]').attr('name'),
		        		beforeSend:Indicator.show,
		        		success:function(xhr){
		        			Ajax.handleResponse(xhr.responseText,{replacement:'files'});
		        			Indicator.hide();
		        		}
		        	});
	}
	
	$(function(){
		$('#more').click(function(){
			addMore(1);		
		});
		if (typeof window.FileReader != 'undefined') {
			$('#upload_form input[type="file"]').change(function(){
					if(upload(this.files)){
						$(this).closest('div').remove();
						addMore(1);
						return false;
					}
				});
			$('#upload_form').bind('dragover',function(e){$(this).addClass('hover');return false;})
			.bind('dragleave',function(e){$(this).removeClass('hover');return false;})
			.get(0).ondrop = function(e){
				e.preventDefault();
				$(this).removeClass('hover');
				upload(e.dataTransfer.files);
				return true;
			};
			
			$(document.body).bind('dragover',function(e){return false;})[0].ondrop = function(e){
				var id = e.dataTransfer.getData('Text');
				var target = $(e.target);
				if(!id ||target.is('#upload_form') || target.parents('#upload_form').length)return true;
				var i = id.lastIndexOf('/');
				if(i>0)id = id.substring(i+1);
				if(e.preventDefault)e.preventDefault();
				if (e.stopPropagation) e.stopPropagation();
				$.alerts.confirm(MessageBundle.get('confirm.delete'),
						MessageBundle.get('select'), function(b) {
							if (b) {
								$('#files input[value="'+id+'"]').attr('checked',true);
								del();
							}
						});
			}
		}		

	});
</script>
</head>
<body>
<@s.form id="upload_form" action="upload" method="post" enctype="multipart/form-data" cssClass="form-inline">
	<#list 1..Parameters.size?default('6')?number as index>
		<@s.file name="file" cssStyle="width:194px;" multiple="true"/>
	</#list>
	<div style="clear:both;">
	<div style="text-align:center;padding-top:30px;">
	<@s.submit theme="simple" value="${action.getText('upload')}"/>
	<span style="margin-left:10px;margin-right:10px;">${action.getText('autorename')}:</span><@s.checkbox theme="simple" name="autorename"/>
	</div>
	</div>
<table id="files" class="checkboxgroup" style="margin-top:50px;width:100%;">
	<caption style="font-size:120%;font-weight:bold;"><@s.hidden id="folder" name="folder"/>${action.getText('current.location')}:<span id="current_folder" style="margin-left:10px;">${folder}<#if !folder?ends_with('/')>/</#if></span></caption>
	<thead>
	<tr style="font-weight:bold;">
		<td width="30px" class="center"><input type="checkbox" class="checkbox"/></td>
		<td>${action.getText('name')}</td>
		<td width="100px" class="center">${action.getText('preview')}</td>
		<td>${action.getText('path')}</td>
	</tr>
	</thead>
	<tfoot>
	<tr>
		<td colspan="4" style="text-align:center;padding:5px 0px;">
		<button type="button" class="btn" onclick="del()">${action.getText('delete')}</button>
		<button type="button" class="btn" onclick="mkdir()">${action.getText('create.subfolder')}</button>
		<button type="button" class="btn" onclick="reload()">${action.getText('reload')}</button>
		</td>
	</tr>
	</tfoot>
	<tbody>
	<#list files.entrySet() as entry>
	<tr>
		<td class="center"><#if entry.key!='..'><input type="checkbox" name="id" value="${entry.key}"/></#if></td>
		<td><#if entry.value><a class="uploaditem" style="color:#1c5a50;" href="<@url value="${fileStoragePath}/upload${folderEncoded}/${entry.key?url}"/>" target="_blank">${entry.key}</a><#else><a style="color:blue;" class="ajax view history" replacement="files" href="<@url value="/common/upload/list${folderEncoded}/${entry.key?replace('..','__')?url}"/>">${entry.key}</a></#if></td>
		<td class="center"><#if entry.value && ['jpg','gif','png','bmp']?seq_contains(entry.key?lower_case?split('.')?last)><a href="<@url value="${fileStoragePath}/upload${folderEncoded}/${entry.key?url}"/>" target="_blank"><img class="uploaditem" src="<@url value="${fileStoragePath}/upload${folderEncoded}/${entry.key?url}"/>" style="width:50px;height:50px;"/></a></#if></td>
		<td><#if entry.value><span class="selectthis"><@url value="${fileStoragePath}/upload${folderEncoded}/${entry.key?url}"/></span></#if></td>
	</tr>
	</#list>
	</tbody>
</table>
</@s.form>
</body>
</html></#escape>


