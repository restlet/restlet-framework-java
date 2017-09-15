package org.restlet.ext.apispark.internal.conversion;

import java.net.URL;
import java.util.Arrays;
import java.util.logging.Logger;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.wordnik.swagger.util.Json;

public class ImportUtils {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(ImportUtils.class.getName());

    private static Client client = new Client(Arrays.asList(Protocol.HTTP, Protocol.HTTPS));

    static {
        client.setContext(new Context());
    }

    public static boolean isRemoteUrl(String url) {
        if (url == null) {
            return false;
        }

        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static ClientResource createAuthenticatedClientResource(String url,
            String userName, String password) {
        ClientResource cr = new ClientResource(url);
        cr.setNext(client);
        cr.accept(MediaType.APPLICATION_JSON);
        if (!StringUtils.isNullOrEmpty(userName)
                && !StringUtils.isNullOrEmpty(password)) {
            cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, userName,
                    password);
        }
        return cr;
    }

    /**
     * Downloads the content served at the given URL and de-serializes it in the given Java class.
     * Forces the representation's media type to JSON to avoid de-serialization errors.
     * 
     * @param url
     *            The URL on which the content is served
     * @param userName
     *            The login for basic HTTP authentication.
     * @param password
     *            The password for basic HTTP authentication.
     * @param clazz
     *            The Java class in which to de-serialize.
     * @return The de-serialized content.
     * @throws TranslationException
     */
    public static <T> T getAndDeserialize(String url, String userName, String password,
            Class<T> clazz) throws TranslationException {

        Representation representation;
        ClientResource cr = createAuthenticatedClientResource(url, userName, password);
        representation = cr.get();

        try {

            // Force the media type in case the server returns an invalid one (ex: server specifying text/plain will
            // make de-serialization fail)
            representation.setMediaType(MediaType.APPLICATION_JSON);

            JacksonRepresentation<T> jacksonRepresentation = new JacksonRepresentation<T>(representation, clazz);
            jacksonRepresentation.setObjectMapper(Json.mapper());

            return jacksonRepresentation.getObject();
        } catch (Exception e) {
            throw new TranslationException("file", "Could not deserialize object at " + url +
                    " as " + clazz.getName(), e);
        }
    }

}
