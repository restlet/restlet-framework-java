/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.supercsv;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

/**
 * Representation based on the SuperCSV library. It can serialize and
 * deserialize automatically in CSV.
 * 
 * @see <a href="http://supercsv.sourceforge.net/">SuperCSV project</a>
 * @author Jerome Louvel
 * @param <T>
 *            The type to wrap.
 */
public class SuperCsvRepresentation<T> extends WriterRepresentation {

    /** The (parsed) object to format. */
    private T object;

    /** The object class to instantiate. */
    private Class<T> objectClass;

    /** The JSON representation to parse. */
    private Representation csvRepresentation;

    /** The modifiable SuperCSV preferences. */
    private CsvPreference csvPreference;

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The target media type.
     * @param object
     *            The object to format.
     */
    @SuppressWarnings("unchecked")
    public SuperCsvRepresentation(MediaType mediaType, T object) {
        super(mediaType);
        this.object = object;
        this.objectClass = (Class<T>) ((object == null) ? null : object
                .getClass());
        this.csvRepresentation = null;
        this.csvPreference = null;
    }

    /**
     * Constructor.
     * 
     * @param representation
     *            The representation to parse.
     */
    public SuperCsvRepresentation(Representation representation,
            Class<T> objectClass) {
        super(representation.getMediaType());
        this.object = null;
        this.objectClass = objectClass;
        this.csvRepresentation = representation;
        this.csvPreference = null;
    }

    /**
     * Constructor.
     * 
     * @param object
     *            The object to format.
     */
    public SuperCsvRepresentation(T object) {
        this(MediaType.APPLICATION_JSON, object);
    }

    /**
     * Creates a SuperCSV preference. By default, it returns
     * {@link CsvPreference#EXCEL_PREFERENCE}.
     * 
     * @return The Jackson object mapper.
     */
    protected CsvPreference createCsvPreference() {
        return CsvPreference.EXCEL_PREFERENCE;
    }

    /**
     * Returns the wrapped object, deserializing the representation with
     * SuperCSV if necessary.
     * 
     * @return The wrapped object.
     */
    public T getObject() {
        T result = null;

        if (this.object != null) {
            result = this.object;
        } else if (this.csvRepresentation != null) {
            try {
                CsvBeanReader beanReader = new CsvBeanReader(
                        this.csvRepresentation.getReader(),
                        CsvPreference.EXCEL_PREFERENCE);
                String[] header = beanReader.getCSVHeader(true);
                result = beanReader.read(getObjectClass(), header);
            } catch (IOException e) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Unable to parse the object with Jackson.", e);
            }
        }

        return result;
    }

    /**
     * Returns the object class to instantiate.
     * 
     * @return The object class to instantiate.
     */
    public Class<T> getObjectClass() {
        return objectClass;
    }

    /**
     * Returns the modifiable SuperCSV preferences. Useful to customize
     * mappings.
     * 
     * @return The modifiable SuperCSV preferences.
     */
    public CsvPreference getCsvPreference() {
        if (this.csvPreference == null) {
            this.csvPreference = createCsvPreference();
        }

        return this.csvPreference;
    }

    /**
     * Sets the object to format.
     * 
     * @param object
     *            The object to format.
     */
    public void setObject(T object) {
        this.object = object;
    }

    /**
     * Sets the object class to instantiate.
     * 
     * @param objectClass
     *            The object class to instantiate.
     */
    public void setObjectClass(Class<T> objectClass) {
        this.objectClass = objectClass;
    }

    /**
     * Sets the SuperCSV preferences.
     * 
     * @param csvPreference
     *            The SuperCSV preferences.
     */
    public void setObjectMapper(CsvPreference csvPreference) {
        this.csvPreference = csvPreference;
    }

    @Override
    public void write(Writer writer) throws IOException {
        if (csvRepresentation != null) {
            csvRepresentation.write(writer);
        } else if (object != null) {
            
            for(Method method : object.getClass().getMethods()){
                if(method.getName().startsWith("get")){
                    
                }
                
            }
            
            new CsvBeanWriter(writer, getCsvPreference()).write(object);
        }
    }
}
