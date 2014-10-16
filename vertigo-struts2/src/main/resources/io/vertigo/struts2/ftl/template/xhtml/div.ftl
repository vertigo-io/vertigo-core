<#--
/*
 * $Id: div.ftl,v 1.4 2014/03/18 10:52:48 npiedeloup Exp $
 *
 */
-->

${stack.setValue('#controlLayout_previoustype', controlLayout_type?default('none'))}
${stack.setValue('#controlLayout_previousColumnCount', controlLayout_currentColumnCount?default(0))}
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
>
${stack.setValue('#controlLayout_type', 'table')}
${stack.setValue('#controlLayout_currentColumnCount', 0)}
${stack.setValue('#controlLayout_tablecolspan', tablecolspan)}
	<#elseif parameters.dynamicAttributes['layout'] = 'none'>
	${stack.setValue('#controlLayout_type', 'none')}
	<#-- none -->
	</#if> <#-- layout == 'table' -->
<#else> <#-- layout??-->
<div<#rt/>
<#if parameters.id??> id="${parameters.id?html}"</#if><#rt/>
<#if parameters.name??> name="${parameters.name?html}"</#if><#rt/>
<#if parameters.cssClass??> class="${parameters.cssClass?html}"</#if><#rt/>
<#if parameters.cssStyle??> style="${parameters.cssStyle?html}"</#if><#rt/>
<#if parameters.title??> title="${parameters.title?html}"</#if><#rt/>
<#include "/${parameters.templateDir}/simple/scripting-events.ftl" />
<#include "/${parameters.templateDir}/simple/common-attributes.ftl" />
<#include "/${parameters.templateDir}/simple/dynamic-attributes.ftl" />
>
${stack.setValue('#controlLayout_type', 'div')}
</#if>
