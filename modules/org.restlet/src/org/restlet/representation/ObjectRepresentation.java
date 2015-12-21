/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.representation;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.restlet.data.MediaType;

/**
 * Representation based on a serializable Java object.<br>
 * It supports binary representations of JavaBeans using the
 * {@link ObjectInputStream} and {@link ObjectOutputStream} classes. In this
 * case, it handles representations having the following media type:
 * {@link MediaType#APPLICATION_JAVA_OBJECT}
 * ("application/x-java-serialized-object"). It also supports textual
 * representations of JavaBeans using the {@link java.beans.XMLEncoder} and
 * {@link java.beans.XMLDecoder} classes. In this case, it handles
 * representations having the following media type:
 * {@link MediaType#APPLICATION_JAVA_OBJECT_XML}
 * ("application/x-java-serialized-object+xml").<br>
 * <br>
 * SECURITY WARNING: The usage of {@link java.beans.XMLDecoder} when
 * deserializing XML presentations from untrusted sources can lead to malicious
 * attacks. As pointed <a href=
 * "http://blog.diniscruz.com/2013/08/using-xmldecoder-to-execute-server-side.html"
 * >here</a>, the {@link java.beans.XMLDecoder} is able to force the JVM to
 * execute unwanted Java code described inside the XML file. Thus, the support
 * of such format has been disabled by default. You can activate this support by
 * turning on the following system property:
 * org.restlet.representation.ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED.<br>
 * <br>
 * SECURITY WARNING: The usage of {@link ObjectInputStream} when deserializing
 * binary presentations from untrusted sources can lead to malicious attacks. As
 * pointed <a
 * href="https://github.com/restlet/restlet-framework-java/issues/778"
 * >here</a>, the {@link ObjectInputStream} is able to force the JVM to execute
 * unwanted Java code. Thus, the support of such format has been disabled by
 * default. You can activate this support by turning on the following system
 * property: "org.restlet.representation.ObjectRepresentation
 * .VARIANT_OBJECT_BINARY_SUPPORTED".
 * 
 * @author Jerome Louvel
 * @param <T>
 *            The class to serialize, see {@link Serializable}
 */
