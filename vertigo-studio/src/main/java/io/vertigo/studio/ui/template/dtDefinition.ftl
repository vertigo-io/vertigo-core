<#macro table dtDefinitions>
	<table class="mdl-data-table mdl-js-data-table mdl-data-table mdl-shadow--2dp">
		  <thead>
		    <tr>
		      <th class="mdl-data-table__cell--non-numeric">Name</th>
		      <th class="mdl-data-table__cell--non-numeric">Stereotype</th>
		      <th class="mdl-data-table__cell--non-numeric">PackageName</th>
		      <th class="mdl-data-table__cell--non-numeric">DataSpace</th>
		      <th class="mdl-data-table__cell--non-numeric">Persistent</th>
		      <th class="mdl-data-table__cell--non-numeric">Fragment</th>
		    </tr>
		  </thead>
		  <tbody>
			<#list dtDefinitions as dtDefinition>
			    <tr>
		    	    <td class="mdl-data-table__cell--non-numeric"><a href="/studio/definitions/${dtDefinition.name}">${dtDefinition.name}</a></td>
		    	    <td class="mdl-data-table__cell--non-numeric">${dtDefinition.stereotype.name()}</td>
		    	    <td class="mdl-data-table__cell--non-numeric">${dtDefinition.packageName}</td>
		    	    <td class="mdl-data-table__cell--non-numeric">${dtDefinition.dataSpace}</td>
		    	    <td class="mdl-data-table__cell--non-numeric">${dtDefinition.isPersistent() ?then("persistent","")}</td>
		    	    <td class="mdl-data-table__cell--non-numeric">${dtDefinition.getFragment().isPresent() ?then("<a href=\"/studio/definitions/"+dtDefinition.getFragment().get().getName()+"\">"+dtDefinition.getFragment().get().getName()+"</a>","")}</td>
				</tr>
            </#list>
		  </tbody>
	</table>
</#macro>
<#macro detail dtDefinition>
	<ul>
	    <li><a href="/studio/definitions/${dtDefinition.name}">${dtDefinition.name}</a></li>
	    <li>${dtDefinition.stereotype.name()}</li>
	    <li>${dtDefinition.packageName}</li>
	    <li>${dtDefinition.dataSpace}</li>
	    ${dtDefinition.isPersistent() ?then("<li>persistent</li>", "")}
	    ${dtDefinition.getFragment().isPresent()?then("<li>fragment of <a href=\"/studio/definitions/"+dtDefinition.getFragment().get().getName()+"\">"+dtDefinition.getFragment().get().getName()+"</a></li>", "")}
	    
	</ul>
	
	<table class="mdl-data-table mdl-js-data-table mdl-data-table mdl-shadow--2dp">
		  <thead>
		    <tr>
		      <th class="mdl-data-table__cell--non-numeric">Name</th>
		      <th class="mdl-data-table__cell--non-numeric">Type</th>
		      <th class="mdl-data-table__cell--non-numeric">required</th>
		    </tr>
		  </thead>
		  <tbody>
			<#list dtDefinition.fields as dtField>
			    <tr>
		    	    <td class="mdl-data-table__cell--non-numeric">${dtField.name()}</a></td>
		    	    <td class="mdl-data-table__cell--non-numeric">${dtField.type.name()}</td>
		    	    <td class="mdl-data-table__cell--non-numeric">${dtField.isRequired()?then("y","n")}</td>
				</tr>
            </#list>
		  </tbody>
	</table>
</#macro>