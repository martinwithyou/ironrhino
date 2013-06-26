<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title><#if region.name??>${region.name}-</#if>${action.getText('region')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"name":{"cellEdit":"click"},"areacode":{"cellEdit":"click","width":"100px"},"postcode":{"cellEdit":"click","width":"100px"},"rank":{"cellEdit":"click","width":"100px"},"displayOrder":{"cellEdit":"click","width":"100px"}}>
<#assign actionColumnButtons='
<button type="button" class="btn" data-view="input">${action.getText("edit")}</button>
<button type="button" class="btn" data-action="enter">${action.getText("enter")}</button>
'>
<#assign bottomButtons='
<button type="button" class="btn" data-view="input">${action.getText("create")}</button>
<button type="button" class="btn confirm" data-action="save">${action.getText("save")}</button>
<button type="button" class="btn" data-action="delete" data-shown="selected">${action.getText("delete")}</button>
'+r'
<#if region?? && parentId??>
<#if region.parent??>
<a class="btn" href="${getUrl(actionBaseUrl+"?parentId="+region.parent.id)}">${action.getText("upward")}</a>
<#else>
<a class="btn" href="${getUrl(actionBaseUrl)}">${action.getText("upward")}</a>
</#if>
</#if>
'+'
<button type="button" class="btn" onclick="$(\'#move\').toggle()">${action.getText("move")}</button>
<button type="button" class="btn" onclick="$(\'#merge\').toggle()">${action.getText("merge")}</button>
'>
<@richtable entityName="region" columns=columns actionColumnButtons=actionColumnButtons bottomButtons=bottomButtons/>
<form id="move" action="region/move" method="post" class="ajax reset" style="display:none;" onprepare="return confirm('${action.getText('confirm')}?');" onsuccess="Richtable.reload($('#region_form'))">
	<div style="padding-top:10px;text-align:center;">
	<input id="regionId1" type="hidden" name="id"/>
	<span class="treeselect" data-options="{'url':'<@url value="/region/children"/>','name':'this','id':'#regionId1','cache':false}">${action.getText('select')}</span>
	--&gt;
	<input id="regionId2" type="hidden" name="id"/>
	<span class="treeselect" data-options="{'url':'<@url value="/region/children"/>','name':'this','id':'#regionId2','cache':false}">${action.getText('select')}</span>
	<@s.submit theme="simple" value="%{getText('confirm')}" />
	</div>
</form>
<form id="merge" action="region/merge" method="post" class="ajax reset" style="display:none;" onprepare="return confirm('${action.getText('confirm')}?');" onsuccess="Richtable.reload($('#region_form'))">
	<div style="padding-top:10px;text-align:center;">
	<input id="regionId3" type="hidden" name="id"/>
	<span class="treeselect" data-options="{'url':'<@url value="/region/children"/>','name':'this','id':'#regionId3','cache':false}">${action.getText('select')}</span>
	--&gt;
	<input id="regionId4" type="hidden" name="id"/>
	<span class="treeselect" data-options="{'url':'<@url value="/region/children"/>','name':'this','id':'#regionId4','cache':false}">${action.getText('select')}</span>
	<@s.submit theme="simple" value="%{getText('confirm')}" />
	</div>
</form>
</body>
</html></#escape>
