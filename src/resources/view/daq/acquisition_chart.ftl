<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${title!'chart'}</title>
</head>
<body>
<#if !type?? && !place?? && !recent??>
<form id="daterange" action="<@url value="/daq/acquisition/chart"/>" method="get" class="ajax view form-inline nodirty" replacement="c"  style="margin-left:10px;">
	<span style="margin-right:10px;"><@selectDictionary dictionaryName=dictionaryNameAcquisitionType name="type" value=type! class="span2"/></span>
	<span style="margin-right:10px;"><@checkDictionary dictionaryName=dictionaryNameAcquisitionPlace name="place" value=place!/></span>
	<@s.textfield theme="simple" name="date" cssClass="date required"/>
	<@s.submit theme="simple" value="%{getText('confirm')}"/>
</form>
</#if>
<div id="c" style="clear: both;">
<#if type?? && place??>
<#assign dataurl=getUrl("/daq/acquisition/data")/>
<#if request.queryString??>
<#assign dataurl=dataurl+'?'+request.queryString>
</#if>
<div id="chart" class="chart" data="<@url value="${dataurl}"/>" style="width:100%; height:600px;" data-interval="${Parameters.interval?default('60000')}" data-quiet="true">
<span style="color:red;">请先安装flash插件</span>
</div>
</#if>
</div>
</body>
</html></#escape>
