<#--
/*
 * $Id: checkbox.ftl,v 1.1 2013/09/23 16:25:43 npiedeloup Exp $
 *
 */
-->
<#assign currentLayout = controlLayout_type?default('none') />	
<#if !parameters.labelposition?? && (parameters.form.labelposition)??>
<#assign labelpos = parameters.form.labelposition/>
<#else>
<#assign labelpos = parameters.labelposition?default("right")/>
</#if>
<#if labelpos = 'left'>
	<#include "/${parameters.templateDir}/xhtml/controlheader.ftl" />
	<#include "/${parameters.templateDir}/simple_read/checkbox.ftl" />
	<#include "/${parameters.templateDir}/xhtml/controlfooter.ftl" />  
<#elseif labelpos = 'top' && parameters.label??>
	<#if currentLayout = 'table'>
	<tr>
		<#assign tablecolspan = controlLayout_tablecolspan />
	    <th colspan="${parameters.tablecolspan?html}"><#t/>
		<#include "/${parameters.templateDir}/${parameters.theme}/controllabel.ftl" /> 
	    </th>
	</tr>
	<tr>
	   <td <#t/>
			<#if parameters.inputcolspan??><#t/>
			    colspan="${parameters.inputcolspan?html}"<#t/>	    
			<#t/></#if>
			<#if parameters.align??><#t/>
			    align="${parameters.align?html}"<#t/>
			<#t/></#if>
			><#t/>
	        <#include "/${parameters.templateDir}/simple_read/checkbox.ftl" />
	<#else>
		<#include "/${parameters.templateDir}/${parameters.theme}/controllabel.ftl" />
		<#if parameters.label??><br/></#if>
		<#include "/${parameters.templateDir}/simple_read/checkbox.ftl" />
	</#if>
	<#include "/${parameters.templateDir}/xhtml/controlfooter.ftl" />
<#elseif labelpos == 'right'>
	<#if currentLayout = 'table'>
		<#include "/${parameters.templateDir}/xhtml/controlheader-trlogic.ftl" />	
		<td class="checkBoxLeft"<#t/>
		<#if parameters.inputcolspan??><#t/>
		    colspan="${parameters.inputcolspan?html}"<#t/>	    
		<#t/></#if>
		<#if parameters.align??><#t/>
		    align="${parameters.align?html}"<#t/>
		<#t/></#if>
		><#t/>
		<#include "/${parameters.templateDir}/simple_read/checkbox.ftl" />
		${parameters.after?if_exists}<#t/>
		</td><#lt/>
		<th class="checkBoxLabelRight" <#t/>
	    <#if parameters.labelcolspan??>
		    colspan="${parameters.labelcolspan?html}" <#t/>
		</#if>
		><#t/>
		<#if parameters.label??>
			<#include "/${parameters.templateDir}/${parameters.theme}/controllabel.ftl" /> 
		</#if>
		</th><#lt/>
		<#assign columnCount = controlLayout_currentColumnCount + parameters.labelcolspan?default(1) + parameters.inputcolspan?default(1) />	
		${stack.setValue('#controlLayout_currentColumnCount', columnCount)}
		<#include "/${parameters.templateDir}/xhtml/controlfooter-trlogic.ftl" />
	<#else>
		<#include "/${parameters.templateDir}/simple_read/checkbox.ftl" /> 
		<#include "/${parameters.templateDir}/${parameters.theme}/controllabel.ftl" />
	</#if>
</#if>
		
			

	
