<#macro ecstart id action="" readonly="false">
<form id="${id}"  action="${action}"  method="post" class="eXtremeTable ajax view" replacement="${id}_main_content" canResizeColWidth="true"  minColWidth="40">
<div class="eXtremeTable"  id="${id}_main_content"  style="width:100%;" >
<#list Parameters?keys as name>
<#if !name?starts_with('resultPage.')&&!(name?starts_with('_')||name?ends_with('_'))>
<input type="hidden"  name="${name}"  value="${Parameters[name]}" />
</#if>
</#list>

<table border="0" cellspacing="0" cellpadding="0" class="sortable" style="table-layout:fixed;" width="100%">
	<thead>
	<tr>
		<#if readonly=="false">
		<td class="nosort"  width="30px"><input type="checkbox" class="checkbox"/></td>
		</#if>
</#macro>
<#macro ectheadtd name editable=true>
<td class="tableHeader <#if editable>editableColumn</#if>" ><span class="resizeTitle">${action.getText(name)}</span>
<span class="resizeBar"  onmousedown="ECSideUtil.StartResize(event);" onmouseup="ECSideUtil.EndResize(event);"></span></td>
</#macro>
<#macro ecmiddle width="200px" readonly="false">
<#if readonly=="false">
<td class="nosort"  width="${width}" ></td>
</#if>
	</tr>
	</thead>
	<tbody>
</#macro>
<#macro ectbodytrstart rowid,odd,readonly="false">
<tr class="${odd?string("odd","even")}" rowid="${rowid}" >
		<#if readonly=="false"><td width="22px"><input type="checkbox"  name="deleteFlag"  value="1"  class="checkbox"/></td></#if>
</#macro>
<#macro ectbodytd cellName,value,cellEdit="",cellEditTemplate="ec_edit_template_input",cellEditAction="ondblclick",class="">
<td <#if class!="">class="${class}"</#if> <#if cellEdit!="">${(cellEditAction!="")?string(cellEditAction,"ondblclick")}="ECSideUtil.editCell(this,'${cellEdit}','${(cellEditTemplate!="")?string(cellEditTemplate,"ec_edit_template_input")}')"</#if>  cellName="${cellName}" >
<#if value=='true'||value=='false'>
${action.getText(value)}
<#else>
${value}
</#if>
</td>
</#macro>
<#macro ectbodytrend rowid width="200px" buttons="" readonly="false" celleditable="true" deleteable="true">
<#if readonly=="false">
<td class="include_if_edited"  width="${width}">
			<#if buttons!="">
			${buttons?replace("#id",rowid)}
			<#else>
			<#if celleditable=="true">
			<button type="button" onclick="ECSideX.save('${rowid}')">${action.getText('save')}</button>
			</#if>
			<button type="button" onclick="ECSideX.input('${rowid}')">${action.getText('edit')}</button>
			<#if deleteable=="true">
			<button type="button" onclick="ECSideX.del('${rowid}')">${action.getText('delete')}</button>
			</#if>
			</#if>
		</td>
</#if>
</tr>
</#macro>
<#macro ecend buttons="" readonly="false" createable="true" celleditable="true" deleteable="true">
	</tbody>
