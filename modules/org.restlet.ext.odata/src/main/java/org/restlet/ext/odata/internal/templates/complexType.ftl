/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package ${packageName};

<#compress>
<#list type.importedJavaClasses?sort as clazz>
import ${clazz};
</#list>

<#list type.importedTypes?sort as t>
import ${t.fullClassName};
</#list>
</#compress>


<#compress>
/**
 * Generated by the generator tool for the OData extension for the Restlet framework.<br>
 * 
<#if metadata.metadataRef??> * @see <a href="${metadata.metadataRef}">Metadata of the target OData service</a></#if>
 * 
 */
</#compress>

public <#if type.abstractType>abstract </#if>class ${className} {

<#list type.properties?sort_by("name") as property>
  <#if property.type??>
    private ${property.type.className} ${property.propertyName}<#if property.defaultValue??> = property.defaultValue</#if>;
  <#else>
    // private [error: no defined type] ${property.propertyName}<#if property.defaultValue??> = property.defaultValue</#if>;
  </#if>
</#list>
<#list type.complexProperties?sort_by("name") as property>
  <#if property.complexType??>
    private ${property.complexType.className} ${property.propertyName};
  <#else>
    // private [error: no defined type] ${property.propertyName};
  </#if>
</#list>

    /**
     * Constructor without parameter.
     * 
     */
    public ${className}() {
        super();
    }
    
<#list type.properties?sort_by("name") as property>
  <#if property.type??>
   /**
    * Returns the value of the "${property.propertyName}" attribute.
    *
    * @return The value of the "${property.propertyName}" attribute.
    */
   <#if property.getterAccess??>${property.getterAccess}<#else>public</#if> ${property.type.className} get${property.normalizedName?cap_first}() {
      return ${property.propertyName};
   }
  </#if>

</#list>
<#list type.complexProperties?sort_by("name") as property>
   <#if property.complexType??>
   /**
    * Returns the value of the "${property.propertyName}" attribute.
    *
    * @return The value of the "${property.propertyName}" attribute.
    */
   <#if property.getterAccess??>${property.getterAccess}<#else>public</#if> ${property.complexType.className} get${property.normalizedName?cap_first}() {
      return ${property.propertyName};
   }
   </#if>   

</#list>
<#list type.properties?sort_by("name") as property>
  <#if property.type??>
   /**
    * Sets the value of the "${property.propertyName}" attribute.
    *
    * @param ${property.propertyName}
    *     The value of the "${property.normalizedName}" attribute.
    */
   <#if property.setterAccess??>${property.setterAccess}<#else>public</#if> void set${property.normalizedName?cap_first}(${property.type.className} ${property.propertyName}) {
      this.${property.propertyName} = ${property.propertyName};
   }
  </#if>

</#list>
<#list type.complexProperties?sort_by("name") as property>
   <#if property.complexType??>
   /**
    * Sets the value of the "${property.normalizedName}" attribute.
    *
    * @param ${property.propertyName}
    *     The value of the "${property.normalizedName}" attribute.
    */
   <#if property.setterAccess??>${property.setterAccess}<#else>public</#if> void set${property.normalizedName?cap_first}(${property.complexType.className} ${property.propertyName}) {
      this.${property.propertyName} = ${property.propertyName};
   }
   </#if>
   
</#list>
}