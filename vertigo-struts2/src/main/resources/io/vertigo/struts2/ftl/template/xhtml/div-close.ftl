<#--
/*
 * $Id: div-close.ftl,v 1.4 2014/03/18 10:52:48 npiedeloup Exp $
 *
 */
-->
<#if parameters.dynamicAttributes['layout']??>
	<#if parameters.dynamicAttributes['layout'] = "table" > 
	</table><#lt/>
	<#else>
	<#-- none -->
	</#if>
<#else>
</div>
</#if>
${stack.setValue('#controlLayout_type', controlLayout_previoustype?default('none'))}
${stack.setValue('#controlLayout_currentColumnCount', controlLayout_previousColumnCount?default(0))}
<#assign currentLayout = controlLayout_type?default('none') />
<#if currentLayout = 'table'>
	</td><#lt/>
	<#-- Write out the closing td for the html input -->
	<#include "/${parameters.templateDir}/xhtml/controlfooter-trlogic.ftl" />
</#if>