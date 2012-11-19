<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('console')}</title>
<#if printSetting??>
<script>
$(function(){
		$('#trigger .btn').click(function(){
			var t = $(this);
			$.ajax({
				type:'POST',
				url:'<@url value="${actionBaseUrl}/executeJson"/>',
				data:{
					expression : $(this).data('expression')||$(this).text(),
					global: $(this).data('global')||'false'
				},
				beforeSend:function(){
					t.prop('disabled',true);
				},
				success:function(data){
					if(data && data.actionErrors){
						alert(data.actionErrors[0]);
					}else{
						alert(MessageBundle.get('success'));
					}
					t.prop('disabled',false);
				},
				error:function(data){
					alert(MessageBundle.get('error'));
					t.prop('disabled',false);
				}
			});
		});
		$('#switch .btn-group').each(function() {
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
						$.post('<@url value="${actionBaseUrl}/executeJson"/>',
								{
								expression : 'settingControl.setValue("'+key+'","'+value+'")',
								global: false
								}
								,function(data){
									if(data && data.actionErrors){
										alert(data.actionErrors[0]);
										return;
									}
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
</script>
</#if>
</head>
<body>
<@s.form id="form" action="console" method="post" cssClass="ajax focus form-inline well">
	<span>${action.getText('expression')}:<@s.textfield theme="simple" id="expression" name="expression" cssStyle="width:400px;"/></span>
	<span style="margin: 0 10px;">${action.getText('global')}:<@s.checkbox theme="simple" id="global" name="global"/></span>
	<@s.submit id="submit" theme="simple" value="%{getText('confirm')}" />
</@s.form>
<div id="trigger" class="well">
	<#assign triggers = statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('applicationContextConsole').getTriggers()>
	<#assign index=0>
	<#assign count=triggers.keySet()?size>
	<#list triggers.keySet() as expression>
	<#if index%3 == 0>
	<div class="row-fluid" style="margin-top:10px;">
	</#if>
	<button type="button" class="btn span4" data-global="${triggers[expression]?string}"  data-expression="${expression}">${action.getText(expression)}</button>
	<#if (index+1)%3 == 0 || count%3!=0 && index==count-1>
	</div>
	</#if>
	<#assign index=index+1>
	</#list>
</div>
<#if printSetting??>
<div id="switch" class="well">
	<#assign settings = statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('settingControl').getAllBooleanSettings()>
	<#assign index=0>
	<#assign count=settings?size>
	<#list settings as setting>
	<#if index%3 == 0>
	<div class="row-fluid" style="margin-top:10px;">
	</#if>
	<div class="span2" style="text-align:right;" title="${setting.description!}" data-key="${setting.key}">${action.getText(setting.key)}</div>
	<div class="span2"><span class="btn-group"><button class="btn<#if setting.value=='true'> active</#if>" data-value="true">${action.getText("true")}</button><button class="btn<#if setting.value=='false'> active</#if>" data-value="false">${action.getText("false")}</button></span></div>
	<#if (index+1)%3 == 0 || count%3!=0 && index==count-1>
	</div>
	</#if>
	<#assign index=index+1>
	</#list>
</div>
</#if>
</body>
</html></#escape>