<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('upload')}</title>
<style>
#content.hover { border: 1px dashed #333; }
</style>
<script>
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
	function mkdir(){
			$.alerts.prompt('', 'newfolder', '', function(t){
				if(t){
					var folder = $('#current_folder').text()+t;
					var url = CONTEXT_PATH+'/common/upload/mkdir'+folder;
					ajax({url:url,replacement:'files',success:function(){$('#folder').val(folder)}});
				}
			});
		}
	$(function(){
		$('#more').click(function(){
			var f = $('input[type="file"]:last').parent();
			var r;
			for(var i=0;i<3;i++){
				r = f.clone(true);
				f.after(r);
				f = r;
			}		
		});
		if (typeof window.FileReader != 'undefined') {
			if(!$.browser.mozilla) return;
			var uploadurl = $('#upload_form').attr('action');
			var inputname = $('#upload_form input[type="file"]').attr('name');
			$('#content').bind('dragover',function(e){$(this).addClass('hover');return false;})
			.bind('dragleave',function(e){$(this).removeClass('hover');return false;})
			.get(0).ondrop = function(e){
				e.preventDefault();
				$(this).removeClass('hover');
				var files = e.dataTransfer.files;
				if(files){
					var length = files.length;
				  	var size = 0;
				  	for(var i=0;i<length;i++)
				  		size+=files[i].size;
					var xhr = new XMLHttpRequest();
					var boundary = 'xxxxxxxxx';
					xhr.open('POST', uploadurl, true);
  					xhr.setRequestHeader('Content-Type', 'multipart/form-data, boundary='+boundary); 
  					xhr.setRequestHeader('Content-Length', size);
					xhr.onreadystatechange = function() {
				    if (xhr.readyState == 4) {
				      if ((xhr.status >= 200 && xhr.status <= 200) || xhr.status == 304) {
				        if (xhr.responseText != '') {
				        	Ajax.handleResponse(xhr.responseText,{replacement:'files'});
				        	Indicator.hide();
				        }
				      }
				    }
				  	}
				  var body = '';
				  for(var i=0;i<length;i++){
				  	  body += '--' + boundary + '\r\n';
					  body += 'Content-Disposition: form-data; name='+inputname+'; filename=' + files[i].name + '\r\n';  
					  body += 'Content-Type: '+files[i].type+'\r\n\r\n';  
					  body += files[i].getAsBinary() + '\r\n';  
				  }
				  body += '--' + boundary + '--';
				  xhr.sendAsBinary(body);
				  Indicator.show();
				  return true;
				}
			};
		}
	});
</script>
</head>
<body>
<@s.form id="upload_form" action="upload" method="post" enctype="multipart/form-data" cssClass="line">
	<#list 1..6 as index>
		<@s.file name="file" cssStyle="width:194px;" multiple="true"/>
	</#list>
	<div style="clear:both;">
	<div style="text-align:center;padding-top:30px;">
	<@s.submit theme="simple" value="${action.getText('upload')}"/>
	<@button onclick="mkdir()" text="${action.getText('create.subfolder')}"/>
	<@button id="more" text="${action.getText('more')}"/>
	<span style="margin-left:10px;margin-right:10px;">${action.getText('autorename')}:</span><@s.checkbox theme="simple" name="autorename"/>
	</div>
	</div>
<table id="files" style="margin-top:50px;width:100%;">
	<caption style="font-size:120%;font-weight:bold;"><@s.hidden id="folder" name="folder"/>${action.getText('current.location')}:<span id="current_folder" style="margin-left:10px;">${folder}<#if !folder?ends_with('/')>/</#if></span></caption>
	<thead>
	<tr style="font-weight:bold;">
		<td>${action.getText('name')}</td>
		<td>${action.getText('preview')}</td>
		<td>${action.getText('path')}</td>
		<td width="100px"></td>
	</tr>
	</thead>
	<tbody>
	<#list files.entrySet() as entry>
	<tr>
		<td><#if entry.value><a style="color:#1c5a50;" href="<@url value="${fileStoragePath}/upload${folderEncoded}/${entry.key?url}"/>" target="_blank">${entry.key}</a><#else><a style="color:blue;" class="ajax view" replacement="files" href="<@url value="/common/upload/list${folderEncoded}/${entry.key?replace('..','__')?url}"/>">${entry.key}</a></#if></td>
		<td><#if entry.value && ['jpg','gif','png','bmp']?seq_contains(entry.key?lower_case?split('.')?last)><a href="<@url value="${fileStoragePath}/upload${folderEncoded}/${entry.key?url}"/>" target="_blank"><img src="<@url value="${fileStoragePath}/upload${folderEncoded}/${entry.key?url}"/>" style="width:50px;height:50px;"/></a></#if></td>
		<td><#if entry.value><span onclick="select(this)" ondblclick="select(this)"><@url value="${fileStoragePath}/upload${folderEncoded}/${entry.key?url}"/></span></#if></td>
		<td><#if entry.key!='..'><@button type="link" text="${action.getText('delete')}" class="ajax view" replacement="files" href="${getUrl('/common/upload/delete?id='+folder+'/'+entry.key)}" onprepare="confirm('${action.getText('confirm.prompt')}')"/></#if></td>
	</tr>
	</#list>
	</tbody>
</table>
</@s.form>
</body>
</html></#escape>


