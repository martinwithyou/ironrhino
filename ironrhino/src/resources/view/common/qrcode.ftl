<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('qrcode')}</title>
</head>
<body>
<@s.form id="qrcode_form" action="qrcode" method="post" enctype="multipart/form-data" cssClass="form-horizontal" target="_blank">
<@s.textfield label="%{getText('content')}" name="content" cssClass="input-xxlarge"/>
<@s.textfield label="%{getText('width')}" type="number" name="width" cssClass="integer"/>
<@s.textfield label="%{getText('height')}" type="number" name="height" cssClass="integer"/>
<@s.file label="%{getText('watermark')}" name="watermark"/>
<@s.submit value="%{getText('confirm')}" />
</@s.form>
</body>
</html></#escape>


