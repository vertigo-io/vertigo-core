${parameters.after?if_exists}<#t/>
<#assign currentLayout = controlLayout_type?default('none') />
<#if currentLayout = 'table'>
	<#if (parameters.unit)??><#t/>
		<span>
			${parameters.unit?html}<#t/>
		</span>
	</#if>
	</td><#lt/>
	<#-- Write out the closing td for the html input -->
	<#include "/${parameters.templateDir}/xhtml/controlfooter-trlogic.ftl" />
</#if>
