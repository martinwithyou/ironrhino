<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
</head>
<body>
<div class="tabs" tab="#${Parameters.tab!}">
<ul>
	<li><a href="#profile"><span>基本信息</span></a></li>
	<li><a href="#password"><span>修改密码</span></a></li>
	<li><a href="#email"><span>修改email</span></a></li>
	<li><a href="#invite"><span>邀请朋友</span></a></li>
</ul>
<div id="profile"><@s.form id="profile_form" action="profile"
	method="post" cssClass="ajax">
	<@s.textfield label="%{getText('name')}" name="user.name" />
	<@s.select label="%{getText('sex')}" name="user.sex"
		list="@org.ironrhino.common.model.Sex@values()" listKey="name"
		listValue="displayName" />
	<@s.textfield label="%{getText('birthday')}" name="user.birthday"
		cssClass="date" />
	<@s.textfield id="address" label="%{getText('address')}"
		name="user.address">
		<@s.param name="after">
			<span class="link region" regionname="address" full="true">select</span>
		</@s.param>
	</@s.textfield>
	<@s.textfield label="%{getText('postcode')}" name="user.postcode" />
	<@s.textfield label="%{getText('phone')}" name="user.phone" />
	<@s.submit value="%{getText('save')}" />
	</@s.form>
	</div>
<div id="password"><@s.form id="password_form" action="password"
	method="post" cssClass="ajax">
	<@s.password label="%{getText('currentPassword')}"
		name="currentPassword" cssClass="required" />
	<@s.password label="%{getText('password')}" name="password"
		cssClass="required" />
	<@s.password label="%{getText('confirmPassword')}"
		name="confirmPassword" cssClass="required" />
	<@s.submit value="%{getText('save')}" />
</@s.form></div>
<div id="email"><@s.form id="email_form" action="email"
	method="post" cssClass="ajax">
	<@s.password label="%{getText('currentPassword')}"
		name="currentPassword" cssClass="required" />
	<@s.textfield label="%{getText('email')}" name="user.email"
		cssClass="required email" />
	<@s.submit value="%{getText('save')}" />
</@s.form></div>
<div id="invite"><@s.form id="invite_form" action="invite"
	method="post" cssClass="ajax">
	<@s.textfield label="%{getText('email')}" name="user.email" value=""
		cssClass="required email" />
	<@s.submit value="%{getText('save')}" />
</@s.form></div>
</div>
</body>
</html></#escape>

