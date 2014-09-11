<#--
/*
 * $Id: checkbox.ftl,v 1.1 2013/09/23 16:25:43 npiedeloup Exp $
 *
 */
-->

<span
<#if parameters.id??>
 id="${parameters.id?html}"<#rt/>
</#if>
<#if parameters.nameValue?? && parameters.nameValue>
 class="checkbox-checked<#rt/>
<#else>
 class="checkbox-unchecked<#rt/>
</#if>
<#if parameters.cssClass?? >
 ${parameters.cssClass?html}<#rt/>
</#if>
">${util.formatBoolean(parameters.name,parameters.nameValue!false)}<#rt/>
</span><#rt/>
