/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.service.LogService;
import org.restlet.service.RangeService;
import org.restlet.service.Service;

class ServiceListTestCase {

    @Test
    void add_setsServiceContext() {
        Context context = new Context();
        ServiceList list = new ServiceList(context);
        RangeService service = new RangeService();

        list.add(service);

        assertEquals(context, service.getContext());
    }

    @Test
    void addAtIndex_setsServiceContext() {
        Context context = new Context();
        ServiceList list = new ServiceList(context);
        RangeService service = new RangeService();

        list.add(0, service);

        assertEquals(context, service.getContext());
    }

    @Test
    void addAll_setsContextOnEachService() {
        Context context = new Context();
        ServiceList list = new ServiceList(context);
        RangeService rangeService = new RangeService();
        LogService logService = new LogService();

        list.addAll(Arrays.asList(rangeService, logService));

        assertEquals(context, rangeService.getContext());
        assertEquals(context, logService.getContext());
    }

    @Test
    void addAllAtIndex_setsContextOnEachService() {
        Context context = new Context();
        ServiceList list = new ServiceList(context);
        RangeService rangeService = new RangeService();

        list.addAll(0, Arrays.asList(rangeService));

        assertEquals(context, rangeService.getContext());
    }

    @Test
    void getByClass_matchingService_returnsIt() {
        ServiceList list = new ServiceList(new Context());
        RangeService rangeService = new RangeService();
        list.add(rangeService);

        assertEquals(rangeService, list.get(RangeService.class));
    }

    @Test
    void getByClass_noMatch_returnsNull() {
        ServiceList list = new ServiceList(new Context());
        assertNull(list.get(RangeService.class));
    }

    @Test
    void getContext_returnsConstructorContext() {
        Context context = new Context();
        ServiceList list = new ServiceList(context);
        assertEquals(context, list.getContext());
    }

    @Test
    void setList_replacesAllServices() {
        ServiceList list = new ServiceList(new Context());
        list.add(new RangeService());

        list.set(List.of(new LogService()));

        assertEquals(1, list.size());
        assertTrue(list.get(0) instanceof LogService);
    }

    @Test
    void setList_nullList_clearsServices() {
        ServiceList list = new ServiceList(new Context());
        list.add(new RangeService());

        list.set((List<Service>) null);

        assertTrue(list.isEmpty());
    }

    @Test
    void setService_replacesExistingServiceOfSameType() {
        ServiceList list = new ServiceList(new Context());
        RangeService original = new RangeService();
        list.add(original);

        RangeService replacement = new RangeService();
        list.set(replacement);

        assertEquals(1, list.size());
        assertEquals(replacement, list.get(0));
    }

    @Test
    void setService_noExistingServiceOfType_appendsIt() {
        ServiceList list = new ServiceList(new Context());
        list.add(new LogService());

        RangeService rangeService = new RangeService();
        list.set(rangeService);

        assertEquals(2, list.size());
        assertTrue(list.get(RangeService.class) != null);
    }

    @Test
    void setContext_updatesContextOfRegisteredServices() {
        ServiceList list = new ServiceList(new Context());
        RangeService service = new RangeService();
        list.add(service);

        Context newContext = new Context();
        list.setContext(newContext);

        assertEquals(newContext, list.getContext());
        assertEquals(newContext, service.getContext());
    }

    @Test
    void startAndStop_delegateToEachService() throws Exception {
        ServiceList list = new ServiceList(new Context());
        RangeService service = new RangeService();
        list.add(service);

        list.start();
        assertTrue(service.isStarted());

        list.stop();
        assertTrue(service.isStopped());
    }
}
