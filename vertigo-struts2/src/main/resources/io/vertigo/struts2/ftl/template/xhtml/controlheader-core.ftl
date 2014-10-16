<#--
	This template does not support the label top position!!!

	This template handles: 
		* outputting a <th> for the label if there is one.

	Additionally it calls controlerheader-trlogic.ftl to handle table row logic.
-->

<#assign currentLayout = controlLayout_type?default('none') />	
<#if currentLayout = 'table'>
	<#include "/${parameters.templateDir}/xhtml/controlheader-trlogic.ftl" /> 
    <th class="tdLabel" <#t/>
    <#if parameters.labelcolspan??>
	    colspan="${parameters.labelcolspan?html}" <#t/>
	</#if>
	><#t/>
</#if>
<#if parameters.label??>
	<#include "/${parameters.templateDir}/${parameters.theme}/controllabel.ftl" />
</#if>
<#if currentLayout = 'table'>
	</th><#lt/>
	<#-- We only update the controlLayout_currentColumnCount if we actually printed out a th for the lable. -->
	<#assign columnCount = controlLayout_currentColumnCount + parameters.labelcolspan?default(1) />	
	<#-- update the value of the controlLayout_currentColumnCount bean on the value stack. -->
	${stack.setValue('#controlLayout_currentColumnCount', columnCount)}
</#if>
    

