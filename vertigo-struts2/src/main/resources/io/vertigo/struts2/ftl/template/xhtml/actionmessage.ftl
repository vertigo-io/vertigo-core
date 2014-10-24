<#--
/*
 * $Id: actionmessage.ftl,v 1.2 2014/01/15 15:32:30 npiedeloup Exp $
 *
 */
-->

<#if ((fieldErrors?? && fieldErrors?size >0) || (actionErrors?? && actionErrors?size > 0))>
	<div class="errorPanel">
		<h3>L'action demand&eacute;e ne peut se poursuivre car les erreurs suivantes ont &eacute;t&eacute; d&eacute;tect&eacute;es:</h3>
		<#include "/${parameters.templateDir}/xhtml/actionerror.ftl" />
		<@s.fielderror />
	</div>
</#if>
<#include "/${parameters.templateDir}/xhtml/actionmessage-default.ftl"/>
