<#--
/*
 * $Id: radiomap.ftl,v 1.2 2014/01/15 15:32:30 npiedeloup Exp $
 *
 */
-->
<#include "/${parameters.templateDir}/${parameters.theme}/controlheader.ftl" />
<#include "/${parameters.templateDir}/simple/radiomap.ftl" />
<input type="hidden" id="__hiddenradio_${parameters.id?html}" name="${parameters.name?html}" value="" <#rt/>>
<#include "/${parameters.templateDir}/xhtml/controlfooter.ftl" /><#nt/>
