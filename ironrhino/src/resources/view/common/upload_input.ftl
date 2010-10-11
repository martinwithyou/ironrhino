<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('upload')}</title>
<script>
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
	<div>
	<span style="margin-right:10px;">${action.getText('folder')}:</span><@s.textfield theme="simple" name="folder" size="50"/>
	<span style="margin-right:10px;">${action.getText('rename')}:</span><@s.checkbox theme="simple" name="rename"/>
	</div>
	<div style="text-align:center;">
	<@s.submit theme="simple" value="${action.getText('upload')}"/>
	<@button id="more" text="${action.getText('more')}"/>
	</div>
	</div>

</@s.form>
</body>
</html></#escape>


