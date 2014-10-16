<#--
/*
 * $Id: form-common.ftl,v 1.1 2014/03/18 11:08:57 npiedeloup Exp $
 */
-->
<#if (parameters.validate?default(false) == false)><#rt/>
    <#if parameters.onsubmit??><#rt/>
        ${tag.addParameter('onsubmit', "${parameters.onsubmit}") }
    </#if>
</#if>
<form<#rt/>
<#if parameters.id??>
 id="${parameters.id?html}"<#rt/>
</#if>
<#if parameters.name??>
 name="${parameters.name?html}"<#rt/>
</#if>
<#if parameters.onsubmit??>
 onsubmit="${parameters.onsubmit?html}"<#rt/>
</#if>
<#if parameters.onreset??>
 onreset="${parameters.onreset?html}"<#rt/>
</#if>
<#if parameters.action??>
 action="${parameters.action?html}"<#rt/>
</#if>
<#if parameters.target??>
 target="${parameters.target?html}"<#rt/>
</#if>
<#if parameters.method??>
 method="${parameters.method?html}"<#rt/>
<#else>
 method="post"<#rt/>
</#if>
<#if parameters.enctype??>
 enctype="${parameters.enctype?html}"<#rt/>
</#if>
class="form-inline 
<#if parameters.cssClass??>
 ${parameters.cssClass?html}
 </#if>
 "<#rt/>
<#if parameters.cssStyle??>
 style="${parameters.cssStyle?html}"<#rt/>
</#if>
<#if parameters.title??>
 title="${parameters.title?html}"<#rt/>
</#if>
<#if parameters.acceptcharset??>
 accept-charset="${parameters.acceptcharset?html}"<#rt/>
</#if>
<#include "/${parameters.templateDir}/simple/dynamic-attributes.ftl" />