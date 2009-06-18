<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%
response.setHeader("Cache-Control", "max-age=86400");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>region</title>
<script type="text/javascript">
	function _click(){
		<c:if test="${not empty param['input']}">
		var id=$(this).parents('li')[0].id;
		var name=$(this).text();
		window.top.document.getElementById('${param['input']}').value=name;
		//$('#${param['input']}',window.top.document).val($(this).text());
		//window.close();
		</c:if>
	}
	
	Initialization.treeview= function(){
		$("#treeview").treeview({
			<s:if test="async">
			url: "<c:url value="/region/children"/>",
			click:_click,
			</s:if>
			collapsed: true,
			unique: true

		});
		<s:if test="!async">
			$("#treeview span").click(_click);
			var id=document.location.hash;
			if(id)
				$(id).parents("li.expandable").find(">div.hitarea").click();
		</s:if>
	};
</script>
</head>
<body>
<s:property value="treeViewHtml" escape="false" />
</body>
</html>
