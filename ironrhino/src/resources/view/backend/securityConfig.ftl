<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Security Config</title>
<script>
Initialization.init = function(){
	ApplicationContextConsole.get('filterInvocationInterceptorObjectDefinitionSource.definitionAsText',function(text){$('#fsource').val(text)});
	ApplicationContextConsole.get('channelProcessingFilterInvocationDefinitionSource.definitionAsText',function(text){$('#csource').val(text)});
}
</script>
</head>

<body>
<div>FilterInvocationInterceptorObjectDefinitionSource<br />
<textarea id="fsource"
	name="filterInvocationInterceptorObjectDefinitionSource" rows="15"
	cols="100"></textarea> <br />
<button type="button" id="saveFilterInvocationButton" class="btn"
	onclick="ApplicationContextConsole.set('filterInvocationInterceptorObjectDefinitionSource.definitionAsText',$('#fsource').val(),function(){alert('success')})" >
	<span><span>${action.getText('save')}</span></span>
	</button>
<button type="button" id="refreshFilterInvocationButton" class="btn"
	onclick="ApplicationContextConsole.call('filterInvocationInterceptorObjectDefinitionSource.refresh()',null,function(){alert('success');})">
	<span><span>${action.getText('reload')}</span></span>
	</button>
	</div>
<div>ChannelProcessingFilterInvocationDefinitionSource<br />
<textarea id="csource"
	name="channelProcessingFilterInvocationDefinitionSource" rows="15"
	cols="100"></textarea> <br />
<button type="button" id="saveChannelProcessingButton" class="btn"
	onclick="ApplicationContextConsole.set('channelProcessingFilterInvocationDefinitionSource.definitionAsText',$('#csource').val(),function(){alert('success')})" >
	<span><span>${action.getText('save')}</span></span>
	</button>
<button type="button" id="refreshChannelProcessingButton" class="btn"
	onclick="ApplicationContextConsole.call('channelProcessingFilterInvocationDefinitionSource.refresh()',null,function(){alert('success');})" >
	<span><span>${action.getText('reload')}</span></span>
	</button></div>
</body>
</html>
