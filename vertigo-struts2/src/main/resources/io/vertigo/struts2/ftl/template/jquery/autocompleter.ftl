<#--
/*
 * $Id: autocompleter.ftl,v 1.4 2014/02/26 17:49:02 npiedeloup Exp $
 */
-->
<#if parameters.parentTheme == 'xhtml_read' || parameters.parentTheme == 'simple_read'>
  <#include "/${parameters.templateDir}/${parameters.parentTheme}/autocompleter.ftl" />
<#else>
	<#if (parameters.parentTheme == 'xhtml')>
		<#include "/${parameters.templateDir}/xhtml/controlheader.ftl" />
	</#if>
	<input type="hidden"<#rt/>
	  <#if parameters.widgetid?has_content>
	    id="${parameters.widgetid?html}"<#rt/>
	  </#if>
	  <#if parameters.nameValue?has_content>
	    value="${parameters.nameValue?html}"<#rt/>
	  </#if>
	  <#if parameters.widgetname?has_content>
	 	name="${parameters.widgetname?html}"<#rt/>
	  </#if>
	  <#if parameters.disabled!false>
	    disabled="disabled"<#rt/>
	  </#if>
	  <#if parameters.popinURL?has_content>
	 	popinURL="${parameters.popinURL?html}"<#rt/>
	  </#if>
	/><#rt/>
	<#include "/${parameters.templateDir}/jquery/autocomplete-simple-text.ftl" />
	<#assign escapedIconId="icon_${parameters.id?string?replace('.', '_')}">
	<div id="${escapedIconId?html}" class="autocompleter-icon" alt="chercher" />
        <#if (parameters.parentTheme == 'xhtml')>
		<#include "/${parameters.templateDir}/xhtml/controlfooter.ftl" />
	</#if>	
</#if>