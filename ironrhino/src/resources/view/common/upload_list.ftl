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
	function addMore(n){
		var f = $('input[type="file"]:last').parent();
		var r;
		for(var i=0;i<n;i++){
			r = f.clone(true);
			f.after(r);
			f = r;
		}
	}
	function handleFiles(files){
		if(!files)return;
		var uploadurl = $('#upload_form').attr('action');
		var xhr = new XMLHttpRequest();
		var boundary = 'xxxxxxxxx';
		xhr.open('POST', uploadurl, true);
		xhr.setRequestHeader('Content-Type', 'multipart/form-data, boundary='+boundary); 
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
	  body = compose(files,boundary);
	  alert(body);
	  if(body){
	  	Indicator.show();
		  if(xhr.sendAsBinary)
		  	xhr.sendAsBinary(body);
		  else
		  	xhr.send(body);
	  	}else{
	  		xhr.abort();
	  	}
	}
	function compose(files,boundary){
		var inputname = $('#upload_form input[type="file"]').attr('name');
		if($.browser.mozilla){
		  var body = '';
		  for(var i=0;i<files.length;i++){
		  	  body += '--' + boundary + '\r\n';
			  body += 'Content-Disposition: form-data; name='+inputname+'; filename=' + files[i].name + '\r\n';  
			  body += 'Content-Type: '+files[i].type+'\r\n\r\n';  
			  body += files[i].getAsBinary() + '\r\n';  
		  }
		  body += '--' + boundary + '--';
		  return body;
		}else if($.browser.webkit){
			var bb = new BlobBuilder();
			var completed = 0;
			for(var i=0;i<files.length;i++){
			  var f = files[i];
			  var reader = new FileReader();
			  reader.sourceFile = f;
			  reader.onload = function(evt) {
			  	var f = evt.target.sourceFile;
			  	bb.append('--');bb.append(boundary);bb.append('\r\n');
			  	bb.append('Content-Disposition: form-data; name=');bb.append(inputname);bb.append('; filename=');bb.append(f.name);bb.append('\r\n');
				bb.append('Content-Type: ');bb.append(f.type);bb.append('\r\n\r\n');
				bb.append(evt.target.result);bb.append('\r\n');
				completed++;
			  };
			  reader.readAsBinaryString(f);    
		  	}
//		  	while(completed<files.length){
//		  	}
		  	bb.append('--');bb.append(boundary);bb.append('--');
			return bb.getBlob();
		}
	}
	////////////////////////////////////////
	$(function(){
		$('#more').click(function(){
			addMore(1);		
		});
		if (typeof window.FileReader != 'undefined') {
			$('#upload_form input[type="file"]').change(function(){
					handleFiles(this.files);
					$(this).closest('div').remove();
					addMore(1);
					return false;
				});
			$('#content').bind('dragover',function(e){$(this).addClass('hover');return false;})
			.bind('dragleave',function(e){$(this).removeClass('hover');return false;})
			.get(0).ondrop = function(e){
				e.preventDefault();
				$(this).removeClass('hover');
				handleFiles(e.dataTransfer.files);
				return true;
			};
		}
	});
</script>
</head>
<body>
<@s.form id="upload_form" action="upload" method="post" enctype="multipart/form-data" cssClass="line">
	<#list 1..Parameters.size?default('6')?number as index>
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


