<#--
/*
 * $Id: label.ftl,v 1.2 2014/01/15 15:32:30 npiedeloup Exp $
 */
-->
<#include "/${parameters.templateDir}/xhtml/controlheader-core.ftl" />
${parameters.after?if_exists}<#t/>
<#assign currentLayout = controlLayout_type?default('none') />
<#if currentLayout = 'table'>	
<#include "/${parameters.templateDir}/xhtml/controlfooter-trlogic.ftl" />
</#if>
