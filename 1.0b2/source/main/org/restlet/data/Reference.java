/*
 * Copyright 2005-2006 Jérôme LOUVEL
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.data;

import java.io.IOException;

/**
 * Represents a reference to a uniform resource identifier (URI).<br/>
 * Contrary to the java.net.URI class, this interface represents mutable references.
 */
public interface Reference extends Data
{
   /**
    * Returns the authority component for hierarchical identifiers.
    * Includes the user info, host name and the host port number.
    * @return The authority component for hierarchical identifiers.
    */
   public String getAuthority();

   /**
    * Returns the fragment identifier.
    * @return The fragment identifier.
    */
   public String getFragment();

   /**
    * Returns the host identifier.
    * Includes the scheme, the host name and the host port number.
    * @return The host identifier.
    */
   public String getHostIdentifier();

   /**
    * Returns the host name component for server based hierarchical identifiers.
    * @return The host name component for server based hierarchical identifiers.
    */
   public String getHostName();

   /**
    * Returns the optional port number for server based hierarchical identifiers.
    * @return The optional port number for server based hierarchical identifiers.
    */
   public Integer getHostPort();

   /**
    * Returns the absolute resource identifier, without the fragment.
    * @return The absolute resource identifier, without the fragment.
    */
   public String getIdentifier();

   /**
    * Returns the path component for hierarchical identifiers.
    * @return The path component for hierarchical identifiers.
    */
   public String getPath();

   /**
    * Returns the optional query component for hierarchical identifiers.
    * @return The optional query component for hierarchical identifiers.
    */
   public String getQuery();

   /**
    * Returns the optional query component as a form submission.
    * @return The optional query component as a form submission.
    * @throws IOException 
    */
   public Form getQueryAsForm() throws IOException;

   /**
    * Returns the scheme component.
    * @return The scheme component.
    */
   public String getScheme();

   /**
    * Returns the scheme specific part.
    * @return The scheme specific part.
    */
   public String getSchemeSpecificPart();

   /**
    * Returns the user info component for server based hierarchical identifiers.
    * @return The user info component for server based hierarchical identifiers.
    */
   public String getUserInfo();

   /**
    * Indicates if the reference is absolute.
    * @return True if the reference is absolute.
    */
   public boolean isAbsolute();

   /**
    * Indicates if the identifier is hierarchical.
    * @return True if the identifier is hierarchical, false if it is opaque.
    */
   public boolean isHierarchical();

   /**
    * Indicates if the identifier is opaque.
    * @return True if the identifier is opaque, false if it is hierarchical.
    */
   public boolean isOpaque();

   /**
    * Indicates if the reference is relative.
    * @return True if the reference is relative.
    */
   public boolean isRelative();

   /**
    * Sets the authority component for hierarchical identifiers.
    * Includes the user info, host name and the host port number.
    * @param authority The authority component for hierarchical identifiers.
    */
   public void setAuthority(String authority);

   /**
    * Sets the fragment identifier.
    * @param fragment The fragment identifier.
    */
   public void setFragment(String fragment);

   /**
    * Sets the host component for server based hierarchical identifiers.
    * @param host The host component for server based hierarchical identifiers.
    */
   public void setHostName(String host);

   /**
    * Sets the optional port number for server based hierarchical identifiers.
    * @param port The optional port number for server based hierarchical identifiers.
    */
   public void setHostPort(Integer port);

   /**
    * Sets the absolute resource identifier.
    * @param identifier The absolute resource identifier.
    */
   public void setIdentifier(String identifier);

   /**
    * Sets the path component for hierarchical identifiers.
    * @param path The path component for hierarchical identifiers.
    */
   public void setPath(String path);

   /**
    * Returns the query component for hierarchical identifiers. param query The query component for
    * hierarchical identifiers.
    * @param query
    */
   public void setQuery(String query);

   /**
    * Sets the scheme component.
    * @param scheme The scheme component.
    */
   public void setScheme(String scheme);

   /**
    * Sets the scheme specific part.
    * @param schemeSpecificPart The scheme specific part.
    */
   public void setSchemeSpecificPart(String schemeSpecificPart);

   /**
    * Sets the user info component for server based hierarchical identifiers.
    * @param userInfo The user info component for server based hierarchical identifiers.
    */
   public void setUserInfo(String userInfo);

   /**
    * Returns the URI reference string.
    * @return The URI reference string.
    */
   public String toString();

   /**
    * Returns the URI reference string.
    * @param query Indicates if the query should be included;
    * @param fragment Indicates if the fragment should be included;
    * @return The URI reference string.
    */
   public String toString(boolean query, boolean fragment);

}
