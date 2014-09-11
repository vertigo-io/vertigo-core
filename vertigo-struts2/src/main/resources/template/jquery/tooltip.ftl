<#--
/*
 * $Id: tooltip.ftl,v 1.2 2014/01/15 15:32:30 npiedeloup Exp $
 *
 */
-->
<#if parameters.tooltip??><#t/>
      <img
      <#if parameters.tooltipIconPath??><#t/>
      	src='<@s.url value="${parameters.tooltipIconPath}" includeParams="none" encode="false" />'
      <#else><#t/>
      	src='<@s.url value="/struts/tooltip.gif" includeParams="none" encode="false" />'
      </#if><#t/>
      <#if parameters.jsTooltipEnabled?default('false') == 'true'>
          onmouseover="domTT_activate(this, event, 'content', '${parameters.tooltip}'<#t/>
          <#if parameters.tooltipDelay??><#t/>
          	<#t/>,'delay', '${parameters.tooltipDelay}'<#t/>
          </#if><#t/>
          <#t/>,'styleClass', '${parameters.tooltipCssClass?default("StrutsTTClassic")}'<#t/>
          <#t/>)" />
      <#else>
      	title="${parameters.tooltip?html}"
      	alt="${parameters.tooltip?html}" />
     </#if>
</#if><#t/>
