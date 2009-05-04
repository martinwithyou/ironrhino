<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Product Picture</title>
</head>
<body>
<ul id="product_pic_list">
	<@s.iterator value="new int[product.pictureQuantity]" status="status">
		<@s.set name="pictureName"
			value="[1].product.code+(#status.index==0?'':'_'+#status.index)" />
		<li id="li_<@s.property value="#status.index" />"><img
			src='<@s.url value="%{'/pic/'+#pictureName+'.jpg'}"/>'
			style="height: 300px; width: 300px" alt="picture" /><a
			href="picture?actionType=delete&pictureName=${pictureName}"
			class="ajax view" method="post"
			options="{replacement:'product_pic_list'}">delete</a></li>
	</@s.iterator>
</ul>
<@s.form action="picture" method="post" enctype="multipart/form-data"
	cssClass="ajax view">
	<@s.hidden name="actionType" value="save" />
	<@s.hidden name="id" />
	<@s.file label="%{getText('picture')}" name="picture" />
	<@s.checkbox label="%{getText('useWaterMark')}" name="useWaterMark" />
	<@s.checkbox id="overrideDefault" label="%{getText('overrideDefault')}"
		name="overrideDefault" />
	<@s.submit value="%{getText('save')}"/>
</@s.form>
</body>
</html>
