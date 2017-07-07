<#macro table domains>
	<table class="mdl-data-table mdl-js-data-table mdl-data-table mdl-shadow--2dp">
		  <thead>
		    <tr>
		      <th class="mdl-data-table__cell--non-numeric">Name</th>
		      <th class="mdl-data-table__cell--non-numeric">Type</th>
		    </tr>
		  </thead>
		  <tbody>
			<#list domains as domain>
			    <tr>
		    	    <td class="mdl-data-table__cell--non-numeric"><a href="/studio/definitions/${domain.name}">${domain.name}</a></td>
		    	    <td class="mdl-data-table__cell--non-numeric">${domain.dataType}</td>
				</tr>
            </#list>
		  </tbody>
	</table>
</#macro>

<#macro detail domain>
	<h1> Domain </h1>
	<ul>
		<li>name:${domain.name}</li>
		<li>type:${domain.dataType}</li>
	</ul>
</#macro>