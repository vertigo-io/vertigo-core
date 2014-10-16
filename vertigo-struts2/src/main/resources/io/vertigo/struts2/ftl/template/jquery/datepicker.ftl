<#--
/*
 * $Id: datepicker.ftl,v 1.2 2014/01/15 15:32:30 npiedeloup Exp $
 */
-->
<#if parameters.parentTheme = 'xhtml_read'>
   <#include "/${parameters.templateDir}/xhtml_read/datepicker.ftl" />
<#else>
	<#include "/${parameters.templateDir}/xhtml/controlheader.ftl" />
	<#if parameters.inline?default(false)>
		<#include "/${parameters.templateDir}/simple/hidden.ftl" />
		<div id="${parameters.id?html}_inline"
		<#if parameters.cssStyle?if_exists != "">
		 style="${parameters.cssStyle?html}"<#rt/>
		</#if>
		<#if parameters.cssClass?if_exists != "">
		 class="${parameters.cssClass?html}"<#rt/>
		</#if>
		<#include "/${parameters.templateDir}/simple/scripting-events.ftl" />
		<#include "/${parameters.templateDir}/simple/common-attributes.ftl" />
		<#include "/${parameters.templateDir}/simple/dynamic-attributes.ftl" />
		>
		</div>
	<#else>
		<#include "/${parameters.templateDir}/simple/text.ftl" />
	</#if>
	<#include "/${parameters.templateDir}/xhtml/controlfooter.ftl" />
</#if>
