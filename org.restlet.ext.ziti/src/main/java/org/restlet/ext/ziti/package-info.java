/**
 * Integration with OpenZiti, experimental. This extension routes the calls of the HTTP client
 * connector to the origins an OpenZiti identity can reach over the Ziti overlay network, and the
 * other calls over TCP, see {@link org.restlet.ext.ziti.ZitiClientTransport}. Only the client side
 * is supported.
 *
 * @since Restlet 3.0
 * @see <a href="https://openziti.io/">OpenZiti home</a>
 */
package org.restlet.ext.ziti;
