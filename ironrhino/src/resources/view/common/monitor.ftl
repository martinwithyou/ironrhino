<#macro renderTR node>
<tr id="node-${node.id}"<#if node.parent??&&node.parent.id gt 0> class="child-of-node-${node.parent.id}"</#if>>
        <td>${node.name}</td>
        <td <#if node.level gt 1>style="padding-left:${(node.level-1)*19}px"</#if>><#if node.value.longValue gt 0><a href="monitor/chart/${node.key?string}?vtype=l<#if request.queryString??>&${request.queryString}</#if>" class="number">${node.value.longValue}</a><span class="perccent">${node.longPercent!}</span></#if></td>
        <td <#if node.level gt 1>style="padding-left:${(node.level-1)*19}px"</#if>><#if node.value.doubleValue gt 0><a href="monitor/chart/${node.key?string}?vtype=d<#if request.queryString??>&${request.queryString}</#if>" class="number">${node.value.doubleValue}</a><span  class="perccent">${node.doublePercent!}</span></#if></td>
        <td></td>
</tr>
<#if node.leaf>
	<#return>
<#else>
<#list node.children as var>
	<@renderTR var/>
</#list>
</#if>
</#macro>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Monitor</title>
<style>
form{
	display:form-inline;
}
.number{
	float: left;
	display: block;
	width: 80px;
}
.percent{
	float: left;
	display: block;
	width: 20px;
}
</style>
</head>
<body>
<div class="row">
<div class="span6">
<form class="ajax view form-inline" replacement="result">
<span>${action.getText('date')}</span>
<@s.textfield label="%{getText('date')}" theme="simple" name="date" cssClass="date" size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
<div class="span6">
<form class="ajax view form-inline" replacement="result">
<span>${action.getText('date.range')}</span>
<@s.textfield label="%{getText('from')}" theme="simple" name="from" cssClass="date"  size="10" maxlength="10"/>
<i class="icon-arrow-right"></i>
<@s.textfield label="%{getText('to')}" theme="simple" name="to" cssClass="date"  size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
</div>
<div id="result">
<#list result.entrySet() as entry>
<table class="treeTable expanded highlightrow" width="100%">
  <#if entry.key??>
  <caption><h3>${entry.key}</h3></caption>
  </#if>
  <thead>
    <tr>
      <th>name</th>
      <th width="20%">longValue</th>
      <th width="20%">doubleValue</th>
      <th width="10%"></th>
    </tr>
  </thead>
  <tbody>
    <#list entry.value as var>
      <@renderTR var/>
    </#list>
  </tbody>
</table>
</#list>
</div>
</body>
</html></#escape>
