<#macro richtable columns entityName formid='' action='' actionColumnWidth='50px' actionColumnButtons='' bottomButtons='' resizable=true sortable=true readonly=false createable=true celleditable=true deleteable=true searchable=false searchButtons='' includeParameters=true showPageSize=true showCheckColumn=true multipleCheck=true columnfilterable=true>
<@rtstart formid=formid action=action entityName=entityName readonly=readonly resizable=resizable sortable=sortable includeParameters=includeParameters showCheckColumn=showCheckColumn multipleCheck=multipleCheck columnfilterable=columnfilterable/>
<#list columns?keys as name>
<#local cellName=((columns[name]['trimPrefix']??)?string('',entityName+'.'))+name>
<@rttheadtd name=name class=columns[name]['class']! width=columns[name]['width']! cellName=cellName cellEdit=columns[name]['cellEdit'] readonly=readonly resizable=resizable excludeIfNotEdited=columns[name]['excludeIfNotEdited']!false/>
</#list>
<@rtmiddle width=actionColumnWidth readonly=readonly/>
<#local index=0>
<#if resultPage??><#local list=resultPage.result></#if>
<#list list as entity>
<#local index=index+1>
<@rttbodytrstart entity=entity readonly=readonly showCheckColumn=showCheckColumn multipleCheck=multipleCheck/>
<#list columns?keys as name>
	<#if columns[name]['value']??>
	<#local value=columns[name]['value']>
	<#else>
	<#local value=entity[name]!>
	</#if>
	<@rttbodytd entity=entity value=value celleditable=columns[name]['cellEdit']?? template=columns[name]['template']!/>
</#list>
<@rttbodytrend entity=entity buttons=actionColumnButtons readonly=readonly/>
</#list>
<@rtend buttons=bottomButtons readonly=readonly createable=createable celleditable=celleditable deleteable=deleteable searchable=searchable searchButtons=searchButtons showPageSize=showPageSize/>
</#macro>

<#macro rtstart formid='',action='',entityName='',readonly=false,resizable=true,sortable=true,includeParameters=true showCheckColumn=true multipleCheck=true columnfilterable=true>
<#local action=action?has_content?string(action,request.requestURI?substring(request.contextPath?length))>
<form id="<#if formid!=''>${formid}<#else>${entityName}_form</#if>" action="${getUrl(action)}" method="post" class="richtable ajax view"<#if actionBaseUrl!=action> data-actionBaseUrl="${actionBaseUrl}"</#if><#if entityName!=action&&entityName!=''> data-entity="${entityName}"</#if>>
<#if includeParameters>
<#list Parameters?keys as name>
<#if name!='_'&&name!='pn'&&name!='ps'&&!name?starts_with('resultPage.')&&name!='keyword'&&name!='check'>
<input type="hidden" name="${name}" value="${Parameters[name]}" />
</#if>
</#list>
</#if>
<table class="richtable<#if sortable> sortable</#if><#if columnfilterable> filtercolumn</#if> highlightrow<#if resizable> resizable</#if>"<#if resizable> data-minColWidth="40"</#if>>
<thead>
<tr>
<#if showCheckColumn>
<td class="nosort <#if multipleCheck>checkbox<#else>radio</#if>" width="30px"><#if multipleCheck><input type="checkbox" class="checkbox"/></#if></td>
</#if>
</#macro>

<#macro rttheadtd name,cellName='',cellEdit='',class='',width='',readonly=false,resizable=true,excludeIfNotEdited=false>
<td class="tableHeader<#if excludeIfNotEdited> excludeIfNotEdited</#if><#if class!=''> ${class}</#if>"<#if width!=''> width="${width}"</#if><#if !readonly> data-cellName="${cellName}"</#if><#if cellEdit!=''> data-cellEdit="${cellEdit}"</#if>>
<#if resizable>
<span class="resizeTitle">${action.getText(name)}</span>
<span class="resizeBar"></span>
<#else>
${action.getText(name)}
</#if>
</td>
</#macro>
<#macro rtmiddle width='50px' readonly=false>
<#if !readonly>
<td class="nosort" width="${width}"></td>
</#if>
</tr>
</thead>
<tbody>
</#macro>

<#macro rttbodytrstart entity readonly=false showCheckColumn=true multipleCheck=true>
<tr<#if !showCheckColumn&&entity.id??> data-rowid="${entity.id?string}"</#if>>
<#if showCheckColumn><td class="<#if multipleCheck>checkbox<#else>radio</#if>"><input type="<#if multipleCheck>checkbox<#else>radio</#if>" name="check"<#if entity.id??> value="${entity.id?string}"</#if>/></td></#if>
</#macro>

