package org.restlet.ext.xml.format;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.core4j.Enumerable;

/**
 * The Class XmlFormatParser.
 * 
 * @author <a href="mailto:onkar.dhuri@synerzip.com">Onkar Dhuri</a>
 */
public class XmlFormatParser {

  public static final String NS_APP = "http://www.w3.org/2007/app";
  public static final String NS_XML = "http://www.w3.org/XML/1998/namespace";
  public static final String NS_ATOM = "http://www.w3.org/2005/Atom";

  public static final String NS_METADATA = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";
  public static final String NS_DATASERVICES = "http://schemas.microsoft.com/ado/2007/08/dataservices";
  public static final String NS_SCHEME = "http://schemas.microsoft.com/ado/2007/08/dataservices/scheme";
  public static final String NS_RELATED = "http://schemas.microsoft.com/ado/2007/08/dataservices/related/";
  public static final String NS_EDM2006 = "http://schemas.microsoft.com/ado/2006/04/edm"; // edm 1.0
  public static final String NS_EDM2007 = "http://schemas.microsoft.com/ado/2007/05/edm"; // edm 1.1
  public static final String NS_EDM2008_1 = "http://schemas.microsoft.com/ado/2008/01/edm"; // edm 1.2
  public static final String NS_EDM2008_9 = "http://schemas.microsoft.com/ado/2008/09/edm"; // edm 2.0
  public static final String NS_EDM2009_8 = "http://schemas.microsoft.com/ado/2009/08/edm"; // edm ???
  public static final String NS_EDM2009_11 = "http://schemas.microsoft.com/ado/2009/11/edm"; // edm 3.0
  public static final String NS_EDMX = "http://schemas.microsoft.com/ado/2007/06/edmx";
  public static final String NS_EDMANNOTATION = "http://schemas.microsoft.com/ado/2009/02/edm/annotation";
  public static final String NS_CUSTOM_EDMANNOTATION = "http://www.lgc.com/dsdataserver/annotation";

  public static final QName EDMX_EDMX = new QName(NS_EDMX, "Edmx");
  public static final QName EDMX_DATASERVICES = new QName(NS_EDMX, "DataServices");

  public static final QName EDM2006_SCHEMA = new QName(NS_EDM2006, "Schema");
  public static final QName EDM2006_ENTITYTYPE = new QName(NS_EDM2006, "EntityType");
  public static final QName EDM2006_ASSOCIATION = new QName(NS_EDM2006, "Association");
  public static final QName EDM2006_COMPLEXTYPE = new QName(NS_EDM2006, "ComplexType");
  public static final QName EDM2006_ENTITYCONTAINER = new QName(NS_EDM2006, "EntityContainer");
  public static final QName EDM2006_ENTITYSET = new QName(NS_EDM2006, "EntitySet");
  public static final QName EDM2006_ASSOCIATIONSET = new QName(NS_EDM2006, "AssociationSet");
  public static final QName EDM2006_FUNCTIONIMPORT = new QName(NS_EDM2006, "FunctionImport");
  public static final QName EDM2006_PARAMETER = new QName(NS_EDM2006, "Parameter");
  public static final QName EDM2006_END = new QName(NS_EDM2006, "End");
  public static final QName EDM2006_ONDELETE = new QName(NS_EDM2006, "OnDelete");
  public static final QName EDM2006_REFCONSTRAINT = new QName(NS_EDM2006, "ReferentialConstraint");
  public static final QName EDM2006_PRINCIPAL = new QName(NS_EDM2006, "Principal");
  public static final QName EDM2006_DEPENDENT = new QName(NS_EDM2006, "Dependent");
  public static final QName EDM2006_PROPERTYREF = new QName(NS_EDM2006, "PropertyRef");
  public static final QName EDM2006_PROPERTY = new QName(NS_EDM2006, "Property");
  public static final QName EDM2006_NAVIGATIONPROPERTY = new QName(NS_EDM2006, "NavigationProperty");

