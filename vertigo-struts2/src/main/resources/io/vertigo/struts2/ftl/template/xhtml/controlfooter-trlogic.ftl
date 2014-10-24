<#-- This template determines if the user is using table layout control, and if they are
     if a closing tr tag needs to be emitted due to the number of columns exceeding the 
	 controlLayout.tablecolspan parameter.
-->
<#if controlLayout_tablecolspan?exists >
	<#assign columnCount = controlLayout_currentColumnCount />	
	<#assign tablecolspan = controlLayout_tablecolspan />	
	<#if (columnCount >= tablecolspan) >
		</tr><#-- Write out the closing tr. -->
		<#assign columnCount = 0 />
	</#if>
	${stack.setValue('#controlLayout_currentColumnCount', columnCount)}<#t/>
</#if>
