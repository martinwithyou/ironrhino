<#macro rtstart action="" readonly="false">
<form action="${action}" method="post" class="richtable ajax view" canResizeColWidth="true" minColWidth="40">
<#list Parameters?keys as name>
<#if !name?starts_with('resultPage.')&&!(name?starts_with('_')||name?ends_with('_'))>
<input type="hidden" name="${name}" value="${Parameters[name]}" />
</#if>
</#list>
<table border="0" cellspacing="0" cellpadding="0" class="sortable" style="table-layout:fixed;" width="100%">
<thead>
<tr>
<#if readonly=="false">
<td class="nosort" width="30px"><input type="checkbox" class="checkbox"/></td>
</#if>
</#macro>

<#macro rttheadtd name editable=true>
<td class="tableHeader<#if editable> editableColumn</#if>"><span class="resizeTitle">${action.getText(name)}</span>
<span class="resizeBar" onmousedown="ECSideUtil.StartResize(event);" onmouseup="ECSideUtil.EndResize(event);"></span></td>
</#macro>
<#macro rtmiddle width="150px" readonly="false">
<#if readonly=="false">
<td class="nosort" width="${width}"></td>
</#if>
</tr>
</thead>
<tbody>
</#macro>

<#macro rttbodytrstart rowid,odd,readonly="false">
<tr class="${odd?string("odd","even")}" rowid="${rowid}" >
<#if readonly=="false"><td><input type="checkbox" name="deleteFlag" value="1" class="checkbox"/></td></#if>
</#macro>

<#macro rttbodytd cellName,value,entity="",template="",renderLink="false",cellEdit="",cellEditTemplate="rt_edit_template_input",cellEditAction="ondblclick",class="">
<td <#if class!="">class="${class}" </#if><#if cellEdit!="">${(cellEditAction!="")?string(cellEditAction,"ondblclick")}="ECSideUtil.editCell(this,'${cellEdit}','${(cellEditTemplate!="")?string(cellEditTemplate,"rt_edit_template_input")}')" </#if>cellName="${cellName}">
<#if template=="">
<#if renderLink=="true">
<a href="?${cellName}=${value?url('utf-8')}" class="ajax view">
</#if>
<#if value=='true'||value=='false'>
${action.getText(value)}
<#else>
${value?xhtml}
</#if>
<#if renderLink=="true">
</a>
</#if>
<#else>
<#local temp=template?interpret>
<@temp/>
</#if>
</td>
</#macro>

<#macro rttbodytrend rowid buttons="" readonly="false" celleditable="true" deleteable="true">
<#if readonly=="false">
<td class="include_if_edited">
<#if buttons!="">
${buttons?replace("#id",rowid)}
<#else>
<#if celleditable=="true">
<@button onclick="Richtable.save('${rowid}')" text="${action.getText('save')}"/>
</#if>
<@button onclick="Richtable.input('${rowid}')" text="${action.getText('edit')}"/>
<#if deleteable=="true">
<@button onclick="Richtable.del('${rowid}')" text="${action.getText('delete')}"/>
</#if>
</#if>
</td>
</#if>
</tr>
</#macro>

