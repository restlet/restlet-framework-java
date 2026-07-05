/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Date;
import org.junit.jupiter.api.Test;
import org.restlet.util.Series;

/** Test {@link org.restlet.data.Disposition}. */
class DispositionTestCase {

    @Test
    void defaultConstructor_usesNoneType() {
        Disposition disposition = new Disposition();
        assertEquals(Disposition.TYPE_NONE, disposition.getType());
    }

    @Test
    void constructor_withType() {
        Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
        assertEquals(Disposition.TYPE_ATTACHMENT, disposition.getType());
    }

    @Test
    void constructor_withTypeAndParameters() {
        Series<Parameter> parameters = new Form();
        parameters.add(Disposition.NAME_FILENAME, "file.txt");
        Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT, parameters);
        assertEquals("file.txt", disposition.getFilename());
    }

    @Test
    void getParameters_lazilyCreatesEmptySeries() {
        Disposition disposition = new Disposition();
        assertNotNull(disposition.getParameters());
        assertEquals(0, disposition.getParameters().size());
    }

    @Test
    void setType_updatesType() {
        Disposition disposition = new Disposition();
        disposition.setType(Disposition.TYPE_INLINE);
        assertEquals(Disposition.TYPE_INLINE, disposition.getType());
    }

    @Test
    void setFilename_andGetFilename_roundTrip() {
        Disposition disposition = new Disposition();
        disposition.setFilename("report.pdf");
        assertEquals("report.pdf", disposition.getFilename());
    }

    @Test
    void setSize_storesSizeParameter() {
        Disposition disposition = new Disposition();
        disposition.setSize(1024L);
        assertEquals(
                "1024", disposition.getParameters().getFirstValue(Disposition.NAME_SIZE, true));
    }

    @Test
    void setCreationDate_storesFormattedDate() {
        Disposition disposition = new Disposition();
        Date date = new Date(0);
        disposition.setCreationDate(date);
        assertNotNull(
                disposition.getParameters().getFirstValue(Disposition.NAME_CREATION_DATE, true));
    }

    @Test
    void setModificationDate_storesFormattedDate() {
        Disposition disposition = new Disposition();
        disposition.setModificationDate(new Date(0));
        assertNotNull(
                disposition
                        .getParameters()
                        .getFirstValue(Disposition.NAME_MODIFICATION_DATE, true));
    }

    @Test
    void setReadDate_storesFormattedDate() {
        Disposition disposition = new Disposition();
        disposition.setReadDate(new Date(0));
        assertNotNull(disposition.getParameters().getFirstValue(Disposition.NAME_READ_DATE, true));
    }

    @Test
    void addDate_appendsAdditionalDateParameter() {
        Disposition disposition = new Disposition();
        disposition.addDate(Disposition.NAME_CREATION_DATE, new Date(0));
        disposition.addDate(Disposition.NAME_CREATION_DATE, new Date(1000));
        assertEquals(2, disposition.getParameters().subList(Disposition.NAME_CREATION_DATE).size());
    }

    @Test
    void setParameters_replacesParameterSeries() {
        Disposition disposition = new Disposition();
        Series<Parameter> parameters = new Form();
        parameters.add("custom", "value");
        disposition.setParameters(parameters);
        assertSame(parameters, disposition.getParameters());
    }
}
