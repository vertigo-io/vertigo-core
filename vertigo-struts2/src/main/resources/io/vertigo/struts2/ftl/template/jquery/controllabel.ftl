<#--
/*
 * $Id: controllabel.ftl,v 1.1 2013/09/23 16:26:16 npiedeloup Exp $
 * Label pour les controls.
 */
-->
<#assign fieldName = parameters.widgetname!parameters.name!""/><#-- for jquery component -->
<#assign hasFieldErrors = parameters.name?? && fieldErrors?? && fieldErrors[fieldName]??/>
<label <#t/>
<#if parameters.id??>
	for="${parameters.id?html}" <#t/>
</#if>
<#if hasFieldErrors>
	class="errorLabel" <#t/>
</#if>
><#t/>
<#if parameters.label = "default">    
	${util.label(fieldName)?html}<#t/>
<#else>
	${parameters.label?html}<#t/>
</#if>
<#if parameters.required??>
	<#if parameters.required>
 		<em class="required">*</em><#t/>
	</#if>	
<#else>
	<#if util.required(fieldName) >
		<em class="required">*</em><#t/>
	</#if>
</#if>
<#include "/${parameters.templateDir}/xhtml/tooltip.ftl" /> 
</label><#t/>