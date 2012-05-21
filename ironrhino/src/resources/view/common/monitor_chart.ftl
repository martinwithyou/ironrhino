<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Monitor</title>
</head>
<body>
<div class="row">
<div class="span6">
<form action="<@url value="/common/monitor/chart/${uid}"/>" class="ajax view form-inline" replacement="c">
<@s.hidden name="vtype"/>
<@s.hidden name="ctype"/>
<span>${action.getText('date')}</span>
<@s.textfield label="%{getText('date')}" theme="simple" name="date" cssClass="date" size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
<div class="span6">
<form action="<@url value="/common/monitor/chart/${uid}"/>" class="ajax view form-inline" replacement="c">
<@s.hidden name="vtype"/>
<@s.hidden name="ctype"/>
<span>${action.getText('date.range')}</span>
<@s.textfield label="%{getText('from')}" theme="simple" name="from" cssClass="date"  size="10" maxlength="10"/>
<i class="icon-arrow-right"></i>
<@s.textfield label="%{getText('to')}" theme="simple" name="to" cssClass="date"  size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
</div>
<div id="c">
<#assign dataurl='/common/monitor/data'/>
<#if uid??>
<#assign dataurl=dataurl+'/'+uid>
</#if>
<#if request.queryString??>
<#assign dataurl=dataurl+'?'+request.queryString>
</#if>
<div id="chart" class="chart" data="<@url value="${dataurl}"/>" style="width:1024px; height:300px;">
</div>
</div>
</body>
</html></#escape>
