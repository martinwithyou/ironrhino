<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Monitor</title>
<script type="text/javascript">
swfobject.embedSWF('${base}/images/open-flash-chart.swf', 'chart', '1024', '300', '9.0.0','${base}/images/expressInstall.swf',{'data-file':'bar'},{wmode:"transparent"});
</script>
</head>
<body>
<form action="monitor" class="ajax view" replacement="data">
<span>${action.getText('date')}</span>
<@s.textfield label="%{getText('date')}" theme="simple" name="date" cssClass="date" size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
<form action="monitor" class="ajax view" replacement="data">
<span>${action.getText('date.range')}</span>
<@s.textfield label="%{getText('from')}" theme="simple" name="from" cssClass="date"  size="10" maxlength="10"/>
<@s.textfield label="%{getText('to')}" theme="simple" name="to" cssClass="date"  size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
<div id="chart">
</div>
</body>
</html></#escape>
