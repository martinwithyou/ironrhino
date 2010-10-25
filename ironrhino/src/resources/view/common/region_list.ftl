<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('region')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"name":{"cellEdit":"click"},"displayOrder":{"cellEdit":"click"}}>
<#assign actionColumnButtons=r"
<@button text='${action.getText(\'enter\')}' action='enter'/>
">
<#assign bottomButtons=r"
<@button text='${action.getText(\'create\')}' view='input'/>
<@button text='${action.getText(\'save\')}' action='save'/>
<@button text='${action.getText(\'delete\')}' action='delete'/>
<@button text='${action.getText(\'reload\')}' action='reload'/>
<@button text='${action.getText(\'merge\')}' onclick='$(\'#merge\').toggle()'/>
">
<@richtable entityName="region" columns=columns actionColumnButtons=actionColumnButtons bottomButtons=bottomButtons/>
<form id="merge" action="region/merge" method="post" class="ajax reset" style="display:none;" onprepare="return confirm('${action.getText('confirm')}?');" onsuccess="Richtable.reload($('#region_form'))">
	<div style="padding-top:10px;text-align:center;">
	<input id="regionId1" type="hidden" name="id"/>
	<span id="region1" class="selectregion" regionname="region1" full="true" regionid="regionId1" nocache="true">${action.getText('select')}</span>
	--&gt;
	<input id="regionId2" type="hidden" name="id"/>
	<span id="region2" class="selectregion" regionname="region2" full="true" regionid="regionId2" nocache="true">${action.getText('select')}</span>
	<@s.submit theme="simple" value="%{getText('confirm')}" />
</div>
</form>
</body>
</html></#escape>
