<#--
/*
 * $Id: fielderror.ftl,v 1.2 2014/01/15 15:32:30 npiedeloup Exp $
 *
 */
-->
<#if fieldErrors??><#t/>
    <#assign eKeys = fieldErrors.keySet()><#t/>
    <#assign eKeysSize = eKeys.size()><#t/>
    <#assign doneStartUlTag=false><#t/>
    <#assign doneEndUlTag=false><#t/>
    <#assign haveMatchedErrorField=false><#t/>
    <#if (fieldErrorFieldNames?size > 0) ><#t/>
        <#list fieldErrorFieldNames as fieldErrorFieldName><#t/>
            <#list eKeys as eKey><#t/>
                <#if (eKey = fieldErrorFieldName)><#t/>
                    <#assign haveMatchedErrorField=true><#t/>
                    <#assign eValue = fieldErrors[fieldErrorFieldName]><#t/>
                    <#if (haveMatchedErrorField && (!doneStartUlTag))><#t/>
                    <ul<#rt/>
                        <#if parameters.id?if_exists != "">
                                id="${parameters.id?html}"<#rt/>
                        </#if>
                        <#if parameters.cssClass??>
                                class="${parameters.cssClass?html}"<#rt/>
                            <#else>
                                class="errorMessage"<#rt/>
                        </#if>
                        <#if parameters.cssStyle??>
                                style="${parameters.cssStyle?html}"<#rt/>
                        </#if>
                            >
                        <#assign doneStartUlTag=true><#t/>
                    </#if><#t/>
                    <#list eValue as eEachValue><#t/>
                            <li><span class="messageLabel">${util.label(eKey)?html}: </span><span class="message"><#if parameters.escape>${eEachValue!?html}<#else>${eEachValue!}</#if></span></li>
                    </#list><#t/>
                </#if><#t/>
            </#list><#t/>
        </#list><#t/>
        <#if (haveMatchedErrorField && (!doneEndUlTag))><#t/>
        </ul>
            <#assign doneEndUlTag=true><#t/>
        </#if><#t/>
        <#else><#t/>
        <#if (eKeysSize > 0)><#t/>
        <ul<#rt/>
            <#if parameters.cssClass??>
                    class="${parameters.cssClass?html}"<#rt/>
                <#else>
                    class="errorMessage"<#rt/>
            </#if>
            <#if parameters.cssStyle??>
                    style="${parameters.cssStyle?html}"<#rt/>
            </#if>
                >
            <#list eKeys as eKey><#t/>
                <#assign eValue = fieldErrors[eKey]><#t/>
                <#list eValue as eEachValue><#t/>
                    <li><span class="messageLabel">${util.label(eKey)?html}: </span><span class="message"><#if parameters.escape>${eEachValue!?html}<#else>${eEachValue!}</#if></span></li>
                </#list><#t/>
            </#list><#t/>
        </ul>
        </#if><#t/>
    </#if><#t/>
</#if><#t/>
