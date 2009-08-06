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

<#macro selectRecipients contactsList mailContacts readonly=false>
	<select name="recipients" multiple="multiple" size="2" <#if readonly>readonly="readonly"</#if>>
	<#list contactsList as contact>
		<#local found = 0>
		<#list mailContacts as mailContact>
			<#if contact.mailAddress==mailContact.mailAddress>
				<#local found = 1>
				<#break>
			</#if>
		</#list>
		<#if found=1>
		<option value="${contact.mailAddress}" selected>${contact.name}</option>
		<#else>
		<option value="${contact.mailAddress}">${contact.name}</option>
		</#if>
	</#list>
	</select>
</#macro>