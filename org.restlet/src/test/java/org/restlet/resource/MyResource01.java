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
