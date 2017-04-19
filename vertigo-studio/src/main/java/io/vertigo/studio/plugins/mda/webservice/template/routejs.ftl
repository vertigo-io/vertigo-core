/**
 * These routes are generated automatically.
 * Facade ${facade}
 */

const ROOT = "${root}";
import urlBuilder from 'focus-core/util/url/builder';


<#list routes as route>
const ${route.jsUrlMethodName} = urlBuilder('${route.path}', '${route.verb}')
export function ${route.jsMethodName}(<#if (route.webServiceParams?size > 0)>{</#if><#t/>
<#list route.webServiceParams as param><#t/>
/*${param.type}<#if (param.optional)>,optional</#if>*/ ${param.name}<#if (param_index+1) < route.webServiceParams?size>, </#if><#t/>
</#list><#if (route.webServiceParams?size > 0)>}</#if>) {<#lt/>
  /* response ${route.returnType} */  
<#assign needComma = false><#t/>
  return ${route.jsUrlMethodName}(<#if (route.webServiceParams?size > 0)>{</#if><#rt/>
<#if ((0 < route.bodyWebServiceParams?size) || (0 < route.innerBodyWebServiceParams?size))>bodyData:{<#t/>
<#list route.bodyWebServiceParams as param><#t/>
...${param.name}<#if ((param_index+1) < route.bodyWebServiceParams?size) || (0 < route.innerBodyWebServiceParams?size)>, </#if><#t/>
</#list><#list route.innerBodyWebServiceParams as param><#t/>
${param.name}<#if (param_index+1) < route.innerBodyWebServiceParams?size>, </#if><#t/>
</#list>}<#assign needComma = true></#if><#t/>
<#if (route.queryWebServiceParams?size > 0)>
	<#if needComma>, </#if><#t/>
	queryData: {<#list route.queryWebServiceParams as param><#t/>
	${param.name}<#if (param_index+1) < route.queryWebServiceParams?size>, </#if><#t/>
</#list>}<#assign needComma = true></#if><#t/>
<#if (route.headerWebServiceParams?size > 0)>
	<#if needComma>, </#if><#t/>
	/*NotSupported*/headerData:{<#list route.headerWebServiceParams as param><#t/>
	${param.name}<#if (param_index+1) < route.headerWebServiceParams?size>, </#if><#t/>
</#list>}<#assign needComma = true></#if><#t/>
<#if (route.pathWebServiceParams?size > 0)>
	<#if needComma>, </#if><#t/>
	urlData:{<#list route.pathWebServiceParams as param><#t/>
	${param.name}<#if (param_index+1) < route.pathWebServiceParams?size>, </#if><#t/>
</#list>}<#assign needComma = true></#if><#if (route.webServiceParams?size > 0)>}</#if>);
}

</#list>
