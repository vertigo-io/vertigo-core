<#--
/*
 * $Id: reset.ftl,v 1.1 2013/09/23 16:25:43 npiedeloup Exp $
 *
 */
-->
<#assign currentLayout = controlLayout_type?default('none') />	
<#if currentLayout = 'table'>
	<#include "/${parameters.templateDir}/${parameters.theme}/controlheader-trlogic.ftl" />
			<td <#t/>
	<#if parameters.submitcolspan??><#t/>
	    colspan="${parameters.submitcolspan?html}"<#t/>	    
	<#t/></#if>
	<#if parameters.align??><#t/>
	    align="${parameters.align?html}"<#t/>
	<#t/></#if>
	><#t/>
	<#if controlLayout_tablecolspan?exists >
    	<#assign columnCount = controlLayout_currentColumnCount + parameters.submitcolspan?default(1) />	
		<#-- update the value of the controlLayout_currentColumnCount bean on the value stack. -->
		${stack.setValue('#controlLayout_currentColumnCount', columnCount)}<#t/>
	</#if>
</#if>
<#include "/${parameters.templateDir}/simple/reset.ftl" />
<#include "/${parameters.templateDir}/${parameters.theme}/controlfooter.ftl" />
