<#--
/*
 * $Id: popLayoutType.ftl,v 1.4 2014/03/18 10:52:48 npiedeloup Exp $
 */
-->
<#assign previousLayoutStack = (controlLayout_previoustype?default(['none'])) />	
<#assign previousColumnCountStack = (controlLayout_previousColumnCount?default([0])) />	
<#assign currentLayout = previousLayoutStack[0] />
${stack.setValue('#controlLayout_type', previousLayoutStack[0])}
${stack.setValue('#controlLayout_currentColumnCount', previousColumnCountStack[0])}

<#if previousLayoutStack?size == 1>
  <#assign previousType = [] />	
  <#assign previousColumnCount = [] />
<#else>
  <#assign previousType = previousLayoutStack[1..] />	
  <#assign previousColumnCount = previousColumnCountStack[1..] />
</#if>
${stack.setValue('#controlLayout_previoustype', previousType)}
${stack.setValue('#controlLayout_previousColumnCount', previousColumnCount)}
