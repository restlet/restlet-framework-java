package org.restlet.ext.apispark.internal.agent.bean;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Manuel Boillod
 */
public class OperationsAuthorization extends ArrayList<OperationAuthorization> {

    private static final long serialVersionUID = 1L;

    public OperationsAuthorization() {
    }

    public OperationsAuthorization(
            Collection<? extends OperationAuthorization> c) {
        super(c);
    }
}
