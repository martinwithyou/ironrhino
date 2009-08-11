<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Product Category</title>
<script type="text/javascript">
	function _click(){
		var id=$(this).parents('li')[0].id;
		var name=$(this).text();
		if(!confirm('change to '+name+'?'))
		return;
		var url='${base}/backend/pms/product/category/${Parameters.id}?categoryId='+id;
		ajax({url:url,dataType:'json'});
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
			<#if product.category?exists>
			$("#<@s.property value="product.category.id"/>").parents("li.expandable").find(">div.hitarea").click();
			</#if>
		</#if>
	};
</script>
</head>
<body>
<@s.action var="category" name="category!tree" executeResult="false"/>
<@s.property value="#attr.category.treeViewHtml" escape="false" />
</body>
</html></#escape>