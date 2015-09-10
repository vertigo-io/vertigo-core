<#--
/*
 * $Id: div.ftl,v 1.4 2014/03/18 10:52:48 npiedeloup Exp $
 *
 */
-->
<#assign currentLayout = controlLayout_type?default('none') />	
<#if currentLayout = 'table'>
	<#include "/${parameters.templateDir}/xhtml/controlheader-trlogic.ftl" />
		<td <#rt/>
	<#if parameters.dynamicAttributes['colspan']??>
	    colspan="${parameters.dynamicAttributes['colspan']?html}"<#t/>	    
	</#if>
	<#if parameters.align??>
	    align="${parameters.align?html}"<#t/>
	</#if>
	><#lt/>
	<#if controlLayout_tablecolspan?exists >
    		<#assign columnCount = controlLayout_currentColumnCount + parameters.dynamicAttributes['colspan']?default(1)?number />	
		<#-- update the value of the controlLayout_currentColumnCount bean on the value stack. -->
		${stack.setValue('#controlLayout_currentColumnCount', columnCount)}<#t/>
	</#if>
</#if>
<#include "/${parameters.templateDir}/xhtml/pushLayoutType.ftl" />
<#if parameters.dynamicAttributes['layout']??>
	<#if parameters.dynamicAttributes['layout'] = 'table' > 
<#assign tablecolspan = parameters.dynamicAttributes['cols']?default(2)?number />
<table class="grid"<#rt/>
<#if parameters.id??> id="${parameters.id?html}"</#if><#rt/>
<#if parameters.name??> name="${parameters.name?html}"</#if><#rt/>
<#if parameters.cssClass??> class="${parameters.cssClass?default('wwFormTable')?html}"</#if><#rt/>
<#if parameters.cssStyle??> style="${parameters.cssStyle?html}"</#if><#rt/>
<#if parameters.title??> title="${parameters.title?html}"</#if><#rt/>
<#include "/${parameters.templateDir}/simple/scripting-events.ftl" />
<#include "/${parameters.templateDir}/simple/common-attributes.ftl" />
<#include "/${parameters.templateDir}/simple/dynamic-attributes.ftl" />
><#t/>
${stack.setValue('#controlLayout_type', 'table')}<#t/>
${stack.setValue('#controlLayout_currentColumnCount', 0)}<#t/>
${stack.setValue('#controlLayout_tablecolspan', tablecolspan)}<#t/>
<#elseif parameters.dynamicAttributes['layout'] = 'none'>
${stack.setValue('#controlLayout_type', 'none')}<#t/>
	<#-- none --><#t/>
	</#if><#-- layout == 'table' -->
<#else><#-- layout??--><#t/>
<div<#rt/>
<#if parameters.id??> id="${parameters.id?html}"</#if><#rt/>
<#if parameters.name??> name="${parameters.name?html}"</#if><#rt/>
<#if parameters.cssClass??> class="${parameters.cssClass?html}"</#if><#rt/>
<#if parameters.cssStyle??> style="${parameters.cssStyle?html}"</#if><#rt/>
<#if parameters.title??> title="${parameters.title?html}"</#if><#rt/>
<#include "/${parameters.templateDir}/simple/scripting-events.ftl" />
<#include "/${parameters.templateDir}/simple/common-attributes.ftl" />
<#include "/${parameters.templateDir}/simple/dynamic-attributes.ftl" />
><#t/>
${stack.setValue('#controlLayout_type', 'div')}<#t/>
</#if>