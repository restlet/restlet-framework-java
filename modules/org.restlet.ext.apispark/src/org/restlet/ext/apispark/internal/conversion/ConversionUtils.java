package org.restlet.ext.apispark.internal.conversion;

import org.restlet.engine.util.StringUtils;

public class ConversionUtils {

    /**
     * Generates a name for a resource computed from its path. The name is
     * composed of all alphanumeric characters in camel case.<br/>
     * Ex: /contacts/{contactId} => ContactsContactId
     * 
     * @param uri
     *            The URI of the Resource
     * @return The Resource's name computed from the path.
     */
    public static String processResourceName(String uri) {
        String processedUri = "";
        String[] split = uri.replaceAll("\\{", "").replaceAll("\\}", "")
                .split("/");
        for (String str : split) {
            processedUri += StringUtils.firstUpper(str);
        }
        return processedUri;
    }

}
