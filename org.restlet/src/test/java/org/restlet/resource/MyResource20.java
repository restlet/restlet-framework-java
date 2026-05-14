/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

/**
 * Sample annotated interface.
 *
 * @author Jerome Louvel
 */
public interface MyResource20 {

    @Get
    MyBean represent() throws MyException01;

    @Put
    MyBean representAndSerializeException() throws MyException02;
}
