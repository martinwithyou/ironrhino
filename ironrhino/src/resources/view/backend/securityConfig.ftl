<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Security Config</title>
<script>
Initialization.init = function(){
	ApplicationContextConsole.execute('filterInvocationInterceptorObjectDefinitionSource.definitionAsText',function(text){$('#fsource').val(text)});
	ApplicationContextConsole.execute('channelProcessingFilterInvocationDefinitionSource.definitionAsText',function(text){$('#csource').val(text)});
	$('#fsave').click(function(){
		ApplicationContextConsole.execute('filterInvocationInterceptorObjectDefinitionSource.definitionAsText=\''+$('#fsource').val().replace(/#/g,'\#').replace(/\\/g,'\\\\')+'\'',function(){alert('success')});
	});
	$('#ffresh').click(function(){
		ApplicationContextConsole.execute('filterInvocationInterceptorObjectDefinitionSource.refresh()',function(){alert('success');});
	});
	$('#csave').click(function(){
		ApplicationContextConsole.execute('channelProcessingFilterInvocationDefinitionSource.definitionAsText=\''+$('#csource').val().replace(/\\/g,'\\\\')+'\'',function(){alert('success')});
	});
	$('#cfresh').click(function(){
		ApplicationContextConsole.execute('channelProcessingFilterInvocationDefinitionSource.refresh()',function(){alert('success');});
	});
}
</script>
</head>

<body>
<div>FilterInvocationInterceptorObjectDefinitionSource<br />
<textarea id="fsource"
	name="filterInvocationInterceptorObjectDefinitionSource" rows="15"
	cols="100"></textarea> <br />
<@button id="fsave" text="${action.getText('save')}" />
<@button id="ffresh" text="${action.getText('reload')}"/>
</div>
<div>ChannelProcessingFilterInvocationDefinitionSource<br />
<textarea id="csource"
	name="channelProcessingFilterInvocationDefinitionSource" rows="15"
	cols="100"></textarea> <br />
<@button id="csave" text="${action.getText('save')}"/>
<@button id="cfresh" text="${action.getText('reload')}"/>
</div>
</body>
</html></#escape>
