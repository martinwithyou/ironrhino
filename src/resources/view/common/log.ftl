<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('log')}</title>
<style>
	#result{
		line-height:1.5em;
	}
</style>
<script>
	$(function(){
		$('#download').click(function(){
			document.location.href+='/download?id='+$('#filename').val();
		});
		$('#clear').click(function(){
			$('#result').html('');
		});
		$('#view').click(function(){
			$('#result').html('');
			var source = $('#result').data('source');
			if(source) source.close();
			var url = 'log/event?id='+$('#filename').val();
			if($('#tail').val())
				url+='&tail='+$('#tail').val();
			source = new EventSource(url);
			$('#result').data('source',source);
			source.addEventListener('remove',function(event){
				$('#result').html('');
			},false);
			source.addEventListener('replace',function(event){
				$('#result').html(event.data.replace(/\n/g,'<br>')+'<br/>');
			},false);
			source.addEventListener('append',function(event){
				$('#result').append(event.data.replace(/\n/g,'<br>')+'<br/>');
			},false);
	        source.onmessage = function (event) {
	           	$('#result').append(event.data.replace(/\n/g,'<br>')+'<br/>');
	        };
		});
	});
	</script>
</head>
<body>
<input id="filename" type="text" size="50"/><input id="tail" type="text" size="5" value="4096"/><@button id="view" text="${action.getText('view')}"/><@button id="clear" text="${action.getText('clear')}"/><@button id="download" text="${action.getText('download')}"/>
<div id="result">
</div>
</body>
</html></#escape>