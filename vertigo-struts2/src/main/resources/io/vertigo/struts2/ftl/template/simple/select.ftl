<#--
/*
 * $Id: select.ftl,v 1.2 2014/01/17 09:53:30 npiedeloup Exp $
 *
 */
-->
<#setting number_format="#.#####">
<select<#rt/>
 name="${parameters.name?default("")?html}"<#rt/>
<#if parameters.get("size")??>
 size="${parameters.get("size")?html}"<#rt/>
</#if>
<#if parameters.disabled?default(false)>
 disabled="disabled"<#rt/>
</#if>
<#if parameters.tabindex??>
 tabindex="${parameters.tabindex?html}"<#rt/>
</#if>
<#if parameters.id??>
 id="${parameters.id?html}"<#rt/>
</#if>
<#include "/${parameters.templateDir}/simple/css.ftl" />
<#if parameters.title??>
 title="${parameters.title?html}"<#rt/>
</#if>
<#if parameters.multiple?default(false)>
 multiple="multiple"<#rt/>
</#if>
<#include "/${parameters.templateDir}/simple/scripting-events.ftl" />
<#include "/${parameters.templateDir}/simple/common-attributes.ftl" />
<#include "/${parameters.templateDir}/simple/dynamic-attributes.ftl" />
>
<#if parameters.headerKey?? && parameters.headerValue??>
    <option value="${parameters.headerKey?html}"
    <#if tag.contains(parameters.nameValue, parameters.headerKey) == true>
    selected="selected"
    </#if>
    >${parameters.headerValue?html}</option>
</#if>
<#if parameters.emptyOption?default(false)>
    <option value=""></option>
</#if>

<#assign paramListKey = parameters.listKey!util.getIdField(parameters.list) />
<#assign paramListValue = parameters.listValue!util.getDisplayField(parameters.list) />
<@s.iterator value="parameters.list">
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
    <option value="${itemKeyStr?html}"<#rt/>
        <#if tag.contains(parameters.nameValue, itemKey) == true>
 selected="selected"<#rt/>
        </#if>
        <#if itemCssClass?if_exists != "">
 class="${itemCssClass?html}"<#rt/>
        </#if>
        <#if itemCssStyle?if_exists != "">
 style="${itemCssStyle?html}"<#rt/>
        </#if>
        <#if itemTitle?if_exists != "">
 title="${itemTitle?html}"<#rt/>
        </#if>
    >${itemValue?html}</option><#lt/>
</@s.iterator>

<#include "/${parameters.templateDir}/simple/optgroup.ftl" />

</select>
<#if parameters.multiple?default(false)>
<input type="hidden" id="__multiselect_${parameters.id?html}" name="__multiselect_${parameters.name?html}" value=""<#rt/>
<#if parameters.disabled?default(false)>
 disabled="disabled"<#rt/>
</#if>
 />
</#if>