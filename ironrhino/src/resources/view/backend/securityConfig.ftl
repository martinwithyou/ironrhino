<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Security Config</title>
<script>
Initialization.init = function(){
	ApplicationContextConsole.execute('filterInvocationInterceptorObjectDefinitionSource.definitionAsText',function(text){$('#fsource').val(text)});
	ApplicationContextConsole.execute('channelProcessingFilterInvocationDefinitionSource.definitionAsText',function(text){$('#csource').val(text)});
}
</script>
</head>

<body>
<div>FilterInvocationInterceptorObjectDefinitionSource<br />
<textarea id="fsource"
	name="filterInvocationInterceptorObjectDefinitionSource" rows="15"
	cols="100"></textarea> <br />
<button type="button" id="saveFilterInvocationButton" class="btn"
	onclick="ApplicationContextConsole.execute('filterInvocationInterceptorObjectDefinitionSource.definitionAsText=\''+$('#fsource').val().replace(/#/g,'\#').replace(/\\/g,'\\\\')+'\'',function(){alert('success')})" >
	<span><span>${action.getText('save')}</span></span>
	</button>
<button type="button" id="refreshFilterInvocationButton" class="btn"
	onclick="ApplicationContextConsole.execute('filterInvocationInterceptorObjectDefinitionSource.refresh()',function(){alert('success');})">
	<span><span>${action.getText('reload')}</span></span>
	</button>
	</div>
<div>ChannelProcessingFilterInvocationDefinitionSource<br />
<textarea id="csource"
	name="channelProcessingFilterInvocationDefinitionSource" rows="15"
	cols="100"></textarea> <br />
<button type="button" id="saveChannelProcessingButton" class="btn"
	onclick="ApplicationContextConsole.execute('channelProcessingFilterInvocationDefinitionSource.definitionAsText=\''+$('#csource').val().replace(/\\/g,'\\\\')+'\'',function(){alert('success')})" >
	<span><span>${action.getText('save')}</span></span>
	</button>
<button type="button" id="refreshChannelProcessingButton" class="btn"
	onclick="ApplicationContextConsole.execute('channelProcessingFilterInvocationDefinitionSource.refresh()',function(){alert('success');})" >
	<span><span>${action.getText('reload')}</span></span>
	</button></div>
</body>
</html></#escape>
