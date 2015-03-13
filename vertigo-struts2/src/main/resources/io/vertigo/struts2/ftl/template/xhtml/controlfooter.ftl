${parameters.dynamicAttributes.after?if_exists?html}<#t/>
<#assign currentLayout = controlLayout_type?default('none') />
<#if currentLayout = 'table'>
	</td><#lt/>
	<#-- Write out the closing td for the html input -->
<#include "/${parameters.templateDir}/xhtml/controlfooter-trlogic.ftl" />
</#if>