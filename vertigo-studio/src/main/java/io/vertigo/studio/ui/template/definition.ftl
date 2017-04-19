<#import "page.ftl" as page>
<#import "domain.ftl" as domain>
<#import "constraint.ftl" as constraint>
<#import "formatter.ftl" as formatter>
<#import "dtDefinition.ftl" as dtDefinition>
<#import "taskDefinition.ftl" as taskDefinition>

<@page.header/>
		<#switch "${definition.class.simpleName}">
			<#case "Domain">
				<@domain.detail domain=definition/>
			<#break>
			
			<#case "ConstraintDefinition">
				<@constraint.detail constraint=definition/>
			<#break>
			
			<#case "FormatterDefinition">
				<@formatter.detail formatter=definition/>
			<#break>
			
			<#case "DtDefinition">
				<@dtDefinition.detail dtDefinition=definition/>
			<#break>
			
			<#case "TaskDefinition">
				<@taskDefinition.detail taskDefinition=definition/>
			<#break>
			
			<#default>
				<#--By default only the name is displayed -->
	   			<ul class="demo-list-item mdl-list">
		      		<li class="mdl-list__item">${definition.name}</li>
	   			</ul>
		</#switch>  
<@page.footer/>		
