<#--
/*
 * autocompleter.ftl simple ReadOnly
 */
-->

<#assign uiList = stack.findValue(parameters.remoteList) />
<#if parameters.nameValue?? && parameters.nameValue!=''>
	<#assign uiObject = uiList.getById(parameters.remoteListKey, parameters.nameValue) />
</#if>
<span<#rt/>
<#if parameters.id??>
 id="${parameters.id?html}"<#rt/>
</#if>
<#if parameters.title??>
 title="${parameters.title?html}"<#rt/>
</#if>
<#include "/${parameters.templateDir}/simple/css.ftl" />
<#include "/${parameters.templateDir}/simple/scripting-events.ftl" />
<#include "/${parameters.templateDir}/simple/common-attributes.ftl" />
<#include "/${parameters.templateDir}/simple/dynamic-attributes.ftl" />
><#t/>
<#if parameters.headerKey?? && parameters.headerValue?? && tag.contains(parameters.nameValue, parameters.headerKey) == true>
${parameters.headerValue?html}<#t/>
</#if>
<#if uiObject??>
 ${uiObject[parameters.remoteListValue]?html?replace("\n", "<br/>")}<#rt/>
</#if>
</span><#t/>