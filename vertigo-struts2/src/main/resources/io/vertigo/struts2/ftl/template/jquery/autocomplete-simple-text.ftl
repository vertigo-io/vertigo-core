<#--
/*
 * Text simple pour l'autocomplete.
 */
-->
<#assign uiList = stack.findValue(parameters.remoteList) />
<#if parameters.nameValue?? && parameters.nameValue!=''>
	<#assign uiObject = uiList.getById(parameters.remoteListKey, parameters.nameValue) />
</#if>
<input<#rt/>
 type="${parameters.type?default("text")?html}"<#rt/>
<#-- name="${parameters.name?default("")?html}"<#rt/> pas de nom car il ne correspond a aucun champs -->
<#if parameters.get("size")??>
 size="${parameters.get("size")?html}"<#rt/>
</#if>
<#if parameters.maxlength??>
 maxlength="${parameters.maxlength?html}"<#rt/>
</#if>
<#if uiObject??>
<#-- replace \ en - : doit correspondre au formatage du javascript jQuery.ui.autocomplete.prototype._renderItem -->
 value="${uiObject[parameters.remoteListValue]?html?replace('\n', ' - ')}"<#rt/>
</#if>
<#if parameters.disabled?default(false)>
 disabled="disabled"<#rt/>
</#if>
<#if parameters.readonly?default(false)>
 readonly="readonly"<#rt/>
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
<#include "/${parameters.templateDir}/simple/scripting-events.ftl" />
<#include "/${parameters.templateDir}/simple/common-attributes.ftl" />
<#include "/${parameters.templateDir}/simple/dynamic-attributes.ftl" />
/>