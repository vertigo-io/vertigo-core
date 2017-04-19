<#macro table taskDefinitions>
	<table class="mdl-data-table mdl-js-data-table mdl-data-table mdl-shadow--2dp">
		  <thead>
		    <tr>
		      <th class="mdl-data-table__cell--non-numeric">Name</th>
		      <th class="mdl-data-table__cell--non-numeric">PackageName</th>
		      <th class="mdl-data-table__cell--non-numeric">DataSpace</th>
		      <th class="mdl-data-table__cell--non-numeric">request</th>
		    </tr>
		  </thead>
		  
		  <tbody>
			<#list taskDefinitions as taskDefinition>
			    <tr>
		    	    <td class="mdl-data-table__cell--non-numeric"><a href="/studio/definitions/${taskDefinition.name}">${taskDefinition.name}</a></td>
		    	    <td class="mdl-data-table__cell--non-numeric">${taskDefinition.packageName}</td>
		    	    <td class="mdl-data-table__cell--non-numeric">${taskDefinition.dataSpace}</td>
		    	    <td class="mdl-data-table__cell--non-numeric">${taskDefinition.request}</td>
				</tr>
            </#list>
		  </tbody>
	</table>
</#macro>

<#macro detail taskDefinition>
	<ul>
	    <li><a href="/studio/definitions/${taskDefinition.name}">${taskDefinition.name}</a></li>
	    <li>name : ${taskDefinition.name}</li>
	    <li>packageName : ${taskDefinition.packageName}</li>
	    <li>dataSpace : ${taskDefinition.dataSpace}</li>
	    ${taskDefinition.getOutAttributeOption().isPresent()?then("<li>out attribute : " + taskDefinition.getOutAttributeOption().get().getName() + "</li>", "")}
	</ul>
	
	<table class="mdl-data-table mdl-js-data-table mdl-data-table mdl-shadow--2dp">
		  <thead>
		    <tr>
		      <th class="mdl-data-table__cell--non-numeric">Name</th>
		      <th class="mdl-data-table__cell--non-numeric">Domain</th>
		      <th class="mdl-data-table__cell--non-numeric">required</th>
		    </tr>
		  </thead>
		  <tbody>
			<#list taskDefinition.inAttributes as attribute>
			    <tr>
		    	    <td class="mdl-data-table__cell--non-numeric">${attribute.name}</a></td>
		    	    <td class="mdl-data-table__cell--non-numeric">${attribute.domain.name}</td>
		    	    <td class="mdl-data-table__cell--non-numeric">${attribute.isRequired()?then("y","n")}</td>
				</tr>
            </#list>
		  </tbody>
	</table>
</#macro>