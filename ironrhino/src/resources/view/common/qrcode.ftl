<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('qrcode')}</title>
</head>
<body>
<ul class="nav nav-tabs">
	<li class="active"><a href="#decode" data-toggle="tab">${action.getText('decode')}</a></li>
	<li><a href="#encode" data-toggle="tab">${action.getText('encode')}</a></li>
</ul>
<div class="tab-content">
	<div id="decode" class="tab-pane active">
		<#if content??>
		<div class="alert alert-success">
		<button type="button" class="close" data-dismiss="alert">×</button>
		${content}
		</div>
		</#if>
		<@s.form id="qrcode_form" action="qrcode" method="post" enctype="multipart/form-data" cssClass="form-horizontal">
		<@s.hidden name="decode" value="true"/>
		<@s.file label="%{getText('qrcode')}" name="file"/>
		<@s.textfield label="%{getText('url')}" name="url" cssClass="input-xxlarge"/>
		<@s.textfield label="%{getText('encoding')}" name="encoding" cssClass="input-small"/>
		<@s.submit value="%{getText('confirm')}" />
		</@s.form>
	</div>
	<div id="encode" class="tab-pane">
		<@s.form id="qrcode_form" action="qrcode" method="post" enctype="multipart/form-data" cssClass="form-horizontal" target="_blank">
		<@s.textfield label="%{getText('content')}" name="content" value="" cssClass="input-xxlarge"/>
		<@s.textfield label="%{getText('encoding')}" name="encoding" cssClass="input-small"/>
		<@s.textfield label="%{getText('width')}" type="number" name="width" cssClass="integer"/>
		<@s.textfield label="%{getText('height')}" type="number" name="height" cssClass="integer"/>
		<@s.file label="%{getText('watermark')}" name="file"/>
		<@s.submit value="%{getText('confirm')}" />
		</@s.form>
	</div>
</div>
</body>
</html></#escape>


