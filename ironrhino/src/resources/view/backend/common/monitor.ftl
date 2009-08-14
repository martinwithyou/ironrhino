<#macro renderTR node>
<tr id="node-${node.id}"<#if node.parent?exists&&node.parent.id gt 0> class="child-of-node-${node.parent.id}"</#if>>
        <td>${node.name}</td>
        <td>${node.value.long}</td>
        <td>${node.value.double}</td>
        <td><a href="#">detail</a></td>
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
</head>
<body>
<table class="treeTable expanded" width="100%">
  <thead>
    <tr>
      <th>name</th>
      <th width="10%">longValue</th>
      <th width="10%">doubleValue</th>
      <th width="20%"></th>
    </tr>
  </thead>
  <tbody>
    <#list list as var>
      <@renderTR var/>
    </#list>
  </tbody>
</table>
</body>
</html></#escape>