public class ObjectRepresentation<T extends Serializable> extends
        OutputRepresentation {

    /** Indicates whether the JavaBeans XML deserialization is supported or not. */
    public static boolean VARIANT_OBJECT_XML_SUPPORTED = Boolean
            .getBoolean("org.restlet.representation.ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED");

    /**
     * Indicates whether the JavaBeans binary deserialization is supported or
     * not.
     */
    public static boolean VARIANT_OBJECT_BINARY_SUPPORTED = Boolean
            .getBoolean("org.restlet.representation.ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED");

    /** The serializable object. */
    private volatile T object;

    /**
     * Constructor reading the object from a serialized representation. This
     * representation must have the proper media type:
     * "application/x-java-serialized-object".
     * 
     * @param serializedRepresentation
     *            The serialized representation.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IllegalArgumentException
     */
    public ObjectRepresentation(Representation serializedRepresentation)
            throws IOException, ClassNotFoundException,
            IllegalArgumentException {
        this(serializedRepresentation, null);
    }

    /**
     * Constructor reading the object from a serialized representation. This
     * representation must have the proper media type:
     * "application/x-java-serialized-object".
     * 
     * @param serializedRepresentation
     *            The serialized representation.
     * @param classLoader
     *            The class loader used to read the object.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IllegalArgumentException
     */
    public ObjectRepresentation(Representation serializedRepresentation,
            final ClassLoader classLoader) throws IOException,
            ClassNotFoundException, IllegalArgumentException {
        this(serializedRepresentation, classLoader,
                VARIANT_OBJECT_BINARY_SUPPORTED, VARIANT_OBJECT_XML_SUPPORTED);
    }

    /**
     * Constructor reading the object from a serialized representation. This
     * representation must have the proper media type:
     * "application/x-java-serialized-object".
     * 
     * @param serializedRepresentation
     *            The serialized representation.
     * @param classLoader
     *            The class loader used to read the object.
     * @param variantObjectBinarySupported
     *            Indicates whether the JavaBeans binary deserialization is
     *            supported or not.
     * @param variantObjectXmlSupported
     *            Indicates whether the JavaBeans XML deserialization is
     *            supported or not.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    public ObjectRepresentation(Representation serializedRepresentation,
            final ClassLoader classLoader,
            boolean variantObjectBinarySupported,
            boolean variantObjectXmlSupported) throws IOException,
            ClassNotFoundException, IllegalArgumentException {
        super(MediaType.APPLICATION_JAVA_OBJECT);

        if (MediaType.APPLICATION_JAVA_OBJECT.equals(serializedRepresentation
                .getMediaType())) {
            if (!variantObjectBinarySupported) {
                throw new IllegalArgumentException(
                        "SECURITY WARNING: The usage of ObjectInputStream when "
                                + "deserializing binary representations from unstrusted "
                                + "sources can lead to malicious attacks. As pointed "
                                + "here (https://github.com/restlet/restlet-framework-java/issues/778), "
                                + "the ObjectInputStream class is able to force the JVM to execute unwanted "
                                + "Java code. Thus, the support of such format has been disactivated "
                                + "by default. You can activate this support by turning on the following system property: "
                                + "org.restlet.representation.ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED.");
            }
            setMediaType(MediaType.APPLICATION_JAVA_OBJECT);
            InputStream is = serializedRepresentation.getStream();
            ObjectInputStream ois = null;
            if (classLoader != null) {
                ois = new ObjectInputStream(is) {
                    @Override
                    protected Class<?> resolveClass(
                            java.io.ObjectStreamClass desc)
                            throws java.io.IOException,
                            java.lang.ClassNotFoundException {
                        return Class
                                .forName(desc.getName(), false, classLoader);
                    }
                };
            } else {
                ois = new ObjectInputStream(is);
            }

            this.object = (T) ois.readObject();

            if (is.read() != -1) {
                throw new IOException(
                        "The input stream has not been fully read.");
            }

            ois.close();
            // [ifndef android]
        } else if (MediaType.APPLICATION_JAVA_OBJECT_XML
                .equals(serializedRepresentation.getMediaType())) {
            if (!variantObjectXmlSupported) {
                throw new IllegalArgumentException(
                        "SECURITY WARNING: The usage of XMLDecoder when "
                                + "deserializing XML representations from unstrusted "
                                + "sources can lead to malicious attacks. As pointed "
                                + "here (http://blog.diniscruz.com/2013/08/using-xmldecoder-to-execute-server-side.html), "
                                + "the XMLDecoder class is able to force the JVM to "
                                + "execute unwanted Java code described inside the XML "
                                + "file. Thus, the support of such format has been "
                                + "disactivated by default. You can activate this "
                                + "support by turning on the following system property: "
                                + "org.restlet.representation.ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED.");
            }
            setMediaType(MediaType.APPLICATION_JAVA_OBJECT_XML);
            InputStream is = serializedRepresentation.getStream();
            java.beans.XMLDecoder decoder = new java.beans.XMLDecoder(is);
            this.object = (T) decoder.readObject();

            if (is.read() != -1) {
                decoder.close();
                throw new IOException(
                        "The input stream has not been fully read.");
            }

            decoder.close();
            // [enddef]
        } else {
            throw new IllegalArgumentException(
                    "The serialized representation must have this media type: "
                            + MediaType.APPLICATION_JAVA_OBJECT.toString()
                            + " or this one: "
                            + MediaType.APPLICATION_JAVA_OBJECT_XML.toString());
        }
    }

    /**
     * Constructor for the {@link MediaType#APPLICATION_JAVA_OBJECT} type.
     * 
     * @param object
     *            The serializable object.
     */
    public ObjectRepresentation(T object) {
        super(MediaType.APPLICATION_JAVA_OBJECT);
        this.object = object;
    }

    /**
     * Constructor for either the {@link MediaType#APPLICATION_JAVA_OBJECT} type
     * or the {@link MediaType#APPLICATION_XML} type. In the first case, the
     * Java Object Serialization mechanism is used, based on
     * {@link ObjectOutputStream}. In the latter case, the JavaBeans XML
     * serialization is used, based on {@link java.beans.XMLEncoder}.
     * 
     * @param object
     *            The serializable object.
     * @param mediaType
     *            The media type.
     */
    public ObjectRepresentation(T object, MediaType mediaType) {
        super((mediaType == null) ? MediaType.APPLICATION_JAVA_OBJECT
                : mediaType);
        this.object = object;
    }

    /**
     * Returns the represented object.
     * 
     * @return The represented object.
     * @throws IOException
     */
    public T getObject() throws IOException {
        return this.object;
    }

    /**
     * Releases the represented object.
     */
    @Override
    public void release() {
        setObject(null);
        super.release();
    }

    /**
     * Sets the represented object.
     * 
     * @param object
     *            The represented object.
     */
    public void setObject(T object) {
        this.object = object;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        if (MediaType.APPLICATION_JAVA_OBJECT.isCompatible(getMediaType())) {
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            oos.writeObject(getObject());
            oos.flush();
            // [ifndef android]
        } else if (MediaType.APPLICATION_JAVA_OBJECT_XML
                .isCompatible(getMediaType())) {
            java.beans.XMLEncoder encoder = new java.beans.XMLEncoder(
                    outputStream);
            encoder.writeObject(getObject());
            encoder.close();
            // [enddef]
        }
    }

}
