<#--
/*
 * $Id: select.ftl,v 1.1 2013/09/23 16:25:43 npiedeloup Exp $
 *
 */
-->
<#if parameters.multiple?default(false) >
	<#assign value = parameters.nameValue />
<#elseif parameters.nameValue??>
	<#assign value = [parameters.nameValue] />
</#if>
<#if value?? && value?has_content>
	<#assign itemCount = 0/>
	<#list value as selectedValue>
		<#assign itemCount = itemCount + 1/>
<span<#rt/>
		<#if parameters.id??>
 id="${parameters.id?html}-${itemCount?html}"<#rt/>
		</#if>
		<#if parameters.title??>
 title="${parameters.title?html}"<#rt/>
		</#if>
		<#include "/${parameters.templateDir}/simple/css.ftl" /><#t/>
		<#include "/${parameters.templateDir}/simple/scripting-events.ftl" /><#t/>
		<#include "/${parameters.templateDir}/simple/common-attributes.ftl" /><#t/>
		<#include "/${parameters.templateDir}/simple/dynamic-attributes.ftl" /><#t/>
		><#t/>
		<#if parameters.headerKey?? && parameters.headerValue?? && tag.contains(parameters.nameValue, parameters.headerKey) == true>
		${parameters.headerValue?html}<#t/>
		<#else>
		<#if selectedValue?? && selectedValue!='' >
		<#if parameters.list.getById??>
		<#assign paramListKey = parameters.listKey!util.getIdField(parameters.list) />
		<#assign paramListValue = parameters.listValue!util.getDisplayField(parameters.list) />
		<#assign uiObject = parameters.list.getById(paramListKey, selectedValue) />
		<#if uiObject??>
		 ${uiObject[paramListValue]?html?replace("\n", "<br/>")}<#t/>
		</#if>
		<#else> <#-- si pas de getById : liste ou map brute -->
		<#list parameters.list as entry>
		<#if entry.key = selectedValue>
		 ${entry.value?html?replace("\n", "<br/>")}<#t/>
		</#if>
		</#list>
		</#if>
		</#if>
		</#if>
		</span><#t/>
		<br><#lt/>
	</#list>
</#if>