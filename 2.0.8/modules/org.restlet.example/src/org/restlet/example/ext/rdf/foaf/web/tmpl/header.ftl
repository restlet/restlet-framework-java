<#import "/library.ftl" as lib>
<#macro listToString list separator=" ">
	<#list list as item>${item}<#if item_has_next>${separator}</#if></#list>
</#macro>
<#macro concatUris parentUri relativeUri>
	<#if parentUri?ends_with("/")>
		${parentUri}${relativeUri}
	<#else>
		${parentUri}/${relativeUri}
	</#if>
</#macro>
