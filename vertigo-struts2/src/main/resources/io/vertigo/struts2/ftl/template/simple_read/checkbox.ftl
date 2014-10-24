<#--
/*
 * $Id: checkbox.ftl,v 1.1 2013/09/23 16:25:43 npiedeloup Exp $
 *
 */
-->

<span<#t/>
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
"><#rt/>
<#assign itemValue = stack.findValue(parameters.name)?default('')/>
<#if itemValue?is_enumerable && parameters.fieldValue?? >
	${util.formatBoolean(parameters.name,itemValue?seq_contains(parameters.fieldValue?html)!false)}<#t/>	
<#else>
	${util.formatBoolean(parameters.name,parameters.nameValue!false)}<#t/>
</#if>
</span><#t/>
