<#--
/*
 * $Id: password.ftl,v 1.2 2014/01/17 09:33:28 npiedeloup Exp $
 *
 */
-->
<span<#rt/>
<#if parameters.id??>
 id="${parameters.id?html}"<#rt/>
</#if>
<#include "/${parameters.templateDir}/simple/css.ftl" />
<#if parameters.title??>
 title="${parameters.title?html}"<#rt/>
</#if>
<#include "/${parameters.templateDir}/simple/scripting-events.ftl" />
<#include "/${parameters.templateDir}/simple/common-attributes.ftl" />
<#include "/${parameters.templateDir}/simple/dynamic-attributes.ftl" />
>
<#if parameters.nameValue??>
 ${parameters.nameValue?replace('.*', '*', 'r')}
</#if>
</span>