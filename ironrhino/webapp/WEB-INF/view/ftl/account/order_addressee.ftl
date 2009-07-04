<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>ironrhino</title>
</head>
<body>
<@s.form id="addressee" namespace="/account" action="order!addressee"
	method="post">
	<@s.textfield label="%{getText('name')}" name="order.addressee.name"
		cssClass="required" />
	<@s.textfield label="%{getText('address')}" name="address"
		id="order.addressee.address" cssClass="required">
		<@s.param name="after">
			<span class="link" onclick="Region.select('address')">select</span>
		</@s.param>
	</@s.textfield>
	<@s.textfield label="%{getText('postcode')}"
		name="order.addressee.postcode" cssClass="required" />
	<@s.textfield label="%{getText('phone')}" name="phone"
		cssClass="required" />
	<@s.textarea label="%{getText('description')}" name="order.description" />
	<@s.submit value="%{getText('save')}" />
</@s.form>
</body>
</html>


