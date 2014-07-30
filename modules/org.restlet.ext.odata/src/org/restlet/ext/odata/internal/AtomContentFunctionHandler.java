package org.restlet.ext.odata.internal;

import java.io.IOException;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.restlet.Context;
import org.restlet.ext.odata.internal.edm.TypeUtils;
import org.restlet.ext.xml.format.XmlFormatParser;
import org.restlet.representation.Representation;

/**
 * This class is added for parsing the representation result provided by RESLET as a response in the 
 * respective object/ return type for the functions/actions.
 * 
 * @author Akshay
 */
public class AtomContentFunctionHandler extends XmlFormatParser implements FunctionContentHandler {

	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.internal.FunctionContentHandler#parseResult(java.lang.Class, org.restlet.representation.Representation, 
	 * java.lang.String, java.util.List)
	 */
	@SuppressWarnings({ "unused", "unchecked", "rawtypes" })
	public Object parseResult(Class<?> classType,
			Representation representation, String functionName, List<?> entity) {
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLEventReader reader = factory.createXMLEventReader(representation.getReader());

			while (reader.hasNext()) {
				XMLEvent event = reader.nextEvent();
				StartElement startElement = null;

				if (event.isEndElement()
						&& startElement != null
						&& event.asEndElement().getName()
								.equals(startElement.getName())) {
					break;
				}

				if (isStartElement(event, new QName(XmlFormatParser.NS_DATASERVICES, functionName))) {
					startElement = event.asStartElement();
				}

				if (event.isStartElement()
						&& event.asStartElement().getName().getNamespaceURI()
								.equals(NS_DATASERVICES)
						&& event.asStartElement().getName()
								.equals(DATASERVICES_ELEMENT)) {
					if (entity instanceof List) {
						Object value = TypeUtils.convert(classType,
								reader.getElementText());
						((List) entity).add(value);
					}
				}
			}
		} catch (XMLStreamException e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the xml due to Stream Exception: " + e.getMessage());
		} catch (IOException e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the xml due to IO Exception: " + e.getMessage());
		}
		return entity;
	}
			
}