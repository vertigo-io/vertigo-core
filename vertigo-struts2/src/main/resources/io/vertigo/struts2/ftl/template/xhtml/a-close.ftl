<#--
/*
 * Override tag a to support insert into <s:div layout="table">
 */
-->
<a<#rt/>
<#if parameters.id?if_exists != "">
 id="${parameters.id?html}"<#rt/>
</#if>
<#if parameters.href?if_exists != "">
 href="${parameters.href}"<#rt/>
</#if>
<#if parameters.tabindex??>
 tabindex="${parameters.tabindex?html}"<#rt/>
</#if>
<#if parameters.cssClass??>
 class="${parameters.cssClass?html}"<#rt/>
</#if>
<#if parameters.cssStyle??>
 style="${parameters.cssStyle?html}"<#rt/>
</#if>
<#if parameters.title??>
 title="${parameters.title?html}"<#rt/>
</#if>
<#include "/${parameters.templateDir}/${parameters.expandTheme}/scripting-events.ftl" />
<#include "/${parameters.templateDir}/${parameters.expandTheme}/common-attributes.ftl" />
<#include "/${parameters.templateDir}/${parameters.expandTheme}/dynamic-attributes.ftl" />
>${parameters.body}</a>

<#include "/${parameters.templateDir}/xhtml/popLayoutType.ftl" />

<#assign currentLayout = controlLayout_type?default('none') />
<#if currentLayout = 'table'>
	</td><#lt/>
	<#-- Write out the closing td for the html input -->
	<#include "/${parameters.templateDir}/xhtml/controlfooter-trlogic.ftl" />
</#if>