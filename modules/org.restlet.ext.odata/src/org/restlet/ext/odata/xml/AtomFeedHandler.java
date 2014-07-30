package org.restlet.ext.odata.xml;

import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.restlet.Context;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.atom.Content;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Link;
import org.restlet.ext.atom.Person;
import org.restlet.ext.atom.Relation;
import org.restlet.ext.odata.internal.edm.EntitySet;
import org.restlet.ext.odata.internal.edm.EntityType;
import org.restlet.ext.odata.internal.edm.Mapping;
import org.restlet.ext.odata.internal.edm.Metadata;
import org.restlet.ext.odata.internal.edm.TypeUtils;
import org.restlet.ext.odata.internal.reflect.ReflectUtils;
import org.restlet.ext.odata.streaming.StreamReference;
import org.restlet.ext.xml.format.FormatParser;
import org.restlet.ext.xml.format.XmlFormatParser;

/**
 * The Class AtomFeedHandler which parses the AtomFeed using STAX Event Iterator model.
 *
 * @param <T> the generic type 
 * 
 * @author <a href="mailto:onkar.dhuri@synerzip.com">Onkar Dhuri</a>
 */
public class AtomFeedHandler<T> extends XmlFormatParser implements
		FormatParser<Feed> {

	/** The metadata. */
	protected Metadata metadata;
	
	/** The entity set name. */
	protected String entitySetName;
	
	/** The entity type. */
	private EntityType entityType;
	
	/** The entity class. */
	private Class<?> entityClass;
	
	/** The entities. */
	private List<T> entities;
	
	/** The references from the entry to Web resources. */
    private volatile List<Link> links;
    
    /** The feed. */
    private Feed feed;
    
	 /** The baseURL. */
    private String baseURL;
    
	/**
	 * Gets the entities.
	 *
	 * @return the entities
	 */
	public List<T> getEntities() {
		return entities;
	}
	
	/**
	 * Gets the feed.
	 *
	 * @return the feed
	 */
	public Feed getFeed(){
		if(feed == null){
			feed = new Feed();
		}
		return feed;
	}
	
	/**
	 * Sets the feed.
	 *
	 * @param feed the new feed
	 */
	public void setFeed(Feed feed){
		this.feed = feed;
	}
	
	/**
     * Returns the references from the entry to Web resources.
     * 
     * @return The references from the entry to Web resources.
     */
    public List<Link> getLinks() {
        // Lazy initialization with double-check.
        List<Link> l = this.links;
        if (l == null) {
            synchronized (this) {
                l = this.links;
                if (l == null) {
                    this.links = l = new ArrayList<Link>();
                }
            }
        }
        return l;
    }

	/**
	 * Instantiates a new atom feed handler.
	 *
	 * @param entitySetName the entity set name
	 * @param entityType the entity type
	 * @param entityClass the entity class
	 */
	public AtomFeedHandler(String entitySetName, EntityType entityType, Class<?> entityClass) {
		this.entitySetName = entitySetName;
		this.entityType = entityType;
		this.entityClass = entityClass;
		this.entities = new ArrayList<T>();
	}
	
	/**
	 * Instantiates a new atom feed handler.
	 *
	 * @param entitySetName the entity set name
	 * @param entityType the entity type
	 * @param entityClass the entity class
	 * @param metadata the metadata
	 */
	public AtomFeedHandler(String entitySetName, EntityType entityType, Class<?> entityClass, Metadata metadata) {
		this.entitySetName = entitySetName;
		this.entityType = entityType;
		this.entityClass = entityClass;
		this.entities = new ArrayList<T>();
		this.metadata = metadata;
	}

	/* (non-Javadoc)
	 * @see org.restlet.ext.xml.format.FormatParser#parse(java.io.Reader)
	 */
	public Feed parse(Reader reader) {
		return parseFeed(reader, getEntitySet());
	}

	/**
	 * Parses the feed.
	 *
	 * @param reader the reader
	 * @param entitySet the entity set
	 * @return the feed
	 */
	public Feed parseFeed(Reader reader, EntitySet entitySet) {
		try {

			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLEventReader eventReader = factory.createXMLEventReader(reader);

			while (eventReader.hasNext()) {
				XMLEvent event;
				event = eventReader.nextEvent();

				if (isStartElement(event, ATOM_ENTRY)) {
					@SuppressWarnings("unchecked")
					// create instance of entity, parse it and add it to list of entities
					T entity = (T) entityClass.newInstance();
					this.parseEntry(eventReader, event.asStartElement(), entitySet, entity);
					this.entities.add(entity);

				} else if (isStartElement(event, ATOM_LINK)) {
					if ("next".equals(event.asStartElement()
							.getAttributeByName(new QName("rel")).getValue())) {
						this.getFeed().setBaseReference(event.asStartElement()
								.getAttributeByName(new QName("href"))
								.getValue());
					}
					parseAtomFeedLinks(event);
				} else if (isStartElement(event, ATOM_FEED)) {
					Attribute attributeByName = event.asStartElement().getAttributeByName(XML_BASE);
					if(attributeByName!=null){
						baseURL=attributeByName.getValue();
					}			
				} else if (isEndElement(event, ATOM_FEED)) {
					// return from a sub feed, if we went down the hierarchy
					break;
				}

			}
		} catch (XMLStreamException e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the feed due to: " + e.getMessage());
		} catch (InstantiationException e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the feed due to: " + e.getMessage());
		} catch (IllegalAccessException e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the feed due to: " + e.getMessage());
		}

		return this.getFeed();

	}

	/**
	 * Method to parse atom links.
	 * @param event
	 */
	private void parseAtomFeedLinks(XMLEvent event) {
		Link link = new Link();
		link.setHref(new Reference(event.asStartElement()
				.getAttributeByName(new QName("href"))
				.getValue()));
		link.setRel(Relation.valueOf(event.asStartElement()
				.getAttributeByName(new QName("rel"))
				.getValue()));
		if(null != event.asStartElement()
				.getAttributeByName(new QName("type"))){
			String type = event.asStartElement()
					.getAttributeByName(new QName("type"))
					.getValue();
			if (type != null && type.length() > 0) {
				link.setType(new MediaType(type));
			}
		}
		
		if(null != event.asStartElement()
				.getAttributeByName(new QName("hreflang"))){
			link.setHrefLang(new Language(event.asStartElement()
					.getAttributeByName(new QName("hreflang"))
					.getValue()));
		}
		
		if(null != event.asStartElement()
				.getAttributeByName(new QName("title"))){
			link.setTitle(event.asStartElement()
					.getAttributeByName(new QName("title"))
					.getValue());
		}
		
		if(null != event.asStartElement()
				.getAttributeByName(new QName("length"))){
			final String attr = event.asStartElement()
					.getAttributeByName(new QName("length"))
					.getValue();
			link.setLength((attr == null) ? -1L : Long.parseLong(attr));
		}
		
		
		// Glean the content
		Content currentContent = new Content();
		// Content available inline
		//initiateInlineMixedContent();
		link.setContent(currentContent);
		
		
		getFeed().getLinks().add(link);
	}
	
	

	/**
	 * Parses all properties of an entity.
	 *
	 * @param <T> the generic type
	 * @param reader the reader
	 * @param propertiesElement the properties element
	 * @param entity the entity
	 * @return the t
	 */
	public static <T> T parseProperties(
			XMLEventReader reader, StartElement propertiesElement, T entity) {

		try {
			String propertyName = null;
			while (reader.hasNext()) {
				XMLEvent event = reader.nextEvent();
	
				if (event.isEndElement()
						&& event.asEndElement().getName()
								.equals(propertiesElement.getName())) {
					return entity;
				}
				
				if (event.isStartElement()
						&& event.asStartElement().getName().getNamespaceURI()
								.equals(NS_DATASERVICES)) {
	
					String name = event.asStartElement().getName().getLocalPart();
					propertyName = ReflectUtils.normalize(name);
					//Check if that property is java reserved key word. If so then prefix it with '_' 
					if(ReflectUtils.isReservedWord(propertyName)){
						propertyName="_"+propertyName;
					}
					parsePropertiesByType(reader, entity, propertyName, event);
				}
			}
		} catch (Exception e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the properties due to: " + e.getMessage());
			throw new RuntimeException();
		}
		return entity;
	}

	/** Method to parse properties of different types.
	 * @param reader
	 * @param entity
	 * @param propertyName
	 * @param event
	 * @throws XMLStreamException
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private static <T> void parsePropertiesByType(XMLEventReader reader, T entity, String propertyName, XMLEvent event) {
		try {
			Object value = null;
			Attribute typeAttribute = event.asStartElement()
					.getAttributeByName(M_TYPE);
			Attribute nullAttribute = event.asStartElement()
					.getAttributeByName(M_NULL);
			boolean isNull = nullAttribute != null
					&& "true".equals(nullAttribute.getValue());
			if (typeAttribute == null) { // Simple String
				value = reader.getElementText();
			} else if (typeAttribute.getValue().toLowerCase().startsWith("edm")
					&& !isNull) { // EDM Type
				value = TypeUtils.fromEdm(reader.getElementText(),
						typeAttribute.getValue());
			} else if (typeAttribute.getValue().toLowerCase()
					.startsWith("collection")) {// collection type
				Object o = ReflectUtils.getPropertyObject(entity, propertyName);
				// Delegate the collection handling to respective handler.
				CollectionPropertyHandler.parse(reader, o,
						event.asStartElement(), entity);
			} else if (!isNull) {// complex type
				// get or create the property instance
				Object o = ReflectUtils.getPropertyObject(entity, propertyName);
				// populate the object
				parseProperties(reader, event.asStartElement(), (T) o);
				// set it back to parent entity
				ReflectUtils.invokeSetter(entity, propertyName, o);
			}
			if (value != null) {
				ReflectUtils.invokeSetter(entity, propertyName, value);
			}
		} catch (XMLStreamException e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the property due to: " + e.getMessage());
		} catch (Exception e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the property due to: " + e.getMessage());
		}
	}

	/**
	 * Parses the ds atom entry.
	 *
	 * @param entityType the entity type
	 * @param reader the reader
	 * @param event the event
	 * @param entity the entity
	 */
	private void parseDSAtomEntry(EntityType entityType, XMLEventReader reader, XMLEvent event, T entity) {
		// as end element is not included in parseProperties, we need a wrapper method around it to handle it.
		AtomFeedHandler.parseProperties(reader, event.asStartElement(), entity);
	}

	
	/**
	 * API will provide the Text for the given element 
	 * 
	 * @param reader - XMLEventReader
	 * @param element - StartElement
	 * @return String 
	 */
	public static String innerText(XMLEventReader reader, StartElement element) {
		try {
			StringWriter sw = new StringWriter();
			while (reader.hasNext()) {

				XMLEvent event = reader.nextEvent();
				if (event.isEndElement()
						&& event.asEndElement().getName()
								.equals(element.getName())) {

					return sw.toString();
				} else {
					sw.append(event.asCharacters().getData());
				}

			}
		} catch (XMLStreamException e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the inner content due to: " + e.getMessage());
		}
		throw new RuntimeException();
	}



	/**
	 * Gets the entity set.
	 *
	 * @return the entity set
	 */
	private EntitySet getEntitySet() {		
		EntitySet entitySet = new EntitySet(entitySetName);
		entitySet.setType(entityType);
		
		return entitySet;
	}

	/**
	 * Parses the entry.
	 *
	 * @param reader the reader
	 * @param entryElement the entry element
	 * @param entitySet the entity set
	 * @param entity the entity
	 * @return the t
	 */
	@SuppressWarnings("rawtypes")
	private T parseEntry(XMLEventReader reader,
			StartElement entryElement, EntitySet entitySet, T entity) {

		String id = null;
		String title = null;
		String summary = null;
		String updated = null;
		String contentType = null;
		Person p = null;
		String relativeURL=null;
		//read the base URL form Feed/Entry
		if(null==baseURL){
			Attribute attributeByName = entryElement.getAttributeByName(XML_BASE);
			if(attributeByName!=null){
				baseURL=attributeByName.getValue();
			}			
		}
		

		Entry rt = new Entry();

		while (reader.hasNext()) {
			try {
				XMLEvent event;
				event = reader.nextEvent();
				if (event.isEndElement()
						&& event.asEndElement().getName()
								.equals(entryElement.getName())) {
					rt.setId(id); // http://localhost:8810/Oneoff01.svc/Comment(1)
					rt.setTitle(title);
					rt.setSummary(summary);
					rt.setUpdated(Date.valueOf(updated.split("T")[0]));	
					this.getFeed().getEntries().add(rt);
					if(p!=null){
						//set author details in the entity.
						setDetailsOfPerson(entity, p, null); // third parameter would be null if we don't have contributor object.
					}
					return entity;
				}
	
				if (isStartElement(event, ATOM_ID)) {
					id = reader.getElementText();
				} else if (isStartElement(event, ATOM_TITLE)) {
					title = reader.getElementText();
				} else if (isStartElement(event, ATOM_SUMMARY)) {
					summary = reader.getElementText();
				} else if (isStartElement(event, ATOM_UPDATED)) {
					updated = reader.getElementText();
				} else if (isStartElement(event, ATOM_LINK)) {
					Link link = parseAtomLink(reader, event.asStartElement(),
							entitySet, entity);
					rt.getLinks().add(link);
				} else if (isStartElement(event, M_PROPERTIES)) {
					parseDSAtomEntry(entitySet.getType(), reader, event, entity);
				/*} else if (isStartElement(event, M_ACTION)) {
					AtomFunction function = parseAtomFunction(reader,
							event.asStartElement());
					actions.put(function.getFQFunctionName(), metadata
							.findFunctionImport(function.title,
									entitySet.getType(), FunctionKind.Action));
				} else if (isStartElement(event, M_FUNCTION)) {
					AtomFunction function = parseAtomFunction(reader,
							event.asStartElement());
					functions.put(function.getFQFunctionName(), metadata
							.findFunctionImport(function.title,
									entitySet.getType(), FunctionKind.Function));*/
				} else if(isStartElement(event, ATOM_AUTHOR)){ 
					// handle Author tag completely and create person object.
					p = new Person();
					parseAuthor(reader, p, event);
				} else if (isStartElement(event, ATOM_CONTENT)) {
					contentType = getAttributeValueIfExists(event.asStartElement(),
							"type");
					relativeURL = getAttributeValueIfExists(event.asStartElement(),
							"src");
					if (MediaType.APPLICATION_XML.getName().equals(contentType)) {
						StartElement contentElement = event.asStartElement();
						StartElement valueElement = null;
						while (reader.hasNext()) {
							// handle content in separate handlers
							XMLEvent event2;
							try {
								event2 = reader.nextEvent();
								if (valueElement == null && event2.isStartElement()) {
									valueElement = event2.asStartElement();
									if (isStartElement(event2, M_PROPERTIES)) {
										this.parseDSAtomEntry(entitySet.getType(), reader, event2, entity);
									} else {
										// TODO: Onkar : Set Basic content by implementing innerText method later
										//BasicAtomEntry bae = new BasicAtomEntry();
										Entry bae = new Entry();
										//bae.content = innerText(reader, event2.asStartElement());
										rt = bae;
									}
								}
								if (event2.isEndElement()
										&& event2.asEndElement().getName()
												.equals(contentElement.getName())) {
									break;
								}
							} catch (XMLStreamException e) {
								Context.getCurrentLogger().warning(
					                    "Cannot parse the entry due to: " + e.getMessage());
							}
						}
					} else {
						if(entityType.isBlob()){
							for (Field field : entity.getClass()
									.getDeclaredFields()) {
								Class<?> type = field.getType();							
								if(type.getName().contains("StreamReference")){
									Reference baseReference = new Reference(baseURL);
								    StreamReference streamReference = new StreamReference(baseReference,relativeURL);
								    streamReference.setContentType(contentType);
								    ReflectUtils.invokeSetter(entity,
												ReflectUtils.normalize(field.getName()), streamReference);
								    break;
								}
							}
						}
						Entry e = new Entry();
						// TODO: Onkar : Set Basic content by implementing innerText method later
						//e.setContent(innerText(reader, event.asStartElement()));
						rt = e;
					}
				} else if(event.toString() != null && event.toString().isEmpty() && event.toString().trim().isEmpty()){
					continue; 
				} else { // Handle Custom feeds where some properties are outside content tag. ie. not in m:properties
					if(event.isStartElement()){
						List<Mapping> mappings = metadata.getMappings();	
						Entry e = new Entry();
						StartElement element = event.asStartElement();
						String parentTag = element.getName().getLocalPart();
						
						// Iterate through inline attributes and set respective property for the entity.
						// Eg: <cafe:contact title="Chief"/></entry>
						String nsPrefix = element.getName().getPrefix();
						for (Iterator iterator1 =  element.getAttributes(); iterator1.hasNext();) {
							 Attribute attribute = (Attribute)iterator1.next();
							 String attributeValue = getAttributeValueIfExists(element, attribute.getName());
							 String attibuteTag = parentTag+"/@"+attribute.getName().getLocalPart();
							 for (Iterator iterator = mappings.iterator(); iterator.hasNext();) {
								Mapping mapping = (Mapping) iterator.next();
								if(null != mapping.getNsPrefix() && mapping.getNsPrefix().equalsIgnoreCase(nsPrefix)){
									if(attributeValue!= null & mapping.getValuePath().equalsIgnoreCase(attibuteTag)){
										String propertyName = ReflectUtils.normalize(mapping.getPropertyPath());
										ReflectUtils.invokeSetter(entity, propertyName, attributeValue);
										break;
									}
								}
							 }
						}
						
						
						StartElement valueElement = null;
						String propertyName = null;
						while(reader.hasNext()){
							
							XMLEvent event2;
							
							try {
								// iterate all the sub-elements under parent tag with 
								event2 = reader.nextEvent();
								if (event2.isEndElement()
										&& event2.asEndElement().getName()
												.equals(element.getName())) {
									break;
								}
								if (valueElement == null && event2.isStartElement()) {
									valueElement = event2.asStartElement();
									// Iterate through sub-paths and set respective property for the entity.
									// Eg:<cafe:Company><cafe:Name>Cafe corp.</cafe:Name></cafe:Company>
									String innerTag = parentTag+"/"+valueElement.getName().getLocalPart();
									for (Iterator iterator = mappings.iterator(); iterator.hasNext();) {
										Mapping mapping = (Mapping) iterator.next();
										if(mapping.getValuePath().equalsIgnoreCase(innerTag)){
											
											//TODO: Abhijeet : Extract this into method and refer it in parseProperties
											propertyName = ReflectUtils.normalize(mapping.getPropertyPath());
											parsePropertiesByType(reader, entity, propertyName, event2);
											break; // exit for loop if mapping is present and addressed
											
										}
									}
								}
							}catch (XMLStreamException e1) {
								Context.getCurrentLogger().warning(
					                    "Cannot parse the entry due to: " + e1.getMessage());
							} catch (Exception e1) {
								Context.getCurrentLogger().warning(
					                    "Cannot parse the entry due to: " + e1.getMessage());
							}
						}
						rt = e;
					}
				}
			} catch (XMLStreamException e1) {
				Context.getCurrentLogger().warning(
	                    "Cannot parse the entry due to: " + e1.getMessage());
			} catch (Exception e1) {
				Context.getCurrentLogger().warning(
	                    "Cannot parse the entry due to: " + e1.getMessage());
			}

		}
		throw new RuntimeException();
	}

	/**
	 * @param reader
	 * @param p
	 * @param event
	 */
	private void parseAuthor(XMLEventReader reader, Person p, XMLEvent event) {
		StartElement contentElement = event.asStartElement();
		StartElement valueElement = null;
		while (reader.hasNext()) {
			// handle content in separate handlers
			XMLEvent event2;
			try {
				event2 = reader.nextEvent();
				if (valueElement == null && event2.isStartElement()) {
					valueElement = event2.asStartElement();
					if (isStartElement(event2, ATOM_NAME)) {
						String name = reader.getElementText();
						if(!name.isEmpty()){
							p.setName(name);
						}
					}else if (isStartElement(event2, ATOM_EMAIL)) {
						String email = reader.getElementText();
						if(!email.isEmpty()){
							p.setEmail(email);
						}
					}
				}
				if (event2.isEndElement()
						&& event2.asEndElement().getName()
								.equals(contentElement.getName())) {
					break;
				}
			}catch (XMLStreamException e) {
				Context.getCurrentLogger().warning(
	                    "Cannot parse the author due to: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Method to set atom mappings to corresponding entity.
	 * @param entity
	 * @param author
	 * @param contributor
	 */
	private void setDetailsOfPerson(T entity, Person author, Person contributor) {
		 // Handle Atom mapped values.
        for (Mapping m : metadata.getMappings()) {
            if (entityType != null && entityType.equals(m.getType())
                    && m.getNsUri() == null && m.getNsPrefix() == null) {
            	
            	Object value = null;
                if ("SyndicationAuthorEmail".equals(m.getValuePath())) {
                    value = (author != null) ? author.getEmail() : null;
                } else if ("SyndicationAuthorName".equals(m.getValuePath())) {
                    value = (author != null) ? author.getName() : null;
                } else if ("SyndicationAuthorUri".equals(m.getValuePath())) {
                    value = (author != null) ? author.getUri().toString()
                            : null;
                } else if ("SyndicationContributorEmail".equals(m
                        .getValuePath())) {
                    value = (contributor != null) ? contributor.getEmail()
                            : null;
                } else if ("SyndicationContributorName"
                        .equals(m.getValuePath())) {
                    value = (contributor != null) ? contributor.getName()
                            : null;
                } else if ("SyndicationContributorUri".equals(m.getValuePath())) {
                    value = (contributor != null) ? contributor.getUri()
                            .toString() : null;
                } /*else if ("SyndicationPublished".equals(m.getValuePath())) {
                    value = entry.getPublished();
                } else if ("SyndicationRights".equals(m.getValuePath())) {
                    value = (entry.getRights() != null) ? entry.getRights()
                            .getContent() : null;
                } else if ("SyndicationSummary".equals(m.getValuePath())) {
                    value = entry.getSummary();
                } else if ("SyndicationTitle".equals(m.getValuePath())) {
                    value = (entry.getTitle() != null) ? entry.getTitle()
                            .getContent() : null;
                } else if ("SyndicationUpdated".equals(m.getValuePath())) {
                    value = entry.getUpdated();
                }*/
                
                try {
                    if (value != null) {
                        ReflectUtils.invokeSetter(entity, m.getPropertyPath(),
                                value);
                    }
                } catch (Exception e) {
					Context.getCurrentLogger().warning(
		                    "Cannot parse the content due to: " + e.getMessage());
                }
            }
        }
		
	}

	/**
	 * Parses the atom link.
	 *
	 * @param reader the reader
	 * @param linkElement the link element
	 * @param entitySet the entity set
	 * @param entity the entity
	 * @return the link
	 */
	private Link parseAtomLink(XMLEventReader reader, StartElement linkElement,
			EntitySet entitySet, T entity) {

		try {
			Link rt = new Link();
			rt.setRel(Relation.valueOf(getAttributeValueIfExists(linkElement,
					"rel")));
			rt.setType(MediaType.valueOf(getAttributeValueIfExists(linkElement,
					"type")));
			rt.setTitle(getAttributeValueIfExists(linkElement, "title"));
			rt.setHref(new Reference(getAttributeValueIfExists(linkElement,
					"href")));
			//boolean inlineContent = false;

			// expected cases:
			// 1. </link> - no inlined content, i.e. deferred
			// 2. <m:inline/></link> - inlined content but null entity or empty feed
			// 3. <m:inline><feed>...</m:inline></link> - inlined content with 1 or more items
			// 4. <m:inline><entry>..</m:inline></link> - inlined content 1 an item

			while (reader.hasNext()) {
				XMLEvent event;
				event = reader.nextEvent();

				if (event.isEndElement()
						&& event.asEndElement().getName()
								.equals(linkElement.getName())) {
					break;
				} else if (isStartElement(event, XmlFormatParser.M_INLINE)) {
					//inlineContent = true; // may be null content.
				} else if (isStartElement(event, ATOM_FEED)) {
					//skip all tags until we encounter first entry element to exclude link and other tags.
					// Fix for edit link tag to prevent pre-matured exit of parent link iteration. 
					while( reader.hasNext()){
						event = reader.nextEvent();
						if(isStartElement(event,ATOM_ENTRY)){
							String propertyName = rt.getHref().getLastSegment();
							// create a property object
							Object o = ReflectUtils.getPropertyObject(entity, propertyName);
							parseInlineEntities(reader, entitySet, entity, event, propertyName, o);
							// This break is added to not handle additional inline entities here. Those entries shall be handled in next else-if block.
							break; 
						} else if(event.isEndElement() && event.asEndElement().getName().equals(ATOM_FEED.getLocalPart())){
							break;
						}
					}
				} else if (isStartElement(event, ATOM_ENTRY)) { //handle the inline entity  
					String propertyName = rt.getHref().getLastSegment();
					// create a property object
					Object o = ReflectUtils.getPropertyObject(entity, propertyName);
					parseInlineEntities(reader, entitySet, entity, event, propertyName, o);
				}
			}
			return rt;
		} catch (XMLStreamException e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the atom link due to: " + e.getMessage());
		} catch (Exception e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the atom link due to: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Method to parse Inline entities.
	 * @param reader
	 * @param entitySet
	 * @param entity
	 * @param event
	 * @param propertyName
	 * @param o
	 * @throws NoSuchFieldException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws Exception
	 */
	@SuppressWarnings(value = {"unchecked", "rawtypes" })
	private void parseInlineEntities(XMLEventReader reader, EntitySet entitySet, T entity, XMLEvent event,
			String propertyName, Object o) {
		try {
			if (o instanceof List) { // Collection of complex i.e. one to many association
				Field field = entity.getClass().getDeclaredField(ReflectUtils.normalize(propertyName));
				// String currentMType = startElement.getAttributeByName(M_TYPE).getValue();
				// get the parameterize type using reflection
				if (field.getGenericType() instanceof ParameterizedType) {
					// determine what type of collection it is
					ParameterizedType listType = (ParameterizedType) field.getGenericType();
					Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];
					// Create new Item Instance
					Object obj;
					obj = listClass.newInstance();

					// create a new instance and populate the properties
					this.parseEntry(reader, event.asStartElement(), entitySet, (T) obj);
					((List) o).add(obj);
				}

			} else { // complex object i.e. embedded object in parent entity
				// populate the object
				this.parseEntry(reader, event.asStartElement(), entitySet, (T) o);
				// set it back to parent entity
				ReflectUtils.invokeSetter(entity, ReflectUtils.normalize(propertyName), o);
			}
		} catch (InstantiationException e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the inline entities due to: " + e.getMessage());
		} catch (IllegalAccessException e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the inline entities due to: " + e.getMessage());
		} catch (SecurityException e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the inline entities due to: " + e.getMessage());
		} catch (NoSuchFieldException e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the inline entities due to: " + e.getMessage());
		} catch (Exception e) {
			Context.getCurrentLogger().warning(
                    "Cannot parse the inline entities due to: " + e.getMessage());
		}
	}
	
}
