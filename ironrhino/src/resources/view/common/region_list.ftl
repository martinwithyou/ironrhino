<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('region')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"name":{"cellEdit":"click"},"areacode":{"cellEdit":"click"},"postcode":{"cellEdit":"click"},"displayOrder":{"cellEdit":"click"}}>
<#assign actionColumnButtons='
<button type="button" class="btn" data-action="enter">${action.getText("enter")}</button>
'>
<#assign bottomButtons='
<button type="button" class="btn" data-view="input">${action.getText("create")}</button>
<button type="button" class="btn" data-action="save">${action.getText("save")}</button>
<button type="button" class="btn" data-action="delete">${action.getText("delete")}</button>
<button type="button" class="btn" data-action="reload">${action.getText("reload")}</button>
'+r'
<#if region?? && parentId??>
<#if region.parent??>
<a class="btn" href="${getUrl("/common/region?parentId="+region.parent.id)}">${action.getText("upward")}</a><#t>
<#else>
<a class="btn" href="${getUrl("/common/region")}"/>${action.getText("upward")}</a><#t>
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
	<span id="region1" class="treeselect" data-options="{'url':'<@url value="/region/children"/>','name':'region1','id':'regionId1','cache':false}">${action.getText('select')}</span>
	--&gt;
	<input id="regionId2" type="hidden" name="id"/>
	<span id="region2" class="treeselect" data-options="{'url':'<@url value="/region/children"/>','name':'region2','id':'regionId2','cache':false}">${action.getText('select')}</span>
	<@s.submit theme="simple" value="%{getText('confirm')}" />
	</div>
</form>
<form id="merge" action="region/merge" method="post" class="ajax reset" style="display:none;" onprepare="return confirm('${action.getText('confirm')}?');" onsuccess="Richtable.reload($('#region_form'))">
	<div style="padding-top:10px;text-align:center;">
	<input id="regionId3" type="hidden" name="id"/>
	<span id="region3" class="treeselect" data-options="{'url':'<@url value="/region/children"/>','name':'region3','id':'regionId3','cache':false}">${action.getText('select')}</span>
	--&gt;
	<input id="regionId4" type="hidden" name="id"/>
	<span id="region4" class="treeselect" data-options="{'url':'<@url value="/region/children"/>','name':'region4','id':'regionId4','cache':false}">${action.getText('select')}</span>
	<@s.submit theme="simple" value="%{getText('confirm')}" />
	</div>
</form>
</body>
</html></#escape>
