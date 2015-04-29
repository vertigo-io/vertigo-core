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
		<#if selectedValue?? && selectedValue!='' >
			<#if parameters.list.getById??>
				<#assign paramListKey = parameters.listKey!util.getIdField(parameters.list) />
				<#assign paramListValue = parameters.listValue!util.getDisplayField(parameters.list) />
				<#assign uiObject = parameters.list.getById(paramListKey, selectedValue) />
				<#assign itemCount = itemCount + 1/>
<span <#rt/>
				<#if parameters.id??>
 id="${parameters.id?html}-${itemCount?html}"<#rt/>
				</#if>
				<#if uiObject?? && parameters.multiple?default(false) && parameters.listTitle??>
 title="${uiObject.get(parameters.listTitle)?html}"<#rt/>
				<#elseif parameters.title??>
 title="${parameters.title?html}"<#rt/>
				</#if>
				<#assign previousCssClass = appendedCssClass?default('') />
				<#if uiObject?? && parameters.multiple?default(false) && parameters.listCssClass??>
					<#assign appendedCssClass = previousCssClass + " " + uiObject.get(parameters.listCssClass)?html />
				</#if>
				<#include "/${parameters.templateDir}/simple/css.ftl" /><#t/>
				<#include "/${parameters.templateDir}/simple/scripting-events.ftl" /><#t/>
				<#include "/${parameters.templateDir}/simple/common-attributes.ftl" /><#t/>
				<#include "/${parameters.templateDir}/simple/dynamic-attributes.ftl" /><#t/>
				<#assign appendedCssClass = previousCssClass/>
				><#t/>
				<#if parameters.headerKey?? && parameters.headerValue?? && tag.contains(parameters.nameValue, parameters.headerKey) == true>
					${parameters.headerValue?html}<#t/>
				<#else>
					<#if uiObject??>
					 	${uiObject[paramListValue]?html?replace("\n", "<br/>")}<#t/>
					<#else> <#-- si pas de getById : liste ou map brute -->
						<#list parameters.list as entry>
							<#if entry.key = selectedValue>
							 	${entry.value?html?replace("\n", "<br/>")}<#t/>
							</#if>
						</#list>
					</#if>
				</#if>
				</span><#t/>
				<br><#lt/>
			</#if>
		</#if>
	</#list>
</#if>
