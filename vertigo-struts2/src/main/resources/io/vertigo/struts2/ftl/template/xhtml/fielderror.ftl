<#--
/*
 * $Id: fielderror.ftl,v 1.2 2014/01/15 15:32:30 npiedeloup Exp $
 *
 */
-->
<#if fieldErrors??>
    <#assign eKeys = fieldErrors.keySet()>
    <#assign eKeysSize = eKeys.size()>
    <#assign doneStartUlTag=false>
    <#assign doneEndUlTag=false>
    <#assign haveMatchedErrorField=false>
    <#if (fieldErrorFieldNames?size > 0) >
        <#list fieldErrorFieldNames as fieldErrorFieldName>
            <#list eKeys as eKey>
                <#if (eKey = fieldErrorFieldName)>
                    <#assign haveMatchedErrorField=true>
                    <#assign eValue = fieldErrors[fieldErrorFieldName]>
                    <#if (haveMatchedErrorField && (!doneStartUlTag))>
	<ul <#rt/>
                        <#if parameters.id?if_exists != "">
                                id="${parameters.id?html}" <#t/>
                        </#if>
                        <#if parameters.cssClass??>
                                class="${parameters.cssClass?html}" <#t/>
                            <#else>
                                class="errorMessage"<#t/>
                        </#if>
                        <#if parameters.cssStyle??>
                                style="${parameters.cssStyle?html}" <#t/>
                        </#if>
	><#lt/>
                        <#assign doneStartUlTag=true>
                    </#if>
                    <#list eValue as eEachValue>
		<li><span class="messageLabel">${util.label(eKey)?html}: </span><span class="message"><#if parameters.escape>${eEachValue!?html}<#else>${eEachValue!}</#if></span></li>
                    </#list>
                </#if>
            </#list>
        </#list>
        <#if (haveMatchedErrorField && (!doneEndUlTag))>
	</ul>
            <#assign doneEndUlTag=true>
        </#if>
        <#else>
        <#if (eKeysSize > 0)>
	<ul <#rt/>
            <#if parameters.cssClass??>
                    class="${parameters.cssClass?html}" <#t/>
                <#else>
                    class="errorMessage" <#t/>
            </#if>
            <#if parameters.cssStyle??>
                    style="${parameters.cssStyle?html}" <#t/>
            </#if>
                ><#lt/>
            <#list eKeys as eKey>
                <#assign eValue = fieldErrors[eKey]>
                <#list eValue as eEachValue>
		<li><span class="messageLabel">${util.label(eKey)?html}: </span><span class="message"><#if parameters.escape>${eEachValue!?html}<#else>${eEachValue!}</#if></span></li>
                </#list>
            </#list>
	</ul>
        </#if>
    </#if>
</#if>
