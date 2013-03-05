<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('pageView')}</title>
</head>
<body>


<div class="row">
<div class="span6">
<form class="ajax view form-inline" data-replacement="pv_result">
<span>${action.getText('date')}</span>
<@s.textfield label="%{getText('date')}" theme="simple" id="" name="date" cssClass="date" size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
<div class="span6">
<form class="ajax view form-inline" data-replacement="pv_result">
<span>${action.getText('date')}${action.getText('range')}</span>
<@s.textfield label="%{getText('from')}" theme="simple" id="" name="from" cssClass="date"  size="10" maxlength="10"/>
<i class="icon-arrow-right"></i>
<@s.textfield label="%{getText('to')}" theme="simple" id="" name="to" cssClass="date"  size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
</div>
<div id="pv_result">
<#assign dataurl=getUrl("/common/pageView/pv")/>
<#if request.queryString?has_content>
<#assign dataurl=dataurl+'?'+request.queryString/>
<#elseif date??>
<#assign dataurl=dataurl+'?date='+date?string('yyyy-MM-dd')/>
</#if>
<div class="ajaxpanel" data-url="${dataurl}"></div>
</div>

<div class="row">
<div class="span6">
</div>
<div class="span6">
<form class="ajax view form-inline" data-replacement="uip_result">
<span>${action.getText('date')}${action.getText('range')}</span>
<@s.textfield label="%{getText('from')}" theme="simple" id="" name="from" cssClass="date"  size="10" maxlength="10"/>
<i class="icon-arrow-right"></i>
<@s.textfield label="%{getText('to')}" theme="simple" id="" name="to" cssClass="date"  size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
</div>
<div id="uip_result">
<#assign dataurl=getUrl("/common/pageView/uip")/>
<#if request.queryString?has_content>
<#assign dataurl=dataurl+'?'+request.queryString/>
<#elseif from?? && to??>
<#assign dataurl=dataurl+'?from='+from?string('yyyy-MM-dd')+'&to='+to?string('yyyy-MM-dd')/>
</#if>
<div class="ajaxpanel" data-url="${dataurl}"></div>
</div>


<div class="row">
<div class="span6">
</div>
<div class="span6">
<form class="ajax view form-inline" data-replacement="usid_result">
<span>${action.getText('date')}${action.getText('range')}</span>
<@s.textfield label="%{getText('from')}" theme="simple" id="" name="from" cssClass="date"  size="10" maxlength="10"/>
<i class="icon-arrow-right"></i>
<@s.textfield label="%{getText('to')}" theme="simple" id="" name="to" cssClass="date"  size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
</div>
<div id="usid_result">
<#assign dataurl=getUrl("/common/pageView/usid")/>
<#if request.queryString?has_content>
<#assign dataurl=dataurl+'?'+request.queryString/>
<#elseif from?? && to??>
<#assign dataurl=dataurl+'?from='+from?string('yyyy-MM-dd')+'&to='+to?string('yyyy-MM-dd')/>
</#if>
<div class="ajaxpanel" data-url="${dataurl}"></div>
</div>

<div class="row">
<div class="span6">
</div>
<div class="span6">
<form class="ajax view form-inline" data-replacement="uu_result">
<span>${action.getText('date')}${action.getText('range')}</span>
<@s.textfield label="%{getText('from')}" theme="simple" id="" name="from" cssClass="date"  size="10" maxlength="10"/>
<i class="icon-arrow-right"></i>
<@s.textfield label="%{getText('to')}" theme="simple" id="" name="to" cssClass="date"  size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
</div>
<div id="uu_result">
<#assign dataurl=getUrl("/common/pageView/uu")/>
<#if request.queryString?has_content>
<#assign dataurl=dataurl+'?'+request.queryString/>
<#elseif from?? && to??>
<#assign dataurl=dataurl+'?from='+from?string('yyyy-MM-dd')+'&to='+to?string('yyyy-MM-dd')/>
</#if>
<div class="ajaxpanel" data-url="${dataurl}"></div>
</div>

</body>
</html></#escape>
