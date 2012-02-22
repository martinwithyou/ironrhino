<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#escape x as x?html><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>${action.getText('region')}${action.getText('list')}</title>
</head>
<body>
<#assign columns={"name":{"cellEdit":"click"},"areacode":{"cellEdit":"click"},"postcode":{"cellEdit":"click"},"displayOrder":{"cellEdit":"click"}}>
<#assign actionColumnButtons=r"
<@button text='${action.getText(\'enter\')}' action='enter'/>
">
<#assign bottomButtons=r"
<@button text='${action.getText(\'create\')}' view='input'/>
<@button text='${action.getText(\'save\')}' action='save'/>
<@button text='${action.getText(\'delete\')}' action='delete'/>
<@button text='${action.getText(\'reload\')}' action='reload'/>
<#if region?? && parentId??>
<#if region.parent??>
<@button text='${action.getText(\'upward\')}' type='link' href='${getUrl(\'/common/region?parentId=\'+region.parent.id)}'/>
<#else>
<@button text='${action.getText(\'upward\')}' type='link' href='${getUrl(\'/common/region\')}'/>
</#if>
</#if>
<@button text='${action.getText(\'move\')}' onclick='$(\'#move\').toggle()'/>
<@button text='${action.getText(\'merge\')}' onclick='$(\'#merge\').toggle()'/>
">
<@richtable entityName="region" columns=columns actionColumnButtons=actionColumnButtons bottomButtons=bottomButtons/>
<form id="move" action="region/move" method="post" class="ajax reset" style="display:none;" onprepare="return confirm('${action.getText('confirm')}?');" onsuccess="Richtable.reload($('#region_form'))">
	<div style="padding-top:10px;text-align:center;">
	<input id="regionId1" type="hidden" name="id"/>
	<span id="region1" class="treeselect" treeoptions="{'url':'<@url value="/region/children"/>','name':'region1','id':'regionId1','cache':false}">${action.getText('select')}</span>
	--&gt;
	<input id="regionId2" type="hidden" name="id"/>
	<span id="region2" class="treeselect"  treeoptions="{'url':'<@url value="/region/children"/>','name':'region2','id':'regionId2','cache':false}">${action.getText('select')}</span>
	<@s.submit theme="simple" value="%{getText('confirm')}" />
	</div>
</form>
<form id="merge" action="region/merge" method="post" class="ajax reset" style="display:none;" onprepare="return confirm('${action.getText('confirm')}?');" onsuccess="Richtable.reload($('#region_form'))">
	<div style="padding-top:10px;text-align:center;">
	<input id="regionId3" type="hidden" name="id"/>
	<span id="region3" class="treeselect" treeoptions="{'url':'<@url value="/region/children"/>','name':'region3','id':'regionId3','cache':false}">${action.getText('select')}</span>
	--&gt;
	<input id="regionId4" type="hidden" name="id"/>
	<span id="region4" class="treeselect"  treeoptions="{'url':'<@url value="/region/children"/>','name':'region4','id':'regionId4','cache':false}">${action.getText('select')}</span>
	<@s.submit theme="simple" value="%{getText('confirm')}" />
	</div>
</form>
</body>
</html></#escape>
