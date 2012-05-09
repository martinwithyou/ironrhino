<#macro richtable columns entityName formid='' action='' actionColumnWidth='60px' actionColumnButtons='' bottomButtons='' resizable=true sortable=true readonly=false createable=true celleditable=true deleteable=true searchable=false searchButtons='' includeParameters=true showPageSize=true showCheckColumn=true multipleCheck=true columnfilterable=true>
<@rtstart formid=formid action=action?has_content?string(action,request.requestURI?substring(request.contextPath?length)) entityName=entityName readonly=readonly resizable=resizable sortable=sortable includeParameters=includeParameters showCheckColumn=showCheckColumn multipleCheck=multipleCheck columnfilterable=columnfilterable/>
<#list columns?keys as name>
<#local cellName=((columns[name]['trimPrefix']??)?string('',entityName+'.'))+name>
<@rttheadtd name=name class=columns[name]['class']! width=columns[name]['width']! cellName=cellName cellEdit=columns[name]['cellEdit'] readonly=readonly resizable=resizable excludeIfNotEdited=columns[name]['excludeIfNotEdited']!false/>
</#list>
<@rtmiddle width=actionColumnWidth readonly=readonly/>
<#local index=0>
<#if resultPage??><#local list=resultPage.result></#if>
<#list list as entity>
<#local index=index+1>
<@rttbodytrstart entity=entity odd=(index%2==1) readonly=readonly showCheckColumn=showCheckColumn multipleCheck=multipleCheck/>
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
<form id="<#if formid!=''>${formid}<#else>${entityName}_form</#if>" action="${getUrl(action)}" method="post" class="richtable ajax view" <#if entityName!=action&&entityName!=''> entity="${entityName}"</#if>>
<#if includeParameters>
<#list Parameters?keys as name>
<#if name!='_'&&name!='pn'&&name!='ps'&&!name?starts_with('resultPage.')&&name!='keyword'&&name!='check'>
<input type="hidden" name="${name}" value="${Parameters[name]}" />
</#if>
</#list>
</#if>
<table class="richtable<#if sortable> sortable</#if><#if columnfilterable> filtercolumn</#if> highlightrow<#if resizable> resizable</#if>"<#if resizable> minColWidth="40"</#if>>
<thead>
<tr>
<#if showCheckColumn>
<td class="nosort" width="30px"><#if multipleCheck><input type="checkbox" class="checkbox"/></#if></td>
</#if>
</#macro>

<#macro rttheadtd name,cellName='',cellEdit='',class='',width='',readonly=false,resizable=true,excludeIfNotEdited=false>
<td class="tableHeader<#if excludeIfNotEdited> excludeIfNotEdited</#if><#if class!=''> ${class}</#if>"<#if width!=''> width="${width}"</#if><#if !readonly> cellName="${cellName}"</#if><#if cellEdit!=''> cellEdit="${cellEdit}"</#if>>
<#if resizable>
<span class="resizeTitle">${action.getText(name)}</span>
<span class="resizeBar"></span>
<#else>
${action.getText(name)}
</#if>
</td>
</#macro>
<#macro rtmiddle width='60px' readonly=false>
<#if !readonly>
<td class="nosort" width="${width}"></td>
</#if>
</tr>
</thead>
<tbody>
</#macro>

<#macro rttbodytrstart entity odd readonly=false showCheckColumn=true multipleCheck=true>
<tr class="${odd?string('odd','even')}"<#if !showCheckColumn&&entity.id??> rowid="${entity.id?string}"</#if>>
<#if showCheckColumn><td class="<#if multipleCheck>checkbox<#else>radio</#if>"><input type="<#if multipleCheck>checkbox<#else>radio</#if>" name="check"<#if entity.id??> value="${entity.id?string}"</#if>/></td></#if>
</#macro>

<#macro rttbodytd value,entity,celleditable=true,template=''>
<td<#if celleditable><#if value??><#if value?is_boolean> cellValue="${value?string}"</#if><#if value?is_hash&&value.displayName??> cellValue="${value.name()}"</#if></#if></#if>><#rt>
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
<@button text=action.getText('edit') view='input'/>
</#if>
</td>
</#if>
</tr>
</#macro>

<#macro rtend buttons='' readonly=false createable=true celleditable=true deleteable=true searchable=false searchButtons='' showPageSize=true>
</tbody>
</table>
<div class="toolbar clearfix">
<div class="pagination">
<#if resultPage??>
<#if resultPage.first>
<span class="disabled" title="${action.getText('firstpage')}">&lt;&lt;</span>
<span class="disabled" title="${action.getText('previouspage')}">&lt;</span>
<#else>
<a class="firstPage" title="${action.getText('firstpage')}" href="${resultPage.renderUrl(1)}">&lt;&lt;</a>
<a class="prevPage" title="${action.getText('previouspage')}" href="${resultPage.renderUrl(resultPage.previousPage)}">&lt;</a>
</#if>
<#if resultPage.last>
<span class="disabled" title="${action.getText('nextpage')}">&gt;</span>
<span class="disabled" title="${action.getText('lastpage')}">&gt;&gt;</span>
<#else>
<a class="nextPage" title="${action.getText('nextpage')}" href="${resultPage.renderUrl(resultPage.nextPage)}">&gt;</a>
<a class="lastPage" title="${action.getText('lastpage')}" href="${resultPage.renderUrl(resultPage.totalPage)}">&gt;&gt;</a>
</#if>
<input type="text" name="resultPage.pageNo" value="${resultPage.pageNo}" class="inputPage"/>/<span class="totalPage">${resultPage.totalPage}</span>${action.getText('page')}
<#if showPageSize>
<span>${action.getText('pagesize')}</span><select name="resultPage.pageSize" class="pageSize">
<#local array=[5,10,20,50,100,500]>
<#list array as ps>
<option value="${ps}" <#if resultPage.pageSize==ps>selected</#if>>${ps}</option>
</#list> 
<option value="${resultPage.totalRecord}">${action.getText('all')}</option>
</select><span>${action.getText('row')}</span>
</#if>
</#if>
</div>
<div class="action">
<#if buttons!=''>
<#local temp=buttons?interpret>
<@temp/>
<#else>
<#if !readonly>
<#if createable><@button text=action.getText('create') view='input'/></#if>
<#if celleditable><@button text=action.getText('save') action='save'/></#if>
<#if deleteable><@button text=action.getText('delete') action='delete'/></#if>
</#if><@button text=action.getText('reload') action='reload'/></#if>
</div>
<div class="search">
<#if searchable>
<@s.textfield theme="simple" name="keyword" cssClass="focus" size="15"/><@s.submit theme="simple" value="%{getText('search')}" />
</#if>
<#if searchButtons!=''>
<#local temp=searchButtons?interpret>
<@temp/>
</#if>
</div>
<div class="status">
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