<#macro rtend buttons="" readonly="false" createable="true" celleditable="true" deleteable="true">
</tbody>
</table>
<div class="toolbar" style="width:100%;" >
<table class="toolbarTable" cellpadding="0" cellspacing="0" >
<tr>
<td class="pageNavigationTool" width="33%" nowrap="nowrap">
<#if resultPage?exists>
<input type="button" <#if resultPage.pageNo==1>disabled="disabled" class="pageNav firstPageD"<#else>class="pageNav firstPage"</#if> title="${action.getText('firstpage')}" />
<input type="button" <#if resultPage.pageNo==1>disabled="disabled" class="pageNav prevPageD"<#else>class="pageNav prevPage"</#if> title="${action.getText('previouspage')}" />
<input type="button" <#if resultPage.pageNo==resultPage.totalPage>disabled="disabled" class="pageNav nextPageD"<#else> class="pageNav nextPage"</#if> title="${action.getText('nextpage')}" />
<input type="button" <#if resultPage.pageNo==resultPage.totalPage>disabled="disabled" class="pageNav lastPageD"<#else>class="pageNav lastPage"</#if> title="${action.getText('lastpage')}"/>
<input type="button" class="pageNav jumpPage"/>
<input type="text" name="resultPage.pageNo" value="${resultPage.pageNo}" class="jumpPageInput"/>/<span class="totalPage">${resultPage.totalPage}</span>${action.getText('page')}
${action.getText('pagesize')}<select name="resultPage.pageSize">
<#local array=[5,10,20,50,100,500]>
<#list array as ps>
<option value="${ps}" <#if resultPage.pageSize==ps>selected</#if>>${ps}</option>
</#list> 
<option value="${resultPage.totalRecord}">${action.getText('all')}</option>
</select>${action.getText('row')}
</#if>
</td>
<td class="extendTool" align="center">
<div style="text-align: center; width: 100%; padding: 3px;" class="richtable">
<#if buttons!="">
${buttons}
<#else>
<#if readonly=="false">
<#if createable=="true"><@button onclick="Richtable.input()" text="${action.getText('create')}"/></#if>
<#if celleditable=="true"><@button onclick="Richtable.save()" text="${action.getText('save')}"/></#if>
<#if deleteable=="true"><@button onclick="Richtable.del()" text="${action.getText('delete')}"/></#if>
</#if><@button onclick="Richtable.reload()" text="${action.getText('reload')}"/></#if>
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
</table>
</div>
</form>
<#if readonly=="false">
<div style="display: none;"><textarea id="rt_edit_template_input">
<input type="text" class="inputtext" value="" onblur="ECSideUtil.updateCell(this,'input')" style="width: 100%;"/>
</textarea>
<textarea id="select_template_boolean">
<select onblur="ECSideUtil.updateCell(this,'select')" style="width: 100%;">
<option value="true">${action.getText('true')}</option>
<option value="false">${action.getText('false')}</option>
</select>
</textarea>
</div>
</#if>
</#macro>

<#macro richtable config entityName action="" actionColumnWidth="150px" actionColumnButtons="" bottomButtons="" readonly=false createable=true celleditable=true deleteable=true>
<@rtstart action="${action?has_content?string(action,entityName)}" readonly="${readonly?string}"/>
<#list config?keys as name>
<@rttheadtd name="${name}" editable=(!readonly)&&(config[name]["cellEdit"]?exists)/>
</#list>
<@rtmiddle width="${actionColumnWidth}" readonly="${readonly?string}"/>
<#local index=0>
<#if resultPage?exists><#local list=resultPage.result></#if>
<#list list as entity>
<#local index=index+1>
<@rttbodytrstart rowid="${entity.id}" odd=(index%2==1)  readonly="${readonly?string}"/>
	<#list config?keys as name>
		<#local cellName=(config[name]["trimPrefix"]?exists?string('',entityName+'.'))+name>
		<#local value=config[name]['value']?exists?string(config[name]['value']?if_exists,entity[name]?if_exists?string)>
		<#if (!readonly&&celleditable)&&config[name]["cellEdit"]?exists>
		<#local edit=config[name]["cellEdit"]?split(",")>
		<@rttbodytd entity="${entity}" cellName="${cellName}" value="${value}" template="${config[name]['template']?if_exists}" renderLink="${config[name]['renderLink']?if_exists?string}" cellEdit="${edit[0]}" cellEditTemplate="${edit[1]?if_exists}" cellEditAction="${edit[2]?if_exists}" class="${config[name]['class']?if_exists}"/>
		<#else>
		<@rttbodytd entity="${entity}" cellName="${cellName}" value="${value}" template="${config[name]['template']?if_exists}" renderLink="${config[name]['renderLink']?if_exists?string}" class="${config[name]['class']?if_exists}"/>
		</#if>
	</#list>
	<@rttbodytrend rowid="${entity.id}" buttons="${actionColumnButtons}" readonly="${readonly?string}" celleditable="${celleditable?string}" deleteable="${deleteable?string}"/>
</#list>
<@rtend buttons="${bottomButtons}" readonly="${readonly?string}" createable="${createable?string}" celleditable="${celleditable?string}" deleteable="${deleteable?string}"/>
</#macro>