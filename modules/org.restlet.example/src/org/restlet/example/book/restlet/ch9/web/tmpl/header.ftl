<#import "/library.ftl" as lib>
<#macro listTags tagList>
	<#list tagList as tag>${tag}<#if tag_has_next> </#if></#list>
</#macro>
