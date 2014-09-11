<#--
/*
 */
-->
<#if parameters.parentTheme = 'xhtml_read'>
   <#-- rien -->
<#else>
<#assign escapedOptionId="${parameters.id?string?replace('.', '_')}">
<script type='text/javascript'>
jQuery(document).ready(function () {
  <#if parameters.valueWidget?if_exists != "">
	jQuery("#${parameters.id?html}").val("${parameters.valueWidget?html}");
  </#if>
	var options_${escapedOptionId?html} = {};
  <#if parameters.widgetid?if_exists != "">
	options_${escapedOptionId?html}.hiddenid = "${parameters.widgetid?html}";
  </#if>
  <#if parameters.delay??>
	options_${escapedOptionId?html}.delay = ${parameters.delay?html};
  </#if>
  <#if parameters.loadMinimumCount??>
	options_${escapedOptionId?html}.minimum = ${parameters.loadMinimumCount?html};
  </#if>
  <#if parameters.autoFocus?default(false) >
	options_${escapedOptionId?html}.autoFocus = true;
  </#if>
  <#if parameters.selectBox?default(false) || (parameters.list?? && parameters.listKey?? && !parameters.hrefUrl??) >
	options_${escapedOptionId?html}.selectBox = true;
  <#else>
	options_${escapedOptionId?html}.selectBox = false;
  </#if>
  <#if parameters.selectBoxIcon?default(false) >
	options_${escapedOptionId?html}.selectBoxIcon = true;
  </#if>
  <#if parameters.onSearchTopics?exists>
	options_${escapedOptionId?html}.onsearchtopics = "${parameters.onSearchTopics?html}";
  </#if>
  <#if parameters.forceValidOption?default(true) >
	options_${escapedOptionId?html}.forceValidOption = true;
  <#else>
	options_${escapedOptionId?html}.forceValidOption = false;
  </#if>
  <#if parameters.onFocusTopics?exists>
	options_${escapedOptionId?html}.onfocustopics = "${parameters.onFocusTopics?html}";
  </#if>
  <#if parameters.onSelectTopics?exists>
	options_${escapedOptionId?html}.onselecttopics = "${parameters.onSelectTopics?html}";
  </#if>
  <#if parameters.list?? && !parameters.listKey?? && !parameters.selectBox?? &&  !parameters.hrefUrl??>
	options_${escapedOptionId?html}.list = new Array();
<@s.iterator value="parameters.list">
        <#if parameters.listValue??>
            <#if stack.findString(parameters.listValue)??>
              <#assign itemValue = stack.findString(parameters.listValue)/>
            <#else>
              <#assign itemValue = ''/>
            </#if>
        <#else>
            <#assign itemValue = stack.findString('top')/>
        </#if>
	options_${escapedOptionId?html}.list.push("${itemValue?html}");
</@s.iterator>
  </#if>
  <#if parameters.remoteList?? && parameters.hrefUrl?? && !parameters.selectBox??>
	options_${escapedOptionId?html}.hrefparameter = "list=${parameters.remoteList?html}<#rt/>
	<#if parameters.remoteListKey??>
		&listKey=${parameters.remoteListKey?html}<#t/>
	</#if>
	<#if parameters.remoteListValue??>
		&listValue=${parameters.remoteListValue?html}<#t/>
	</#if>
	&CTX=${CTX?html}";<#lt/>
	<#-- 
	options_${escapedOptionId?html}.list = "${parameters.remoteList?html}";
	-->
	<#if parameters.remoteListKey??>
	options_${escapedOptionId?html}.listkey = "${parameters.remoteListKey?html}";
	</#if>
	<#if parameters.remoteListValue??>
	options_${escapedOptionId?html}.listvalue = "${parameters.remoteListValue?html}";
	</#if>
	<#if parameters.listLabel??>
	options_${escapedOptionId?html}.listlabel = "${parameters.listLabel?html}";
	</#if>
  </#if>
  <#include "/${parameters.templateDir}/jquery/base.ftl" />
  <#include "/${parameters.templateDir}/jquery/interactive.ftl" />
  <#include "/${parameters.templateDir}/jquery/topics.ftl" />
  <#include "/${parameters.templateDir}/jquery/action.ftl" />
  <#include "/${parameters.templateDir}/jquery/container.ftl" />
  <#include "/${parameters.templateDir}/jquery/draggable.ftl" />
  <#include "/${parameters.templateDir}/jquery/droppable.ftl" />
  <#include "/${parameters.templateDir}/jquery/resizable.ftl" />
  <#include "/${parameters.templateDir}/jquery/selectable.ftl" />
  <#include "/${parameters.templateDir}/jquery/sortable.ftl" />

  <#include "/${parameters.templateDir}/jquery/jquery-ui-bind.ftl" />
  <#assign escapedIconId="icon_${parameters.id?string?replace('.', '_')}">
  initAutocompleter('#${parameters.widgetid?html}', '#${escapedOptionId?html}', '#${escapedIconId?html}', ${parameters.loadMinimumCount!1?html});
 });
</script>
</#if>