<#import "page.ftl" as page>
<#import "domain.ftl" as domain>
<#import "constraint.ftl" as constraint>
<#import "formatter.ftl" as formatter>
<#import "dtDefinition.ftl" as dtDefinition>
<#import "taskDefinition.ftl" as taskDefinition>

<@page.header/>
		<div class="mdl-layout mdl-layout--fixed-header mdl-js-layout mdl-color--grey-100">
		<#list searchGrammar as grammar>
		    <h1 id='${grammar.key}'>${grammar.key}</h1>
	    	<script>Diagram(${grammar.value}).addTo();</script>
        </#list>			
		</div>			
<@page.footer/>		
