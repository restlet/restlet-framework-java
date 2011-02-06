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

package org.restlet.ext.javamail;

import java.io.IOException;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;
import org.w3c.dom.DOMException;

/**
 * A JavaMail converter helper to convert from Message objects to
 * MessageRepresentation.
 * 
 * @author Thierry Boileau
 */
public class JavaMailConverter extends ConverterHelper {

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        return null;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        return null;
    }

    @Override
    public <T> float score(Representation source, Class<T> target,
            UniformResource resource) {
        return -1.0f;
    }

    @Override
    public float score(Object source, Variant target, UniformResource resource) {
        if (source instanceof Message) {
            return 1.0f;
        }

        return -1.0f;
    }

    @Override
    public <T> T toObject(Representation source, Class<T> target,
            UniformResource resource) throws IOException {
        return null;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            UniformResource resource) throws IOException {
        if (source instanceof Message) {
            try {
                return new MessageRepresentation((Message) source);
            } catch (DOMException e) {
                throw new IOException(
                        "Cannot convert the source message as a MessageRepresentation due to:"
                                + e.getMessage());
            } catch (MessagingException e) {
                throw new IOException(
                        "Cannot convert the source message as a MessageRepresentation due to:"
                                + e.getMessage());
            }
        }

        return null;
    }

}
