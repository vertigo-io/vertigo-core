<#--
/*
 * Override tag a to support insert into <s:div layout="table">
 */
-->
<#assign currentLayout = controlLayout_type?default('none') />	
<#if currentLayout = 'table'>
	<#include "/${parameters.templateDir}/xhtml/controlheader-trlogic.ftl" />
		<td <#rt/>
	<#if parameters.inputcolspan??>
	    colspan="${parameters.inputcolspan?html}"<#t/>	    
	</#if>
	<#if parameters.align??>
	    align="${parameters.align?html}"<#t/>
	</#if>
	><#lt/>
	<#if controlLayout_tablecolspan?exists >
    		<#assign columnCount = controlLayout_currentColumnCount + parameters.inputcolspan?default(1) />	
		<#-- update the value of the controlLayout_currentColumnCount bean on the value stack. -->
		${stack.setValue('#controlLayout_currentColumnCount', columnCount)}<#t/>
	</#if>
</#if>
<#include "/${parameters.templateDir}/xhtml/pushLayoutType.ftl" />
${stack.setValue('#controlLayout_type', 'none')}<#t/>
