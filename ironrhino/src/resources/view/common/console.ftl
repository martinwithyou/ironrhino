<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('console')}</title>
<#if printSetting??>
<script>
$(function(){
	$.post('<@url value="/common/console/executeJson"/>',
	{
	expression : 'settingControl.getAllBooleanSettings()'
	}
	,function(data){
		var count = data.length;
		if(!count)
			return;
		var html = '<div id="switch" class="well">';
		$.each(data,function(i,setting){
			if(i%3 == 0)
				html += '<div class="row-fluid" style="margin-top:10px;">';
			html += '<div class="span2" style="text-align:right;" title="'+(setting.description||'')+'">'+setting.key+'</div>';
			html += '<div class="span2"><span class="btn-group switch"><button class="btn'+(setting.value=='true'?' active':'')+'" data-value="true">'+MessageBundle.get('true')+'</button><button class="btn'+(setting.value=='false'?' active':'')+'" data-value="false">'+MessageBundle.get('false')+'</button></span></div>';
			if((i+1)%3 == 0 || count%3!=0 && i==count-1)
				html += '</div>';
		});
		html += '</div>';
		$(html).insertAfter($('#dashboard'));
		$('.switch',$('#switch')).each(function() {
				var t = $(this);
				$('.active',t).css({
										'font-weight' : 'bold'
									});
				t.children().css('cursor', 'pointer').click(function() {
							var button = $(this);
							if(button.hasClass('active'))
								return;
							var key = button.closest('div').prev().text();
							var value = button.data('value');
							$.post('<@url value="/common/console/executeJson"/>',
									{
									expression : 'settingControl.setValue("'+key+'","'+value+'")'
									}
									,function(data){
										t.children().removeClass('active').css({
													'font-weight' : 'normal'
												});
										button.addClass('active').css({
													'font-weight' : 'bold'
												});
									});
							
						});
			});
	});
});
</script>
</#if>
</head>
<body>
<@s.form id="form" action="console" method="post" cssClass="ajax focus form-inline well">
	<span>${action.getText('expression')}:<@s.textfield theme="simple" id="expression" name="expression" cssStyle="width:400px;"/></span>
	<span style="margin: 0 10px;">${action.getText('global')}:<@s.checkbox theme="simple" id="global" name="global"/></span>
	<@s.submit id="submit" theme="simple" value="%{getText('confirm')}" />
</@s.form>
<div id="dashboard" class="well">
	<div class="row-fluid">
	<button type="button" class="btn span4" onclick="$('#expression').val($(this).text());$('#global').attr('checked',false);$('#form').submit()">indexManager.rebuild()</button>
	<button type="button" class="btn span4" onclick="$('#expression').val($(this).text());$('#global').attr('checked',true);$('#form').submit()">freemarkerConfiguration.clearTemplateCache()</button>
	</div>
</div>
</body>
</html></#escape>