</table>
<div class="toolbar"  style="width:100%;" >
	<table class="toolbarTable"  cellpadding="0"  cellspacing="0" >
	<tr>
	<td class="pageNavigationTool" width="33%" nowrap="nowrap">
	<#if resultPage?exists>
	<input type="button" <#if resultPage.pageNo==1>disabled="disabled"  class="pageNav firstPageD"<#else>class="pageNav firstPage"</#if>  onclick="ECSideX.gotoPage(1);"  title="${action.getText('firstpage')}" />
	<input type="button" <#if resultPage.pageNo==1>disabled="disabled"  class="pageNav prevPageD"<#else>class="pageNav prevPage"</#if> onclick="ECSideX.gotoPage(${resultPage.pageNo-1});"  title="${action.getText('previouspage')}" />
	<input type="button" <#if resultPage.pageNo==resultPage.totalPage>disabled="disabled"  class="pageNav nextPageD"<#else>  class="pageNav nextPage"</#if>  onclick="ECSideX.gotoPage(${resultPage.pageNo+1});"  title="${action.getText('nextpage')}" />
	<input type="button" <#if resultPage.pageNo==resultPage.totalPage>disabled="disabled"  class="pageNav lastPageD"<#else>class="pageNav lastPage"</#if>  onclick="ECSideX.gotoPage(${resultPage.totalPage});"  title="${action.getText('lastpage')}"/>
	<input type="button"  class="pageNav jumpPage"  onclick="ECSideX.reload();" />
	<input type="text"  name="resultPage.pageNo"  value="${resultPage.pageNo}"  class="jumpPageInput"  onkeydown="if (event.keyCode && event.keyCode==13 ) {ECSideX.gotoPageByInput();return false; } " />/${resultPage.totalPage}${action.getText('page')}
				${action.getText('pagesize')}<select name="resultPage.pageSize"  onchange="ECSideX.reload();" >
				<#assign array=[5,10,20,50,100,500]>
				<#list array as ps>
				<option value="${ps}" <#if resultPage.pageSize==ps>selected</#if>>${ps}</option>
				</#list> 
				<option value="${resultPage.totalRecord}">${action.getText('all')}</option>
				</select>${action.getText('row')}
	</#if>
	</td>
	<td class="extendTool" align="center">
		<div style="text-align: center; width: 100%; padding: 3px;"
			class="eXtremeTable">
			<#if buttons!="">
			${buttons}
			<#else>
				<#if readonly=="false">
					<#if createable=="true">
					<button type="button" onclick="ECSideX.input()">${action.getText('create')}</button>
					</#if>
					<#if celleditable=="true">
					<button type="button" onclick="ECSideX.save()">${action.getText('save')}</button>
					</#if>
					<#if deleteable=="true">
					<button type="button" onclick="ECSideX.del()">${action.getText('delete')}</button>
					</#if>
				</#if>
				<button type="button" onclick="ECSideX.reload()">${action.getText('reload')}</button>
			</#if>
		</div>
	</td>
	

	<td class="statusTool" width="33%">
		<#if resultPage?exists>
		${action.getText('total')}${resultPage.totalRecord}${action.getText('record')}<#if resultPage.totalRecord!=0>,${action.getText('display')}${resultPage.start+1}-${resultPage.start+resultPage.result?size}</#if>
		<#else>
		${action.getText('total')}${list?size}${action.getText('record')}<#if list?size!=0>,${action.getText('display')}1-${list?size}</#if>	
		</#if>
	</td>
	</tr>
	</table></div>
</div>
</form>
<#if readonly=="false">
<div style="display: none;"><textarea
	id="ec_edit_template_input">
	<input type="text" class="inputtext" value=""
	onblur="ECSideUtil.updateCell(this,'input')" style="width: 100%;"
	 />
</textarea>
<textarea id="select_template_boolean">
	<select onblur="ECSideUtil.updateCell(this,'select')"
	style="width: 100%;">
	<option value="true">${action.getText('true')}</option>
	<option value="false">${action.getText('false')}</option>
</select>
</textarea>
</div>
</#if>
</#macro>

<#macro ectable config entityName id="ec" action="" actionColumnWidth="200px" actionColumnButtons="" bottomButtons="" readonly=false createable=true celleditable=true deleteable=true>
<#if action!="">
<@ecstart id="${id}" action="${action}" readonly="${readonly?string}"/>
<#else>
<@ecstart id="${id}" action="${entityName}" readonly="${readonly?string}"/>
</#if>
	<#list config?keys as name>
		<#call ectheadtd name="${name}" editable=(!readonly)&&(config[name]["cellEdit"]?exists)/>
	</#list>
<@ecmiddle width="${actionColumnWidth}" readonly="${readonly?string}"/>
<#assign index=0>
<#if resultPage?exists>
<#assign list=resultPage.result>
</#if>
<#list list as entity>
<#assign index=index+1>
<@ectbodytrstart rowid="${entity.id}" odd=(index%2==1)   readonly="${readonly?string}"/>
	<#list config?keys as name>
		<#assign cellName=(config[name]["trimPrefix"]?exists?string('',entityName+'.'))+name>
		<#if (!readonly&&celleditable)&&config[name]["cellEdit"]?exists>
		<#assign edit=config[name]["cellEdit"]?split(",")>
		<#call ectbodytd cellName="${cellName}" value="${config[name]['value']?exists?string(config[name]['value']?if_exists,entity[name]?if_exists?string)}"
		 cellEdit="${edit[0]}" cellEditTemplate="${edit[1]?if_exists}" cellEditAction="${edit[2]?if_exists}" class="${config[name]['class']?if_exists}" />
		<#else>
		<#call ectbodytd cellName="${cellName}" value="${config[name]['value']?exists?string(config[name]['value']?if_exists,entity[name]?if_exists?string)}"  class="${config[name]['class']?if_exists}"/>
		</#if>
	</#list>
	<@ectbodytrend rowid="${entity.id}" width="${actionColumnWidth}" buttons="${actionColumnButtons}" readonly="${readonly?string}"  celleditable="${celleditable?string}" deleteable="${deleteable?string}"/>
</#list>
<@ecend buttons="${bottomButtons}"  readonly="${readonly?string}" createable="${createable?string}" celleditable="${celleditable?string}" deleteable="${deleteable?string}"/>
</#macro>