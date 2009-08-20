<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Control Panel</title>
<script>
Initialization.init = function(){
$('#rebuild_category_tree').click(function(){ApplicationContextConsole.execute('categoryTreeControl.buildCategoryTree()',function(){alert('success')})});
$('#generate_static_page').click(function(){ApplicationContextConsole.execute('productPageGenerator.generate()',function(){alert('success');});});
$('#compile_rulebase').click(function(){ApplicationContextConsole.execute('ruleProvider.compileRuleBase()',function(){alert('success');});});
$('#compass_index').click(function(){ApplicationContextConsole.execute('compassGps.index()',function(){alert('success');});});
$('#execute').click(function(){ApplicationContextConsole.execute($('#cmd').val(),function(result){alert(result);});});
}

</script>
</head>
<body>
<div>
<@button id="rebuild_category_tree"/>
<@button id="compile_rulebase"/>
<@button id="compass_index"/>
</div>
<div><input id="cmd" type="text" name="cmd" size="80" />
<@button id="execute"/>
</div>
</body>
</html></#escape>
