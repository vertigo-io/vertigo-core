<#--
/*
 * $Id: pushLayoutType.ftl,v 1.4 2014/03/18 10:52:48 npiedeloup Exp $
 */
-->
<#assign previousType = [controlLayout_type?default('none')] + controlLayout_previoustype?default([]) />
<#assign previousColumnCount = [controlLayout_currentColumnCount?default(0)] + controlLayout_previousColumnCount?default([]) />	

${stack.setValue('#controlLayout_previoustype', previousType)}<#t/>
${stack.setValue('#controlLayout_previousColumnCount', previousColumnCount)}<#t/>