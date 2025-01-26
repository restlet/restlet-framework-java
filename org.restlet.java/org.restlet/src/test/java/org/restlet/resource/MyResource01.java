/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.resource;

/**
 * Sample annotated interface.
 * 
 * @author Jerome Louvel
 */
public interface MyResource01 {

    @Get
    MyBean represent();

    @Put
    String store(MyBean bean);

    @Post
    boolean accept(MyBean bean);

    @Delete("txt")
    String remove();

    @Options("txt")
    String describe();

}