  public static final QName EDM2007_SCHEMA = new QName(NS_EDM2007, "Schema");
  public static final QName EDM2007_ENTITYTYPE = new QName(NS_EDM2007, "EntityType");
  public static final QName EDM2007_ASSOCIATION = new QName(NS_EDM2007, "Association");
  public static final QName EDM2007_COMPLEXTYPE = new QName(NS_EDM2007, "ComplexType");
  public static final QName EDM2007_ENTITYCONTAINER = new QName(NS_EDM2007, "EntityContainer");
  public static final QName EDM2007_ENTITYSET = new QName(NS_EDM2007, "EntitySet");
  public static final QName EDM2007_ASSOCIATIONSET = new QName(NS_EDM2007, "AssociationSet");
  public static final QName EDM2007_FUNCTIONIMPORT = new QName(NS_EDM2007, "FunctionImport");
  public static final QName EDM2007_PARAMETER = new QName(NS_EDM2007, "Parameter");
  public static final QName EDM2007_END = new QName(NS_EDM2007, "End");
  public static final QName EDM2007_ONDELETE = new QName(NS_EDM2007, "OnDelete");
  public static final QName EDM2007_REFCONSTRAINT = new QName(NS_EDM2007, "ReferentialConstraint");
  public static final QName EDM2007_PRINCIPAL = new QName(NS_EDM2007, "Principal");
  public static final QName EDM2007_DEPENDENT = new QName(NS_EDM2007, "Dependent");
  public static final QName EDM2007_PROPERTYREF = new QName(NS_EDM2007, "PropertyRef");
  public static final QName EDM2007_PROPERTY = new QName(NS_EDM2007, "Property");
  public static final QName EDM2007_NAVIGATIONPROPERTY = new QName(NS_EDM2007, "NavigationProperty");

  public static final QName EDM2008_1_SCHEMA = new QName(NS_EDM2008_1, "Schema");
  public static final QName EDM2008_1_ENTITYTYPE = new QName(NS_EDM2008_1, "EntityType");
  public static final QName EDM2008_1_ASSOCIATION = new QName(NS_EDM2008_1, "Association");
  public static final QName EDM2008_1_COMPLEXTYPE = new QName(NS_EDM2008_1, "ComplexType");
  public static final QName EDM2008_1_ENTITYCONTAINER = new QName(NS_EDM2008_1, "EntityContainer");
  public static final QName EDM2008_1_ENTITYSET = new QName(NS_EDM2008_1, "EntitySet");
  public static final QName EDM2008_1_ASSOCIATIONSET = new QName(NS_EDM2008_1, "AssociationSet");
  public static final QName EDM2008_1_FUNCTIONIMPORT = new QName(NS_EDM2008_1, "FunctionImport");
  public static final QName EDM2008_1_PARAMETER = new QName(NS_EDM2008_1, "Parameter");
  public static final QName EDM2008_1_END = new QName(NS_EDM2008_1, "End");
  public static final QName EDM2008_1_ONDELETE = new QName(NS_EDM2008_1, "OnDelete");
  public static final QName EDM2008_1_REFCONSTRAINT = new QName(NS_EDM2008_1, "ReferentialConstraint");
  public static final QName EDM2008_1_PRINCIPAL = new QName(NS_EDM2008_1, "Principal");
  public static final QName EDM2008_1_DEPENDENT = new QName(NS_EDM2008_1, "Dependent");
  public static final QName EDM2008_1_PROPERTYREF = new QName(NS_EDM2008_1, "PropertyRef");
  public static final QName EDM2008_1_PROPERTY = new QName(NS_EDM2008_1, "Property");
  public static final QName EDM2008_1_NAVIGATIONPROPERTY = new QName(NS_EDM2008_1, "NavigationProperty");

  public static final QName EDM2008_9_SCHEMA = new QName(NS_EDM2008_9, "Schema");
  public static final QName EDM2008_9_ENTITYTYPE = new QName(NS_EDM2008_9, "EntityType");
  public static final QName EDM2008_9_ASSOCIATION = new QName(NS_EDM2008_9, "Association");
  public static final QName EDM2008_9_COMPLEXTYPE = new QName(NS_EDM2008_9, "ComplexType");
  public static final QName EDM2008_9_ENTITYCONTAINER = new QName(NS_EDM2008_9, "EntityContainer");
  public static final QName EDM2008_9_ENTITYSET = new QName(NS_EDM2008_9, "EntitySet");
  public static final QName EDM2008_9_ASSOCIATIONSET = new QName(NS_EDM2008_9, "AssociationSet");
  public static final QName EDM2008_9_FUNCTIONIMPORT = new QName(NS_EDM2008_9, "FunctionImport");
  public static final QName EDM2008_9_PARAMETER = new QName(NS_EDM2008_9, "Parameter");
  public static final QName EDM2008_9_END = new QName(NS_EDM2008_9, "End");
  public static final QName EDM2008_9_ONDELETE = new QName(NS_EDM2008_9, "OnDelete");
  public static final QName EDM2008_9_REFCONSTRAINT = new QName(NS_EDM2008_9, "ReferentialConstraint");
  public static final QName EDM2008_9_PRINCIPAL = new QName(NS_EDM2008_9, "Principal");
  public static final QName EDM2008_9_DEPENDENT = new QName(NS_EDM2008_9, "Dependent");
  public static final QName EDM2008_9_PROPERTYREF = new QName(NS_EDM2008_9, "PropertyRef");
  public static final QName EDM2008_9_PROPERTY = new QName(NS_EDM2008_9, "Property");
  public static final QName EDM2008_9_NAVIGATIONPROPERTY = new QName(NS_EDM2008_9, "NavigationProperty");

