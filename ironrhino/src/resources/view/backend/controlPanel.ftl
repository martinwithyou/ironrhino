<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Control Panel</title>
<script>
Initialization.init = function(){
$('#rebuild_category_tree').click(function(){ApplicationContextConsole.execute('categoryTreeControl.buildCategoryTree()',function(){alert('success')})});
$('#generate_static_page').click(function(){ApplicationContextConsole.execute('productPageGenerator.generate()',function(){alert('success');});});
$('#send_newArrived_product').click(function(){ApplicationContextConsole.execute('newArrivedProductNotifier.send()',function(){alert('success');});});
$('#compile_rulebase').click(function(){ApplicationContextConsole.execute('ruleProvider.compileRuleBase()',function(){alert('success');});});
$('#compass_index').click(function(){ApplicationContextConsole.execute('compassGps.index()',function(){alert('success');});});
$('#execute').click(function(){ApplicationContextConsole.execute($('#cmd').val(),function(result){alert(result);});});
}

</script>
</head>
<body>
<div><button type="button" id="rebuild_category_tree" class="btn">
	<span><span>rebuild category tree</span></span>
	</button>
	<button type="button"
	id="generate_static_page"  class="btn">
	<span><span>generate static page</span></span>
	</button>
	<button type="button"
	id="send_newArrived_product"  class="btn">
	<span><span>send newArrived product</span></span>
	</button>
	<button type="button"
	id="compile_rulebase"  class="btn">
	<span><span>compile rulebase</span></span>
	</button>
	<button type="button"
	id="compass_index"  class="btn">
	<span><span>compass index</span></span>
	</button>
</div>
<div><input id="cmd" type="text" name="cmd" size="80" />
<button type="button" id="execute"  class="btn"><span><span>execute</span></span></button>
</div>
</body>
</html></#escape>
