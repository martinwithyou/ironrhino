<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Create/Edit Product</title>
</head>
<body>
<@s.form id="save2" action="/pms/product/save2" method="post" cssClass="ajax">
	<@s.if test="%{!product.isNew()}">
		<@s.hidden name="product.id" />
		<@s.textfield label="%{getText('code')}" name="product.code"
			readonly="true" />
	</@s.if>
	<@s.else>
		<@s.hidden name="categoryId" />
		<@s.textfield label="%{getText('code')}" name="product.code" />
	</@s.else>
	<@s.textfield label="%{getText('name')}" name="product.name" />
	<@s.textfield label="%{getText('inventory')}"
		name="product.inventory" />
	<@s.textfield label="%{getText('price')}" name="product.price" />
	<@s.textarea label="%{getText('description')}" name="product.description" cols="50" rows="5"/>
	<@s.select label="%{getText('status')}" name="product.status"
		list="@com.ironrhino.pms.model.ProductStatus@values()" listKey="name"
		listValue="displayName" />
	<@s.textfield label="%{getText('displayOrder')}" name="product.displayOrder" cssClass="integer" />
	<@s.iterator
		value="@org.ironrhino.core.hibernate.CustomizableEntityChanger@getCustomizedProperties('com.ironrhino.pms.model.Product')">
		<@s.textfield label="%{getText(key)}"
			name="%{'product.customProperties.'+key}"
			cssClass="%{value.name=='DATE'?'date':''}" />
	</@s.iterator>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>