  public static final QName EDM2009_8_SCHEMA = new QName(NS_EDM2009_8, "Schema");
  public static final QName EDM2009_8_ENTITYTYPE = new QName(NS_EDM2009_8, "EntityType");
  public static final QName EDM2009_8_ASSOCIATION = new QName(NS_EDM2009_8, "Association");
  public static final QName EDM2009_8_COMPLEXTYPE = new QName(NS_EDM2009_8, "ComplexType");
  public static final QName EDM2009_8_ENTITYCONTAINER = new QName(NS_EDM2009_8, "EntityContainer");
  public static final QName EDM2009_8_ENTITYSET = new QName(NS_EDM2009_8, "EntitySet");
  public static final QName EDM2009_8_ASSOCIATIONSET = new QName(NS_EDM2009_8, "AssociationSet");
  public static final QName EDM2009_8_FUNCTIONIMPORT = new QName(NS_EDM2009_8, "FunctionImport");
  public static final QName EDM2009_8_PARAMETER = new QName(NS_EDM2009_8, "Parameter");
  public static final QName EDM2009_8_END = new QName(NS_EDM2009_8, "End");
  public static final QName EDM2009_8_ONDELETE = new QName(NS_EDM2009_8, "OnDelete");
  public static final QName EDM2009_8_REFCONSTRAINT = new QName(NS_EDM2009_8, "ReferentialConstraint");
  public static final QName EDM2009_8_PRINCIPAL = new QName(NS_EDM2009_8, "Principal");
  public static final QName EDM2009_8_DEPENDENT = new QName(NS_EDM2009_8, "Dependent");
  public static final QName EDM2009_8_PROPERTYREF = new QName(NS_EDM2009_8, "PropertyRef");
  public static final QName EDM2009_8_PROPERTY = new QName(NS_EDM2009_8, "Property");
  public static final QName EDM2009_8_NAVIGATIONPROPERTY = new QName(NS_EDM2009_8, "NavigationProperty");

  public static final QName EDM2009_11_SCHEMA = new QName(NS_EDM2009_11, "Schema");
  public static final QName EDM2009_11_ENTITYTYPE = new QName(NS_EDM2009_11, "EntityType");
  public static final QName EDM2009_11_ASSOCIATION = new QName(NS_EDM2009_11, "Association");
  public static final QName EDM2009_11_COMPLEXTYPE = new QName(NS_EDM2009_11, "ComplexType");
  public static final QName EDM2009_11_ENTITYCONTAINER = new QName(NS_EDM2009_11, "EntityContainer");
  public static final QName EDM2009_11_ENTITYSET = new QName(NS_EDM2009_11, "EntitySet");
  public static final QName EDM2009_11_ASSOCIATIONSET = new QName(NS_EDM2009_11, "AssociationSet");
  public static final QName EDM2009_11_FUNCTIONIMPORT = new QName(NS_EDM2009_11, "FunctionImport");
  public static final QName EDM2009_11_PARAMETER = new QName(NS_EDM2009_11, "Parameter");
  public static final QName EDM2009_11_END = new QName(NS_EDM2009_11, "End");
  public static final QName EDM2009_11_ONDELETE = new QName(NS_EDM2009_11, "OnDelete");
  public static final QName EDM2009_11_REFCONSTRAINT = new QName(NS_EDM2009_11, "ReferentialConstraint");
  public static final QName EDM2009_11_PRINCIPAL = new QName(NS_EDM2009_11, "Principal");
  public static final QName EDM2009_11_DEPENDENT = new QName(NS_EDM2009_11, "Dependent");
  public static final QName EDM2009_11_PROPERTYREF = new QName(NS_EDM2009_11, "PropertyRef");
  public static final QName EDM2009_11_PROPERTY = new QName(NS_EDM2009_11, "Property");
  public static final QName EDM2009_11_NAVIGATIONPROPERTY = new QName(NS_EDM2009_11, "NavigationProperty");

  public static final QName ATOM_FEED = new QName(NS_ATOM, "feed");
  public static final QName ATOM_ENTRY = new QName(NS_ATOM, "entry");
  public static final QName ATOM_ID = new QName(NS_ATOM, "id");
  public static final QName ATOM_TITLE = new QName(NS_ATOM, "title");
  public static final QName ATOM_SUMMARY = new QName(NS_ATOM, "summary");
  public static final QName ATOM_UPDATED = new QName(NS_ATOM, "updated");
  public static final QName ATOM_CATEGORY = new QName(NS_ATOM, "category");
  public static final QName ATOM_CONTENT = new QName(NS_ATOM, "content");
  public static final QName ATOM_LINK = new QName(NS_ATOM, "link");
  public static final QName ATOM_AUTHOR = new QName(NS_ATOM, "author");
  public static final QName ATOM_NAME = new QName(NS_ATOM, "name");
  public static final QName ATOM_EMAIL = new QName(NS_ATOM, "email");

