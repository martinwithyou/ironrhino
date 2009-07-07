<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Category Tree</title>
<script type="text/javascript">
	function _click(){
		var id=$(this).parents('li')[0].id;
		var name=$(this).text();
	}
	Initialization.treeview= function(){
		$("#treeview").treeview({
			<#if async?if_exists>
			url: "${base}/backend/pms/category/children",
			click:_click,
			</#if>
			collapsed: true,
			unique: true

		});
		<#if !(async?if_exists)>
			$("#treeview span").click(_click);
			<#if Parameters.focus?exists>
			$("#${Parameters.focus?html}").parents("li.expandable").find(">div.hitarea").click();
			</#if>
		</#if>
	};
</script>
</head>
<body>
<@s.property value="treeViewHtml" escape="false" />
</body>
</html>
