<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Create/Edit Product</title>
</head>
<body>
<@s.form action="save2" method="post" cssClass="ajax">
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
	<@s.textfield label="%{getText('spec')}" name="product.spec" />
	<@s.textfield label="%{getText('material')}"
		name="product.material" />
	<@s.textfield label="%{getText('size')}" name="product.size" />
	<@s.select label="%{getText('color')}" name="product.color"
		list="@org.ironrhino.pms.model.Color@values()" listKey="name"
		listValue="displayName" />
	<@s.textfield label="%{getText('inventory')}"
		name="product.inventory" />
	<@s.textfield label="%{getText('price')}" name="product.price" />
	<@s.textarea label="%{getText('description')}"
		name="product.description" />
	<@s.select label="%{getText('status')}" name="product.status"
		list="@org.ironrhino.pms.model.ProductStatus@values()" listKey="name"
		listValue="displayName" />
	<@s.textfield label="%{getText('displayOrder')}"
		name="product.displayOrder" />
	<@s.checkbox label="%{getText('released')}"
		name="product.released" />
	<@s.checkbox label="%{getText('newArrival')}"
		name="product.newArrival" />
	<@s.textfield label="%{getText('newArrivalTimeLimit')}"
		name="product.newArrivalTimeLimit" cssClass="date" />
	<@s.iterator
		value="@org.ironrhino.core.ext.hibernate.CustomizableEntityChanger@getCustomizedProperties('org.ironrhino.pms.model.Product')">
		<@s.textfield label="%{getText(key)}"
			name="%{'product.customProperties.'+key}"
			cssClass="%{value.name=='DATE'?'date':''}" />
	</@s.iterator>
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html></#escape>


