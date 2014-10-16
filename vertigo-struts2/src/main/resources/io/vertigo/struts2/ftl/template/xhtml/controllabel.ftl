<#--
/*
 * $Id: controllabel.ftl,v 1.1 2013/09/23 16:25:43 npiedeloup Exp $
 * Label pour les controls.
 */
-->

<#assign hasFieldErrors = parameters.name?? && fieldErrors?? && fieldErrors[parameters.name]??/>
<label <#t/>
<#if parameters.id??>
	for="${parameters.id?html}" <#t/>
</#if>
<#if hasFieldErrors>
	class="errorLabel" <#t/>
</#if>
><#t/>
<#if parameters.label = "default">    
	${util.label(parameters.name)?html}<#t/>
<#else>
	${parameters.label?html}<#t/>
</#if>
<#if parameters.required??>
	<#if parameters.required>
 		<em class="required">*</em><#t/>
	</#if>	
<#else>
	<#if parameters.nameValue?is_boolean && util.required(parameters.name) >
		<em class="required">*</em><#t/>
	</#if>
</#if>
<#include "/${parameters.templateDir}/${parameters.theme}/tooltip.ftl" /> 
</label><#t/>