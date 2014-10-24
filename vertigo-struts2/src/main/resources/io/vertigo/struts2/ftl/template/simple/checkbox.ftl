<#--
/*
 * $Id: checkbox.ftl,v 1.2 2014/03/18 10:52:48 npiedeloup Exp $
 */
-->
<#assign hiddenPrefix = '__checkbox_'>
<input type="checkbox" name="${parameters.name?html}" value="${parameters.fieldValue?html}"<#rt/>
<#assign itemValue = stack.findValue(parameters.name)?default('')/>
<#if itemValue?is_enumerable && parameters.fieldValue?? >
	<#assign hiddenPrefix = '__multiselect_'>
	<#if itemValue?seq_contains(parameters.fieldValue?html) >
 checked="checked"<#rt/>
 	</#if>
<#else>
	<#if parameters.nameValue?? && parameters.nameValue>
 checked="checked"<#rt/>
	</#if>
</#if>
<#if parameters.disabled?default(false)>
 disabled="disabled"<#rt/>
</#if>
<#if parameters.readonly?default(false)>
 readonly="readonly"<#rt/>
</#if>
<#if parameters.tabindex??>
 tabindex="${parameters.tabindex?html}"<#rt/>
</#if>
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
/><#t/>
<input type="hidden" id="${hiddenPrefix}${parameters.id?html}" name="${hiddenPrefix}${parameters.name?html}" value="${parameters.fieldValue?html}"<#rt/>
<#if parameters.disabled?default(false)>
 disabled="disabled"<#rt/>
</#if>
/><#t/>