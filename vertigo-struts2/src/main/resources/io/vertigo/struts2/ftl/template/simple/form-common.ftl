<#--
/*
 * $Id: form-common.ftl,v 1.1 2014/03/18 11:08:57 npiedeloup Exp $
 */
-->
<#if (parameters.validate!false == false)>
    <#if parameters.onsubmit?has_content>
        ${tag.addParameter('onsubmit', "${parameters.onsubmit}") }
    </#if>
</#if>
<form<#rt/>
<#if parameters.id?has_content>
 id="${parameters.id?html}"<#rt/>
</#if>
<#if parameters.name?has_content>
 name="${parameters.name?html}"<#rt/>
</#if>
<#if parameters.onsubmit?has_content>
 onsubmit="${parameters.onsubmit?html}"<#rt/>
</#if>
<#if parameters.onreset?has_content>
 onreset="${parameters.onreset?html}"<#rt/>
</#if>
<#if parameters.action?has_content>
 action="${parameters.action?html}"<#rt/>
</#if>
<#if parameters.target?has_content>
 target="${parameters.target?html}"<#rt/>
</#if>
<#if parameters.method?has_content>
 method="${parameters.method?html}"<#rt/>
<#else>
 method="post"<#rt/>
</#if>
<#if parameters.enctype?has_content>
 enctype="${parameters.enctype?html}"<#rt/>
</#if>
 class="form-inline<#rt/>
<#if parameters.cssClass?has_content>
 ${parameters.cssClass?html}<#rt/>
</#if>
"<#rt/>
<#if parameters.cssStyle?has_content>
 style="${parameters.cssStyle?html}"<#rt/>
</#if>
<#if parameters.title?has_content>
 title="${parameters.title?html}"<#rt/>
</#if>
<#if parameters.acceptcharset?has_content>
 accept-charset="${parameters.acceptcharset?html}"<#rt/>
</#if>
<#include "/${parameters.templateDir}/simple/dynamic-attributes.ftl" />