<#--
/*
 * $Id: radiomap.ftl,v 1.5 2014/03/20 16:39:17 npiedeloup Exp $
 */
-->
<@s.iterator value="parameters.list">
    <#if parameters.listKey??>
        <#assign itemKey = stack.findValue(parameters.listKey)/>
    <#else>
        <#assign itemKey = stack.findValue('top')/>
    </#if>
    <#assign itemKeyStr = itemKey.toString() />
    <#if parameters.listValueKey??>
        <#-- checks the valueStack for the 'valueKey.' The valueKey is then looked-up in the locale 
             file for it's localized value.  This is then used as a label -->
        <#assign itemValue = stack.findString(parameters.listValueKey)/>
        <#-- FIXME: find a better way to get the value than a call to @s.text -->
        <#assign itemValue><@s.text name="${itemValue}"/></#assign>
    <#elseif parameters.listValue??>
        <#assign itemValue = stack.findString(parameters.listValue)/>
    <#else>
        <#assign itemValue = stack.findString('top')/>
    </#if>
    <#if parameters.listCssClass?has_content>
        <#assign itemCssClass= stack.findString(parameters.listCssClass)!''/>
    </#if>
    <#if parameters.listCssStyle?has_content>
        <#assign itemCssStyle= stack.findString(parameters.listCssStyle)!''/>
    </#if>
    <#if parameters.listTitle?has_content>
        <#assign itemTitle= stack.findString(parameters.listTitle)!''/>
    </#if>
<#assign previousCssClass = appendedCssClass!''/>
<#assign appendedCssClass = previousCssClass +' radio'/>
<label for="${parameters.id?html}${itemKeyStr?html}" <#include "/${parameters.templateDir}/simple/css.ftl"/>><#rt/>
<#assign appendedCssClass = previousCssClass/>
<input type="radio"<#rt/>
<#if parameters.name?has_content>
 name="${parameters.name?html}"<#rt/>
</#if>
 id="${parameters.id?html}${itemKeyStr?html}"<#rt/>
<#if tag.contains(parameters.nameValue!'', itemKeyStr)>
 checked="checked"<#rt/>
</#if>
<#if itemKey??>
 value="${itemKeyStr?html}"<#rt/>
</#if>
<#if parameters.disabled!false>
 disabled="disabled"<#rt/>
</#if>
<#if parameters.readonly!false>
 readonly="readonly"<#rt/>
</#if>
<#if parameters.tabindex?has_content>
 tabindex="${parameters.tabindex?html}"<#rt/>
</#if>
<#if itemCssClass?has_content>
 class="${itemCssClass?html}"<#rt/>  <#-- parameter.cssClass, and other are included by css.ftl -->
</#if>
<#if itemCssStyle?has_content>
 style="${itemCssStyle?html}"<#rt/>
</#if>
<#if itemTitle?has_content>
 title="${itemTitle?html}"<#rt/>
<#elseif parameters.title?has_content>
 title="${parameters.title?html}"<#rt/>
</#if>
<#include "/${parameters.templateDir}/${parameters.expandTheme}/css.ftl" />
<#include "/${parameters.templateDir}/${parameters.expandTheme}/scripting-events.ftl" />
<#include "/${parameters.templateDir}/${parameters.expandTheme}/common-attributes.ftl" />
<#include "/${parameters.templateDir}/${parameters.expandTheme}/dynamic-attributes.ftl" />
/><#rt/>
  ${itemValue}<#rt/>
</label>
</@s.iterator>