<#macro renderTR region>
<tr id="node-${region.id}"<#if region.parent?exists&&region.parent.id gt 0> class="child-of-node-${region.parent.id}"</#if>>
        <td>${region.name}</td>
        <td>${region.fullname}</td>
</tr>
<#if region.leaf>
	<#return>
<#else>
<#list region.children as var>
	<@renderTR var/>
</#list>
</#if>
</#macro>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>region table</title>
</head>
<body>
<table class="treeTable">
  <thead>
    <tr>
      <th>name</th>
      <th>fullname</th>
    </tr>
  </thead>
  <tbody>
    <#list regionTree.children as var>
      <@renderTR var/>
    </#list>
  </tbody>
</table>
</body>
</html></#escape>
