<#if !parameters.type?? || parameters.type!="image">
<#if (parameters.nameValue)?default("")?length gt 0><span><span>${parameters.nameValue}</span></span><#elseif parameters.label??><@s.property value="parameters.label"/><#rt/></#if>
</button>
<#else>
${parameters.body}<#rt/>
</#if>
