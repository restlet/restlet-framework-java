package org.restlet.ext.odata.internal;

import java.io.IOException;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.restlet.Context;
import org.restlet.ext.odata.xml.AtomFeedHandler;
import org.restlet.ext.xml.format.XmlFormatParser;
import org.restlet.representation.Representation;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


/**
 * This class is added for parsing the representation result provided by RESLET as a response in the 
 * expected object/return type for the functions/actions.
 * 
 * @author Shantanu
 */
public class JsonContentFunctionHandler extends XmlFormatParser implements FunctionContentHandler {

	
	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.internal.FunctionContentHandler#parseResult(java.lang.Class, org.restlet.representation.Representation, 
	 * java.lang.String, java.util.List)
	 */
	public Object parseResult(Class<?> c, Representation representation, String functionName,List<?> entity) {
		try {
			String jsonString = null;
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLEventReader eventReader;
			eventReader = factory.createXMLEventReader(representation.getReader());

			while (eventReader.hasNext()) {
				XMLEvent event;
				event = eventReader.nextEvent();
				if(isStartElement(event, new QName(XmlFormatParser.NS_DATASERVICES, functionName))){
					jsonString = AtomFeedHandler.innerText(eventReader, event.asStartElement());
				}
			}

			if (jsonString != null && !jsonString.equals("")) {
				StringBuilder jsonStrBuilder = new StringBuilder();
				jsonStrBuilder.append("{");
				int beginIndex = jsonString.indexOf("{");
				int endIndex = jsonString.indexOf("}");
				String jsonStringWithoutBraces = jsonString.substring(beginIndex + 1,	endIndex);

				if (jsonStringWithoutBraces != null) {
					String[] propertiesSplit = jsonStringWithoutBraces.trim().split(",");

					for (int i = 0; i < propertiesSplit.length; i++) {
						String value = propertiesSplit[i];

						if (value.contains(":")) {
							String[] split = value.trim().split(":");
							String valueToNormalise = split[0].trim();
							String normalisedValue = valueToNormalise.substring(0, 1)
									+ valueToNormalise.substring(1, 2).toLowerCase()
									+ valueToNormalise.substring(2);
							jsonStrBuilder.append(normalisedValue);
							jsonStrBuilder.append(":");
							jsonStrBuilder.append(split[1]);
						}

						if (!(i == (propertiesSplit.length - 1))) {
							jsonStrBuilder.append(",");
						}
					}
				}

				jsonStrBuilder.append("}");
				return new Gson().fromJson(jsonStrBuilder.toString(), c);
			}
		} catch (XMLStreamException e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the xml due to Stream Exception: " + e.getMessage());
		} catch (IOException e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the xml due to IO Exception: " + e.getMessage());
		} catch (JsonSyntaxException e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the xml due to Json Syntax Exception: " + e.getMessage());
		}
		return null;
	}

}