<#macro rttbodytd value,entity,celleditable=true,template=''>
<td<#if celleditable><#if value??><#if value?is_boolean> data-cellValue="${value?string}"</#if><#if value?is_hash&&value.displayName??> data-cellValue="${value.name()}"</#if></#if></#if>><#rt>
<#if template==''>
<#if value??>
<#if value?is_boolean>
${action.getText(value?string)}<#t>
<#else>
<#if value?is_hash&&value.displayName??>
${value.displayName}<#t>
<#else>
${value?xhtml}<#t>
</#if>
</#if>
</#if>
<#else>
<#local temp=template?interpret>
<@temp/><#t>
</#if>
</td>
</#macro>

<#macro rttbodytrend entity buttons='' readonly=false>
<#if !readonly>
<td class="action">
<#if buttons!=''>
<#local temp=buttons?interpret>
<@temp/>
<#else>
<button type="button" class="btn" data-view="input">${action.getText("edit")}</button>
</#if>
</td>
</#if>
</tr>
</#macro>

<#macro rtend buttons='' readonly=false createable=true celleditable=true deleteable=true searchable=false searchButtons='' showPageSize=true>
</tbody>
</table>
<div class="toolbar row-fluid">
<div class="pagination span4">
<#if resultPage??>
<ul>
<#if resultPage.first>
<li class="disabled"><a title="${action.getText('firstpage')}">&lt;&lt;</a></li>
<li class="disabled"><a title="${action.getText('previouspage')}">&lt;</a></li>
<#else>
<li><a class="firstPage" title="${action.getText('firstpage')}" href="${resultPage.renderUrl(1)}">&lt;&lt;</a></li>
<li><a class="prevPage" title="${action.getText('previouspage')}" href="${resultPage.renderUrl(resultPage.previousPage)}">&lt;</a></li>
</#if>
<#if resultPage.last>
<li class="disabled"><a title="${action.getText('nextpage')}">&gt;</a></li>
<li class="disabled"><a title="${action.getText('lastpage')}">&gt;&gt;</a></li>
<#else>
<li><a class="nextPage" title="${action.getText('nextpage')}" href="${resultPage.renderUrl(resultPage.nextPage)}">&gt;</a></li>
<li><a class="lastPage" title="${action.getText('lastpage')}" href="${resultPage.renderUrl(resultPage.totalPage)}">&gt;&gt;</a></li>
</#if>
<li>
<input type="text" name="resultPage.pageNo" value="${resultPage.pageNo}" class="inputPage"/><span>/</span><strong class="totalPage">${resultPage.totalPage}</strong>
<#if showPageSize>
<select name="resultPage.pageSize" class="pageSize">
<#local array=[5,10,20,50,100,500]>
<#list array as ps>
<option value="${ps}"<#if resultPage.pageSize==ps> selected</#if>>${ps}</option>
</#list> 
<option value="${resultPage.totalRecord}"<#if resultPage.pageSize==resultPage.totalRecord> selected</#if>>${action.getText('all')}</option>
</select>
</#if>
<#else>
</#if>
</li>
</ul>
</div>
<div class="action span4">
<#if buttons!=''>
<#local temp=buttons?interpret>
<@temp/>
<#else>
<#if !readonly>
<#if createable><button type="button" class="btn" data-view="input">${action.getText("create")}</button></#if>
<#if celleditable><button type="button" class="btn" data-action="save">${action.getText("save")}</button></#if>
<#if deleteable><button type="button" class="btn" data-action="delete">${action.getText("delete")}</button></#if>
</#if><button type="button" class="btn" data-action="reload">${action.getText("reload")}</button></#if>
</div>
<div class="search span2">
<#if searchable>
<div class="control-group">
    <div class="controls">
        <div class="input-append">
            <input type="text" name="keyword" value="${keyword!}" class="focus" placeholder="${action.getText('search')}"/><span class="add-on"><i class="icon-search"></i></span>
        </div>
    </div>
</div>
</#if>
<#if searchButtons!=''>
<#local temp=searchButtons?interpret>
<@temp/>
<#else>
</#if>
</div>
<div class="status span2">
<span>
<#if resultPage??>
${action.getText('total')}${resultPage.totalRecord}${action.getText('record')}<#if resultPage.totalRecord!=0>,${action.getText('display')}${resultPage.start+1}-${resultPage.start+resultPage.result?size}</#if>
<#else>
${action.getText('total')}${list?size}${action.getText('record')}<#if list?size!=0>,${action.getText('display')}1-${list?size}</#if>	
</#if>
</span>
</div>
</div>
</form>
</#macro>