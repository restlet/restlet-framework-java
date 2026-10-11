/**
 * Copyright 2005-2026 Qlik
 *
 * <p>The content of this file is subject to the terms of the Apache 2.0 open source license
 * available at https://www.opensource.org/licenses/apache-2.0
 *
 * <p>Restlet is a registered trademark of QlikTech International AB.
 */
@SuppressWarnings({"module", "requires-automatic", "requires-transitive-automatic"})
module org.restlet.ext.crypto {
    requires transitive org.restlet;

    exports org.restlet.ext.crypto;
    // The internal package is part of the public signatures (e.g. AwsAuthenticator)
    exports org.restlet.ext.crypto.internal;

    provides org.restlet.engine.security.AuthenticatorHelper with
            org.restlet.ext.crypto.internal.HttpAwsS3Helper,
            org.restlet.ext.crypto.internal.HttpAwsQueryHelper,
            org.restlet.ext.crypto.internal.HttpDigestHelper,
            org.restlet.ext.crypto.internal.HttpAzureSharedKeyHelper,
            org.restlet.ext.crypto.internal.HttpAzureSharedKeyLiteHelper;
}
