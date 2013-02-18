<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('upload')}</title>
</head>
<body>
<@s.form id="upload_form" action="${getUrl(actionBaseUrl)}" method="post" cssClass="ajax view form-inline" dynamicAttributes={"data-replacement":"files"}>
	<input type="hidden" name="pick" value="true"/>
	<table id="files" class="checkboxgroup table table-striped middle" style="margin-top:50px;">
		<caption style="font-size:120%;font-weight:bold;"><@s.hidden id="folder" name="folder"/>${action.getText('current.location')}:<span id="current_folder" style="margin-left:10px;">${folder}<#if !folder?ends_with('/')>/</#if></span></caption>
		<thead>
		<tr style="font-weight:bold;height:43px;">
			<td style="width:30px" class="radio;"></td>
			<td style="width:200px;">${action.getText('name')}</td>
			<td class="center">${action.getText('preview')}</td>
		</tr>
		</thead>
		<tfoot>
		<tr>
			<td colspan="3" class="center">
			<button type="button" class="btn mkdir">${action.getText('create.subfolder')}</button>
			<button type="button" class="btn capture">${action.getText('capture')}</button>
			<button type="button" class="btn reload">${action.getText('reload')}</button>
			</td>
		</tr>
		</tfoot>
		<tbody>
		<#list files.entrySet() as entry>
		<tr>
			<td class="radio"><#if entry.value><input type="radio" name="id" value="<@url value="${fileStoragePath}/upload${folderEncoded}/${entry.key?url}"/>" class="custom"/></#if></td>
			<td><#if entry.value><a class="uploaditem" style="color:#1c5a50;" href="<@url value="${fileStoragePath}/upload${folderEncoded}/${entry.key?url}"/>" target="_blank">${entry.key}</a><#else><a style="color:blue;" class="ajax view" data-replacement="files" href="<@url value="${actionBaseUrl}/pick${folderEncoded}/${entry.key?replace('..','__')?url}"/>">${entry.key}</a></#if></td>
			<td class="center"><#if entry.value && ['jpg','gif','png','bmp']?seq_contains(entry.key?lower_case?split('.')?last)><a href="<@url value="${fileStoragePath}/upload${folderEncoded}/${entry.key?url}"/>" target="_blank"><img class="uploaditem" src="<@url value="${fileStoragePath}/upload${folderEncoded}/${entry.key?url}"/>" style="width:50px;height:50px;"/></a></#if></td>
		</tr>
		</#list>
		</tbody>
	</table>
</@s.form>
</body>
</html></#escape>


