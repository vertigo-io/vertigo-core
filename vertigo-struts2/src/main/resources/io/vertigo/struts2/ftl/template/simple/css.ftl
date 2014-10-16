<#--
/*
 * $Id: css.ftl,v 1.2 2014/01/17 09:33:28 npiedeloup Exp $
 *
 */
-->
<#assign hasFieldErrors = parameters.name?? && fieldErrors?? && fieldErrors[parameters.name]??/>
<#if parameters.cssClass?? || (hasFieldErrors && parameters.cssErrorClass??) || (appendedCssClass?? && appendedCssClass != '') >
 class="<#rt/>
</#if>
<#if parameters.cssClass??>
 ${parameters.cssClass?html}<#rt/>
</#if>
<#if (hasFieldErrors && parameters.cssErrorClass??)>
 ${parameters.cssErrorClass?html}<#rt/>
</#if>
<#if (appendedCssClass?? && appendedCssClass != '')>
 ${appendedCssClass?trim?html}<#rt/>
</#if>
<#if parameters.cssClass?? || (hasFieldErrors && parameters.cssErrorClass??) || (appendedCssClass?? && appendedCssClass != '') >
"<#rt/>
</#if>
<#if parameters.cssStyle?? && !(hasFieldErrors && (parameters.cssErrorStyle?? || parameters.cssErrorClass??))>
 style="${parameters.cssStyle?html}"<#rt/>
<#elseif hasFieldErrors && parameters.cssErrorStyle??>
 style="${parameters.cssErrorStyle?html}"<#rt/>
</#if>