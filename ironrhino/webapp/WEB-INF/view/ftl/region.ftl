<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>region</title>
<script type="text/javascript">
	function _click(){
		<#if Parameters.input?exists>
		var id=$(this).parents('li')[0].id;
		var name=$(this).text();
		window.top.document.getElementById('${Parameters.input}').value=name;
		//$('#${Parameters.input}',window.top.document).val($(this).text());
		//window.close();
		</#if>
	}
	
	Initialization.treeview= function(){
		$("#treeview").treeview({
			<#if async>
			url: '${base}/region/children',
			click:_click,
			</#if>
			collapsed: true,
			unique: true

		});
		<#if !async>
			$("#treeview span").click(_click);
			var id=document.location.hash;
			if(id)
				$(id).parents("li.expandable").find(">div.hitarea").click();
		</#if>
	};
</script>
</head>
<body>
<#noescape>${treeViewHtml}</#noescape>
</body>
</html></#escape>
