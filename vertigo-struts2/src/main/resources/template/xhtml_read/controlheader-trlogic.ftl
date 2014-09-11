<#--
	This template handles: 
		* intializing controlLayout.currentColumnCount if it has not been initialzed,
		* emiting a <tr> tag if the currentColumnCount == 0,
-->
<#if ! controlLayout_currentColumnCount?exists >
	<#-- Set the currentColumnCount to 0 because this is the first row of the table. -->
	${stack.setValue('#controlLayout_currentColumnCount', 0)}
</#if>
<#-- Do we need to write the opening tr tag. -->
<#if controlLayout_tablecolspan?exists >
	<#assign columnCount = controlLayout_currentColumnCount />
<#else>
	<#-- Set the currentColumnCount to 0 because this is the first row of the table. -->
	${stack.setValue('#controlLayout_currentColumnCount', 0)}
	<#assign columnCount = 0 />
</#if>
<#if columnCount == 0>
	<#-- Write out the opening tr tag to start the table row. -->
	<tr>
</#if>
