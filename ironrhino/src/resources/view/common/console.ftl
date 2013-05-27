<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('console')}</title>
<style>
.row-fluid{
	margin-top: 10px;
}
.key{
	text-align: right;
	line-height: 30px;
}
</style>
<script>
$(function(){
		$('#trigger .btn').click(function(){
			var t = $(this);
			$.ajax({
				type:'POST',
				url:'<@url value="${actionBaseUrl}/executeJson"/>',
				data:{
					expression : $(this).data('expression')||$(this).text(),
					scope: $(this).data('scope')
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
		<#if printSetting??>
		$('#switch input:checkbox').change(function(e){
			var t = this;
			var key = t.name;
			var value = t.checked;
			$.post('<@url value="${actionBaseUrl}/executeJson"/>',
								{
								expression : 'settingControl.setValue("'+key+'","'+value+'")'
								}
								,function(data){
									if(data && data.actionErrors){
										$(t).closest('.switch').bootstrapSwitch('toggleState');
										alert(data.actionErrors[0]);
										return;
									}
								});
		});
		</#if>
							
});
</script>
</head>
<body>
<@s.form id="form" action="console" method="post" cssClass="ajax focus form-inline well">
	<span>${action.getText('expression')}:<@s.textfield theme="simple" id="expression" name="expression" cssClass="input-xxlarge"/></span>
	<span>${action.getText('scope')}:<@s.select theme="simple" id="scope" name="scope" cssClass="input-medium" list="@org.ironrhino.core.metadata.Scope@values()"/></span>
	<@s.submit id="submit" theme="simple" value="%{getText('confirm')}" />
</@s.form>
<div id="trigger" class="well">
	<#assign triggers = statics['org.ironrhino.core.util.ApplicationContextUtils'].getBean('applicationContextConsole').getTriggers()>
	<#assign index=0>
	<#assign count=triggers.keySet()?size>
	<#list triggers.keySet() as expression>
	<#if index%3 == 0>
	<div class="row-fluid">
	</#if>
	<button type="button" class="btn span4" data-scope="${triggers[expression]?string}"  data-expression="${expression}">${action.getText(expression)}</button>
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
	<div class="row-fluid">
	</#if>
	<div class="span2 key"<#if setting.description?has_content> title="${setting.description}"</#if>>${action.getText(setting.key)}</div>
	<div class="span2"><div class="switch" data-on-label="${action.getText('ON')}" data-off-label="${action.getText('OFF')}"><input type="checkbox" name="${setting.key}"<#if setting.value=='true'> checked="checked"</#if>></div></div>
	<#if (index+1)%3 == 0 || count%3!=0 && index==count-1>
	</div>
	</#if>
	<#assign index=index+1>
	</#list>
</div>
</#if>
</body>
</html></#escape>