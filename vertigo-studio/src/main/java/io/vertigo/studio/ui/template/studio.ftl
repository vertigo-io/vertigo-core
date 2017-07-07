<#import "page.ftl" as page>
<#import "domain.ftl" as domain>
<#import "constraint.ftl" as constraint>
<#import "formatter.ftl" as formatter>
<#import "dtDefinition.ftl" as dtDefinition>
<#import "taskDefinition.ftl" as taskDefinition>

<@page.header/>
		 <div class="mdl-layout mdl-layout--fixed-header mdl-js-layout mdl-color--grey-100">
		
			<div class="mdl-tabs mdl-js-tabs mdl-js-ripple-effect">
			  <div class="mdl-tabs__tab-bar">
			      <a href="#all-panel" class="mdl-tabs__tab is-active">All</a>
			      <#list definitionTypes as definitionType>
				      <a href="#${definitionType.name}-panel" class="mdl-tabs__tab">${definitionType.name}</a>
				  </#list>
			  </div>
			
			  <div class="mdl-tabs__panel is-active" id="all-panel">
					<main class="mdl-layout__content">
						    <div class="mdl-grid">
								<table class="mdl-data-table mdl-js-data-table mdl-data-table mdl-shadow--2dp">
									  <thead>
									    <tr>
									      <th class="mdl-data-table__cell--non-numeric">Name</th>
									      <th>Count</th>
									    </tr>
									  </thead>
									  <tbody>
										<#list definitionTypes as definitionType>
										    <tr>
									    	    <td class="mdl-data-table__cell--non-numeric">${definitionType.name}</td>
									    	    <td>${definitionType.count}</td>
											</tr>
							            </#list>
									  </tbody>
								</table>
							</div>
							<div class="mdl-grid">
								<button onclick="return generate()" class="mdl-button mdl-js-button mdl-button--raised mdl-js-ripple-effect mdl-button--accent">
						  			generate
								</button>
								<button onclick="return clean()" class="mdl-button mdl-js-button mdl-button--raised mdl-js-ripple-effect mdl-button--accent">
						  			clean
								</button>
								<button onclick="return grammar()" class="mdl-button mdl-js-button mdl-button--raised mdl-js-ripple-effect mdl-button--accent">
						  			grammar
								</button>
							</div>
							 <#if result??>	
							 <ul>
								<li>created   : ${result.createdFiles} </li>		
								<li>updated   : ${result.updatedFiles} </li>		
								<li>deleted   : ${result.deletedFiles} </li>		
								<li>error     : ${result.errorFiles} </li>		
								<li>identical : ${result.identicalFiles} </li>		
								<li>duration  : ${result.durationMillis} </li>		
							</ul>
							</#if>
				    </main>
			  </div>


			  <#list definitionTypes as definitionType>
  				<div class="mdl-tabs__panel" id="${definitionType.name}-panel">
	  				<#switch "${definitionType.name}">
	  					<#case "Domain">
	  						<@domain.table domains=definitionType.definitions/>
	  					<#break>
	  					
	  					<#case "ConstraintDefinition">
	  						<@constraint.table constraints=definitionType.definitions/>
	  					<#break>
	  					
	  					<#case "FormatterDefinition">
	  						<@formatter.table formatters=definitionType.definitions/>
	  					<#break>
	  					
	  					<#case "DtDefinition">
	  						<@dtDefinition.table dtDefinitions=definitionType.definitions/>
	  					<#break>
	  					
	  					<#case "TaskDefinition">
	  						<@taskDefinition.table taskDefinitions=definitionType.definitions/>
	  					<#break>
	  					
	  					<#default>
	  						<#--By default only the name is displayed -->
				   			<ul class="demo-list-item mdl-list">
						   		<#list definitionType.definitions as definition>
						      		<li class="mdl-list__item">${definition.name}</li>
				            	</#list>
				   			</ul>
					</#switch>  
				</div>
			  </#list>
		</div>
		
		
		<script language="javascript" type="text/javascript">
			function generate() {
    			window.location.href = "/generate";
			}
			function clean() {
    			window.location.href = "/clean";
			}
			function grammar() {
    			window.location.href = "/grammar";
			}
		</script>

<@page.footer/>		
