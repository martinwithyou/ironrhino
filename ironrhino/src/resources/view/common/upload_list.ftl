<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('upload')}</title>
<script>
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
	});
</script>
</head>
<body>
<@s.form action="upload" method="post" enctype="multipart/form-data" cssClass="line">
	<#list 1..3 as index>
		<@s.file name="file" cssStyle="width:195px;"/>
	</#list>
	<div style="clear:both;">
	<div style="text-align:center;padding-top:30px;">
	<@s.submit theme="simple" value="${action.getText('upload')}"/>
	<span style="margin-left:10px;margin-right:10px;">${action.getText('autorename')}:</span><@s.checkbox theme="simple" name="autorename"/>
	<@button id="more" text="${action.getText('more')}"/>
	</div>
	</div>
<table id="files" style="margin-top:50px;width:100%;">
	<caption><@s.hidden id="folder" name="folder"/>${action.getText('folder')}:<span id="current_folder">${folder}/</span><span style="margin-left:50px;"></span><@button onclick="mkdir()" text="${action.getText('create.subfolder')}"/></caption>
	<tbody>
	<tr>
		<td>${action.getText('name')}</td>
		<td>${action.getText('path')}</td>
		<td width="100px"></td>
	</tr>
	</tbody>
	<tbody>
	<#list files.entrySet() as entry>
	<tr>
		<td><#if entry.value><a href="<@url value="${fileStoragePath}/upload${folder}/${entry.key}"/>" target="_blank">${entry.key}</a><#else><a style="color:blue;" class="ajax view" replacement="files" href="<@url value="/common/upload/list${folder}/${entry.key?replace('..','__')}"/>">${entry.key}</a></#if></td>
		<td><#if entry.value><@url value="${fileStoragePath}/upload${folder}/${entry.key}"/></#if></td>
		<td><#if entry.key!='..'><@button type="link" text="${action.getText('delete')}" class="ajax view" replacement="files" href="${getUrl('/common/upload/delete?id='+folder+'/'+entry.key)}" onprepare="confirm('${action.getText('confirm.prompt')}')"/></#if></td>
	</tr>
	</#list>
	</tbody>
</table>
</@s.form>
</body>
</html></#escape>


