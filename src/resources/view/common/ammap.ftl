<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ammap</title>
<meta name="decorator" content="none"/>
<script src="<@url value="/assets/components/ammap/swfobject.js"/>"></script>
</head>
<body style="background-color:transparent" >
	<div id="ammap" style="width:800px; height:600px;">
		<strong>Please update your Flash Player</strong>
	</div>
	<script type="text/javascript">
		var so = new SWFObject('<@url value="/assets/components/ammap/ammap.swf"/>', 'ammap', '100%', '100%', '8', '#FFFFFF');
		so.addVariable('path', encodeURIComponent('<@url value="/assets/components/ammap/"/>'));
		so.addVariable('data_file',encodeURIComponent('${Parameters.data_file}'));
		<#assign settingsurl='/common/ammap/settings'/>
		<#if request.queryString??>
			<#assign settingsurl=settingsurl+'?'+request.queryString>
		</#if>
   		so.addVariable('settings_file', encodeURIComponent('<@url value="${settingsurl}"/>'));		
		so.addVariable('preloader_color', '#999999');
		so.addParam('wmode','transparent');
		so.write('ammap');
	</script>
</body>
</html>
</#escape>