  public static final QName APP_WORKSPACE = new QName(NS_APP, "workspace");
  public static final QName APP_SERVICE = new QName(NS_APP, "service");
  public static final QName APP_COLLECTION = new QName(NS_APP, "collection");
  public static final QName APP_ACCEPT = new QName(NS_APP, "accept");

  public static final QName M_ETAG = new QName(NS_METADATA, "etag");
  public static final QName M_PROPERTIES = new QName(NS_METADATA, "properties");
  public static final QName M_ACTION = new QName(NS_METADATA, "action");
  public static final QName M_FUNCTION = new QName(NS_METADATA, "function");
  public static final QName M_TYPE = new QName(NS_METADATA, "type");
  public static final QName M_NULL = new QName(NS_METADATA, "null");
  public static final QName M_INLINE = new QName(NS_METADATA, "inline");
  public static final QName M_MIMETYPE = new QName(NS_METADATA, "MimeType");
  public static final QName M_FC_TARGETPATH = new QName(NS_METADATA, "FC_TargetPath");
  public static final QName M_FC_CONTENTKIND = new QName(NS_METADATA, "FC_ContentKind");
  public static final QName M_FC_KEEPINCONTENT = new QName(NS_METADATA, "FC_KeepInContent");
  public static final QName M_FC_EPMCONTENTKIND = new QName(NS_METADATA, "FC_EpmContentKind");
  public static final QName M_FC_EPMKEEPINCONTENT = new QName(NS_METADATA, "FC_EpmKeepInContent");
  public static final QName M_FC_NSPREFIX = new QName(NS_METADATA, "FC_NsPrefix");
  public static final QName M_FC_NSURI = new QName(NS_METADATA, "FC_NsUri");

  public static final QName DATASERVICES_ELEMENT = new QName(NS_DATASERVICES, "element"); // a collection element

  public static final QName XML_BASE = new QName(NS_XML, "base");

  protected static boolean isStartElement(XMLEvent event, QName... names) {
    if (!event.isStartElement()) {
      return false;
    }
    QName name = new QName(event.asStartElement().getName().getNamespaceURI(), event.asStartElement().getName().getLocalPart());
    return Enumerable.create(names).contains(name);

  }

  protected static boolean isElement(XMLEvent event, QName... names) {
    QName name = new QName(event.asStartElement().getName().getNamespaceURI(), event.asStartElement().getName().getLocalPart());
    return Enumerable.create(names).contains(name);

  }

  protected static boolean isEndElement(XMLEvent event, QName qname) {
    if (!event.isEndElement()) {
      return false;
    }
    QName name = event.asEndElement().getName();
    return name.getNamespaceURI().equals(qname.getNamespaceURI())
        && name.getLocalPart().equals(qname.getLocalPart());
  }

  protected static String urlCombine(String base, String rel) {
    if (!base.endsWith("/") && !rel.startsWith("/"))
      base = base + "/";
    return base + rel;
  }

  protected static String getAttributeValueIfExists(StartElement element, String localName) {
    return getAttributeValueIfExists(element, new QName(null, localName));
  }

  protected static String getAttributeValueIfExists(StartElement element, QName attName) {
    Attribute rt = element.getAttributeByName(attName);
    return rt == null ? null : rt.getValue();
  }
  
  protected static boolean isStartElement(XMLStreamReader reader, QName... names) {
    if (!reader.isStartElement()) {
      return false;
    }
    QName name = new QName(reader.getNamespaceURI(), reader.getLocalName());
    return Enumerable.create(names).contains(name);

  }
  
  protected static boolean isElement(XMLStreamReader reader, QName... names) {
    QName name = new QName(reader.getNamespaceURI(), reader.getLocalName());
    return Enumerable.create(names).contains(name);

  }

  protected static boolean isEndElement(XMLStreamReader reader, QName qname) {
    if (!reader.isEndElement()) {
      return false;
    }
    QName name = new QName(reader.getNamespaceURI(), reader.getLocalName());
    return name.getNamespaceURI().equals(qname.getNamespaceURI())
        && name.getLocalPart().equals(qname.getLocalPart());
  }
  
  protected static String getAttributeValueIfExists(XMLStreamReader reader, String attName) {
    return reader.getAttributeValue(null, attName);
  }

}
