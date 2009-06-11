<#if !(parameters.type?? && parameters.type=="image")>
${parameters.body}
<span><span><#if (parameters.nameValue)?default("")?length gt 0>${parameters.nameValue}<#elseif (parameters.body)?default("")?length gt 0>${parameters.body}<#elseif parameters.label??><@s.property value="parameters.label"/></#if></span></span></button>
<#else>
${parameters.body}<#rt/>
</#if>