<#macro table formatters>
	<table class="mdl-data-table mdl-js-data-table mdl-data-table mdl-shadow--2dp">
		  <thead>
		    <tr>
		      <th class="mdl-data-table__cell--non-numeric">Name</th>
		    </tr>
		  </thead>
		  <tbody>
			<#list formatters as formatter>
			    <tr>
		    	    <td class="mdl-data-table__cell--non-numeric"><a href="/studio/definitions/${formatter.name}">${formatter.name}</a></td>
				</tr>
            </#list>
		  </tbody>
	</table>
</#macro>

<#macro detail formatter>
	<ul>
  	    <li>${formatter.name}</li>
	</ul>
</#macro>