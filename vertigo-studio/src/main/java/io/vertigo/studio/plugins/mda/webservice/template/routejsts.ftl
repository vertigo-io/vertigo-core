/*
    Ce fichier a été généré automatiquement.
    Toute modification sera perdue.
*/
/* tslint:disable */
import * as server from "../../../common/services/server";
<#list importList as import>
${import};
</#list>

<#list routes as route>

export function ${route.methodName}(<#t/>
<#list route.webServiceParams as param>
${param.name}: ${param.typeModel.jsType}<#t/>
<#if (param_index+1) < route.webServiceParams?size>, </#if><#t/>
</#list><#t/>
) {
	return server.${route.jsServerCallMethod}(`${route.path}`<#t/>	
	<#if (route.webServiceParams?size > 0)>, <#t/>
	<#if !route.isGet() && (route.webServiceParams?size > 1) >{</#if><#if route.isGet()>[</#if><#list route.webServiceParams as param><#t/>	
	 ${param.name}<#t/>
	 <#if (param_index+1) < route.webServiceParams?size>, </#if><#t/>
	</#list><#if route.isGet() && (route.webServiceParams?size > 0)>]</#if><#if !route.isGet() && (route.webServiceParams?size > 1)>}</#if><#t/>
	</#if>)
}
</#list>