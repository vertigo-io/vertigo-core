<#--
/*
 * $Id: checkboxlist.ftl,v 1.2 2014/03/18 10:52:48 npiedeloup Exp $
 */
-->
<#assign itemCount = 0/>
<#if parameters.list??>
<#assign paramListKey = parameters.listKey!util.getIdField(parameters.list) />
<#assign paramListValue = parameters.listValue!util.getDisplayField(parameters.list) />
<@s.iterator value="parameters.list">
    <#assign itemCount = itemCount + 1/>
    <#if paramListKey??>
            <#if stack.findValue(paramListKey)??>
              <#assign itemKey = stack.findValue(paramListKey)/>
              <#assign itemKeyStr = stack.findString(paramListKey)/>
            <#else>
              <#assign itemKey = ''/>
              <#assign itemKeyStr = ''/>
            </#if>
        <#else>
            <#assign itemKey = stack.findValue('top')/>
            <#assign itemKeyStr = stack.findString('top')>
        </#if>
        <#if paramListValue??>
            <#if stack.findString(paramListValue)??>
              <#assign itemValue = stack.findString(paramListValue)/>
            <#else>
              <#assign itemValue = ''/>
            </#if>
        <#else>
            <#assign itemValue = stack.findString('top')/>
        </#if>
    <#if parameters.listCssClass??>
        <#if stack.findString(parameters.listCssClass)??>
          <#assign itemCssClass= stack.findString(parameters.listCssClass)/>
        <#else>
          <#assign itemCssClass = ''/>
        </#if>
    </#if>
    <#if parameters.listCssStyle??>
        <#if stack.findString(parameters.listCssStyle)??>
          <#assign itemCssStyle= stack.findString(parameters.listCssStyle)/>
        <#else>
          <#assign itemCssStyle = ''/>
        </#if>
    </#if>
    <#if parameters.listTitle??>
        <#if stack.findString(parameters.listTitle)??>
          <#assign itemTitle= stack.findString(parameters.listTitle)/>
        <#else>
          <#assign itemTitle = ''/>
        </#if>
    </#if>
    <#assign itemKeyStr=itemKey.toString() />
<#assign previousCssClass = appendedCssClass!''/>
<#assign appendedCssClass = previousCssClass +' checkbox'/>
<label for="${parameters.name?html}-${itemCount}" <#include "/${parameters.templateDir}/simple/css.ftl"/>><#rt/>
<#assign appendedCssClass = previousCssClass/>
<input type="checkbox" name="${parameters.name?html}" value="${itemKeyStr?html}"<#rt/>
 id="${parameters.name?html}-${itemCount}"<#rt/>
    <#if tag.contains(parameters.nameValue, itemKey)>
 checked="checked"<#rt/>
    </#if>
    <#if parameters.disabled?default(false)>
 disabled="disabled"<#rt/>
    </#if>
    <#if itemCssClass?if_exists != "">
 class="${itemCssClass?html}"<#rt/>
    <#else>
        <#if parameters.cssClass??>
 class="${parameters.cssClass?html}"<#rt/>
        </#if>
    </#if>
    <#if itemCssStyle?if_exists != "">
 style="${itemCssStyle?html}"<#rt/>
    <#else>
        <#if parameters.cssStyle??>
 style="${parameters.cssStyle?html}"<#rt/>
        </#if>
    </#if>
    <#if itemTitle?if_exists != "">
 title="${itemTitle?html}"<#rt/>
    <#else>
        <#if parameters.title??>
 title="${parameters.title?html}"<#rt/>
        </#if>
    </#if>
    <#include "/${parameters.templateDir}/simple/css.ftl" />
    <#include "/${parameters.templateDir}/simple/scripting-events.ftl" />
    <#include "/${parameters.templateDir}/simple/common-attributes.ftl" />
/><#rt/>
${itemValue?html}</label><br/>
</@s.iterator>
    <#else>
 &nbsp;
</#if>
<input type="hidden" id="__multiselect_${parameters.id?html}" name="__multiselect_${parameters.name?html}"<#rt/>
 value=""<#rt/>
<#if parameters.disabled?default(false)>
 disabled="disabled"<#rt/>
</#if>
 />