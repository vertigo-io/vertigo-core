<#macro table constraints>
	<table class="mdl-data-table mdl-js-data-table mdl-data-table mdl-shadow--2dp">
		  <thead>
		    <tr>
		      <th class="mdl-data-table__cell--non-numeric">Name</th>
		      <th class="mdl-data-table__cell--non-numeric">Type</th>
		      <th class="mdl-data-table__cell--non-numeric">Value</th>
		    </tr>
		  </thead>
		  <tbody>
			<#list constraints as constraint>
			    <tr>
		    	    <td class="mdl-data-table__cell--non-numeric"><a href="/studio/definitions/${constraint.name}">${constraint.name}</a></td>
		    	    <td class="mdl-data-table__cell--non-numeric">${constraint.property.name}</td>
		    	    <td class="mdl-data-table__cell--non-numeric">${constraint.propertyValue}</td>
				</tr>
            </#list>
		  </tbody>
	</table>
</#macro>

<#macro detail constraint>
	<ul>
	    <li>${constraint.name}</li>
	    <li>${constraint.property.name}</li>
	    <li>${constraint.propertyValue}</li>
	</ul>
</#macro>