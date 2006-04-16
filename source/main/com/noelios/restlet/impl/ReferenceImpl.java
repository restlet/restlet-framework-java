/*
 * Copyright 2005-2006 Jerome LOUVEL
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

package com.noelios.restlet.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.Manager;
import org.restlet.data.Form;
import org.restlet.data.Reference;

/**
 * Represents a reference to a uniform resource identifier (URI).<br/>
 * Contrary to the java.net.URI class, this interface represents mutable references.
 */
public class ReferenceImpl implements Reference
{
   /** The fragment separator index. */
   protected int fragmentIndex;

   /** The query separator index. */
   protected int queryIndex;

   /** The scheme separator index. */
   protected int schemeIndex;

   /** The internal reference. */
   protected String uri;

   /**
    * Constructor from a URI reference.
    * @param uriReference The URI reference.
    */
   public ReferenceImpl(String uriReference)
   {
      this.uri = uriReference;
      updateIndexes();
   }

   /**
    * Constructor from a URI reference.
    * @param identifier The absolute URI.
    * @param fragment The fragment identifier.
    */
   public ReferenceImpl(String identifier, String fragment)
   {
      if(fragment == null)
      {
         this.uri = identifier;
      }
      else
      {
         this.uri = identifier + '#' + fragment;
      }

      updateIndexes();
   }
   
   /**
    * Constructor from the URI parts.
    * @param scheme The scheme ("http", "https" or "ftp").
    * @param hostName The host name or IP address.
    * @param hostPort The host port (default ports are correctly ignored).
    * @param path The path component for hierarchical identifiers.
    * @param query The optional query component for hierarchical identifiers.
    * @param fragment The optionale fragment identifier.
    */
   public ReferenceImpl(String scheme, String hostName, int hostPort, String path, String query, String fragment)
   {
   	StringBuilder sb = new StringBuilder();

   	// Append the scheme and host name
   	sb.append(scheme).append("://").append(hostName);

   	// Append the host port number 
   	if((scheme.equals("ftp")   && (hostPort != 21)) ||
   		(scheme.equals("http")  && (hostPort != 80)) || 
   		(scheme.equals("https") && (hostPort != 443)))
   	{
   		sb.append(':').append(hostPort);
   	}

   	// Append the path
   	if(path != null)
   	{
   		sb.append(path);
   	}
   	
   	// Append the query string 
   	if(query != null)
   	{
   		sb.append('?').append(query);
   	}
   	
   	// Append the fragment identifier
   	if(fragment != null)
   	{
   		sb.append('#').append(fragment);
   	}

   	// Actually construct the reference
      this.uri = sb.toString();
      updateIndexes();
   }

   /**
    * Returns the authority component for hierarchical identifiers.
    * Includes the user info, host name and the host port number.
    * @return The authority component for hierarchical identifiers.
    */
   public String getAuthority()
   {
      String ssp = getSchemeSpecificPart();

      if(ssp.startsWith("//"))
      {
         int index = ssp.indexOf('/', 2);

         if(index != -1)
         {
            return ssp.substring(2, index);
         }
         else
         {
            index = ssp.indexOf('?');
            if(index != -1)
            {
               return ssp.substring(2, index);
            }
            else
            {
               return ssp.substring(2);
            }
         }
      }
      else
      {
         return null;
      }
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Resource reference equivalent to a URI";
   }

   /**
    * Returns the fragment identifier.
    * @return The fragment identifier.
    */
   public String getFragment()
   {
      if(fragmentIndex != -1)
      {
         return this.uri.substring(fragmentIndex + 1);
      }
      else
      {
         return null;
      }
   }

   /**
    * Returns the host identifier.
    * Includes the scheme, the host name and the host port number.
    * @return The host identifier.
    */
   public String getHostIdentifier()
   {
      StringBuilder result = new StringBuilder();
      result.append(getScheme()).append("://").append(getAuthority());
      return result.toString();
   }

   /**
    * Returns the host name component for server based hierarchical identifiers.
    * @return The host name component for server based hierarchical identifiers.
    */
   public String getHostName()
   {
      String authority = getAuthority();
      int index1 = authority.indexOf('@');
      int index2 = authority.indexOf(':');

      if(index1 != -1)
      {
         // User info found
         if(index2 != -1)
         {
            // Port found
            return authority.substring(index1 + 1, index2);
         }
         else
         {
            // No port found
            return authority.substring(index1 + 1);
         }
      }
      else
      {
         // No user info found
         if(index2 != -1)
         {
            // Port found
            return authority.substring(2, index2);
         }
         else
         {
            // No port found
            return authority.substring(2);
         }
      }
   }

   /**
    * Returns the optional port number for server based hierarchical identifiers.
    * @return The optional port number for server based hierarchical identifiers.
    */
   public Integer getHostPort()
   {
      String authority = getAuthority();
      int index = authority.indexOf(':');

      if(index != -1)
      {
         return Integer.valueOf(authority.substring(index + 1));
      }
      else
      {
         return null;
      }
   }

   /**
    * Returns the absolute resource identifier, without the fragment.
    * @return The absolute resource identifier, without the fragment.
    */
   public String getIdentifier()
   {
      if(fragmentIndex != -1)
      {
         // Fragment found
         return this.uri.substring(0, fragmentIndex);
      }
      else
      {
         // No fragment found
         return this.uri;
      }
   }

   /**
    * Returns the metadata name like "text/html" or "compress" or "iso-8851-1".
    * @return The metadata name like "text/html" or "compress" or "iso-8851-1".
    */
   public String getName()
   {
      return "uri-reference";
   }

   /**
    * Returns the path component for hierarchical identifiers.
    * @return The path component for hierarchical identifiers.
    */
   public String getPath()
   {
      String ssp = getSchemeSpecificPart();

      if(ssp.startsWith("//"))
      {
         // Authority found
         int index1 = ssp.indexOf('/', 2);

         if(index1 != -1)
         {
            // Path found
            int index2 = ssp.indexOf('?');
            if(index2 != -1)
            {
               // Query found
               return ssp.substring(index1, index2);
            }
            else
            {
               // No query found
               return ssp.substring(index1);
            }
         }
         else
         {
            // No path found
            return null;
         }
      }
      else
      {
         // No authority found
         int index = ssp.indexOf('?');
         if(index != -1)
         {
            // Query found
            return ssp.substring(0, index);
         }
         else
         {
            // No query found
            return ssp;
         }
      }
   }

   /**
    * Returns the current reference relatively to a base reference.
    * @param base The base reference to use.
    * @return The current reference relatively to a base reference.
    */
   public Reference getRelativeRef(Reference base)
   {
      Reference result = null;
    
      if(base == null)
      {
         result = this;
      }
      else if(!isAbsolute() || !isHierarchical())
      {
         throw new IllegalArgumentException("The reference must have an absolute hierarchical path component");
      }
      else if(!base.isAbsolute() || !base.isHierarchical())
      {
         throw new IllegalArgumentException("The base reference must have an absolute hierarchical path component");
      }
      else if(!getHostIdentifier().equals(base.getHostIdentifier()))
      {
         result = this;
      }
      else
      {
         String localPath = getPath();
         String basePath = base.getPath();
         String relativePath = null;
         
         if((basePath == null) || (localPath == null))
         {
            relativePath = localPath;
         }
         else
         {
            // Find the junction point
            boolean diffFound = false;
            int lastSlashIndex = -1;
            int i = 0;
            char current;
            while(!diffFound && (i < localPath.length()) && (i < basePath.length()))
            {
               current = localPath.charAt(i);
               
               if(current != basePath.charAt(i))
               {
                  diffFound = true;
               }
               else
               {
                  if(current == '/') lastSlashIndex = i;
                  i++;
               }
            }
            
            if(!diffFound)
            {
               if(localPath.length() == basePath.length())
               {
                  // Both paths are strictely equivalent
                  relativePath = ".";
               }
               else if(i == localPath.length())
               {
                  if(basePath.charAt(i) == '/')
                  {
                     if((i + 1) == basePath.length())
                     {
                        // Both paths are strictely equivalent
                        relativePath = ".";
                     }
                     else
                     {
                        // The local path is a direct parent of the base path
                        // We need to add enough ".." in the relative path
                        StringBuilder sb = new StringBuilder();
                        sb.append("..");
                        boolean canAdd = false;
                        
                        for(int j = i + 1; j < basePath.length(); j++)
                        {
                           if(basePath.charAt(j) == '/')
                           {
                              canAdd = true;
                           }
                           else if(canAdd)
                           {
                              sb.append("/..");
                              canAdd = false;
                           }
                        }
   
                        relativePath = sb.toString();
                     }
                  }
                  else
                  {
                     // The base path has a segment that starts like the last local path segment 
                     // But that is longer. Situation similar to a junction
                     StringBuilder sb = new StringBuilder();
                     sb.append("..");
                     boolean canAdd = false;
                     
                     for(int j = i; j < basePath.length(); j++)
                     {
                        if(basePath.charAt(j) == '/')
                        {
                           canAdd = true;
                        }
                        else if(canAdd)
                        {
                           sb.append("/..");
                           canAdd = false;
                        }
                     }
                     
                     sb.append('/').append(localPath.substring(lastSlashIndex + 1));
                     relativePath = sb.toString();
                  }               
               }
               else if(i == basePath.length())
               {
                  if(localPath.charAt(i) == '/')
                  {
                     if((i + 1) == localPath.length())
                     {
                        // Both paths are strictely equivalent
                        relativePath = ".";
                     }
                     else
                     {
                        // The local path is a direct child of the base path
                        relativePath = localPath.substring(i + 1);
                     }
                  }
                  else
                  {
                     if(lastSlashIndex == (i -1))
                     {
                        // The local path is a direct subpath of the base path
                        relativePath = localPath.substring(i);
                     }
                     else
                     {
                        relativePath = ".." + localPath.substring(lastSlashIndex);
                     }
                  }
               }            
            }
            else
            {
               // We found a junction point,
               // we need to add enough ".." in the relative path
               // and append the rest of the local path
               // the local path is a direct subpath of the base path
               StringBuilder sb = new StringBuilder();
               sb.append("..");
               boolean canAdd = false;
               
               for(int j = i; j < basePath.length(); j++)
               {
                  if(basePath.charAt(j) == '/')
                  {
                     canAdd = true;
                  }
                  else if(canAdd)
                  {
                     sb.append("/..");
                     canAdd = false;
                  }
               }
               
               sb.append('/').append(localPath.substring(lastSlashIndex + 1));
               relativePath = sb.toString();
            }
         }
         
         result = Manager.createReference(relativePath);
      }
      
      return result;
   }

   /**
    * Returns the optional query component for hierarchical identifiers.
    * @return The optional query component for hierarchical identifiers.
    */
   public String getQuery()
   {
      if(queryIndex != -1)
      {
         // Query found
         if(fragmentIndex != -1)
         {
            // Fragment found
            return this.uri.substring(queryIndex + 1, fragmentIndex);
         }
         else
         {
            // No fragment found
            return this.uri.substring(queryIndex + 1);
         }
      }
      else
      {
         // No query found
         return null;
      }
   }

   /**
    * Returns the optional query component as a form submission.
    * @return The optional query component as a form submission.
    * @throws IOException 
    */
   public Form getQueryAsForm() throws IOException
   {
      return new FormImpl(getQuery());
   }

   /**
    * Returns the scheme component.
    * @return The scheme component.
    */
   public String getScheme()
   {
      if(schemeIndex != -1)
      {
         // Scheme found
         return this.uri.substring(0, schemeIndex);
      }
      else
      {
         // No scheme found
         return null;
      }
   }

   /**
    * Returns the scheme specific part.
    * @return The scheme specific part.
    */
   public String getSchemeSpecificPart()
   {
      if(schemeIndex != -1)
      {
         // Scheme found
         if(fragmentIndex != -1)
         {
            // Fragment found
            return this.uri.substring(schemeIndex + 1, fragmentIndex);
         }
         else
         {
            // No fragment found
            return this.uri.substring(schemeIndex + 1);
         }
      }
      else
      {
         // No scheme found
         if(fragmentIndex != -1)
         {
            // Fragment found
            return this.uri.substring(0, fragmentIndex);
         }
         else
         {
            // No fragment found
            return this.uri;
         }
      }
   }

   /**
    * Returns the last segment of a hierarchical path.<br/>
    * For example the "/a/b/c" and "/a/b/c/" paths have the same segments: "a", "b", "c.
    * @return The last segment of a hierarchical path.
    */
   public String getLastSegment()
   {
      String result = null;
      int lastSlash = getPath().lastIndexOf('/');
      
      if(lastSlash != -1)
      {
         result = getPath().substring(lastSlash + 1);
      }
      
      return result;
   }

   /**
    * Returns the segments of a hierarchical path.<br/>
    * A new list is created for each call.
    * @return The segments of a hierarchical path.
    */
   public List<String> getSegments()
   {
      List<String> result = new ArrayList<String>();;
      String path = getPath();
      int start = -2; // The index of the slash starting the segment  
      char current;
      
      if(path != null)
      {
         for(int i = 0; i < path.length(); i++)
         {
            current = path.charAt(i);
            
            if(current == '/')
            {
               if(start == -2)
               {
                  // Beginning of an absolute path or sequence of two separators
                  start = i;
               }
               else
               {
                  // End of a segment
                  result.add(path.substring(start + 1, i));
                  start = i;
               }
            }
            else
            {
               if(start == -2)
               {
                  // Starting a new segment for a relative path
                  start = -1;
               }
               else
               {
                  // Looking for the next character
               }
            }
         }
         
         if(start != -2)
         {
            // Add the last segment
            result.add(path.substring(start + 1));
         }
      }
      
      return result;
   }

   /**
    * Returns the user info component for server based hierarchical identifiers.
    * @return The user info component for server based hierarchical identifiers.
    */
   public String getUserInfo()
   {
      String authority = getAuthority();
      int index = authority.indexOf('@');

      if(index != -1)
      {
         return authority.substring(0, index);
      }
      else
      {
         return null;
      }
   }

   /**
    * Indicates if the reference is absolute.
    * @return True if the reference is absolute.
    */
   public boolean isAbsolute()
   {
      return (getScheme() != null);
   }

   /**
    * Indicates if the identifier is hierarchical.
    * @return True if the identifier is hierarchical, false if it is opaque.
    */
   public boolean isHierarchical()
   {
      return isRelative() || (getSchemeSpecificPart().charAt(0) == '/');
   }

   /**
    * Indicates if the identifier is opaque.
    * @return True if the identifier is opaque, false if it is hierarchical.
    */
   public boolean isOpaque()
   {
      return isAbsolute() && (getSchemeSpecificPart().charAt(0) != '/');
   }

   /**
    * Indicates if the reference is relative.
    * @return True if the reference is relative.
    */
   public boolean isRelative()
   {
      return (getScheme() == null);
   }

   /**
    * Sets the authority component for hierarchical identifiers.
    * @param authority The authority component for hierarchical identifiers.
    */
   public void setAuthority(String authority)
   {
      String ssp = getSchemeSpecificPart();
      String newAuthority = (authority == null) ? "" : "//" + authority;

      if(ssp.startsWith("//"))
      {
         int index = ssp.indexOf('/', 2);

         if(index != -1)
         {
            setSchemeSpecificPart(newAuthority + ssp.substring(index));
         }
         else
         {
            index = ssp.indexOf('?');
            if(index != -1)
            {
               setSchemeSpecificPart(newAuthority + ssp.substring(index));
            }
            else
            {
               setSchemeSpecificPart(newAuthority);
            }
         }
      }
      else
      {
         setSchemeSpecificPart(newAuthority + ssp);
      }
   }

   /**
    * Sets the fragment identifier.
    * @param fragment The fragment identifier.
    */
   public void setFragment(String fragment)
   {
      if(fragment.indexOf('#') == -1)
      {
         throw new IllegalArgumentException("Illegal '#' character detected in parameter");
      }
      else
      {
         if(fragmentIndex != -1)
         {
            // Existing fragment
            if(fragment != null)
            {
               this.uri = this.uri.substring(0, fragmentIndex + 1) + fragment;
            }
            else
            {
               this.uri = this.uri.substring(0, fragmentIndex);
            }
         }
         else
         {
            // No existing fragment
            if(fragment != null)
            {
               this.uri = this.uri + '#' + fragment;
            }
            else
            {
               // Do nothing
            }
         }
      }

      updateIndexes();
   }

   /**
    * Sets the host component for server based hierarchical identifiers.
    * @param host The host component for server based hierarchical identifiers.
    */
   public void setHostName(String host)
   {
      String authority = getAuthority();
      int index1 = authority.indexOf('@');
      int index2 = authority.indexOf(':');

      if(index1 != -1)
      {
         // User info found
         if(index2 != -1)
         {
            // Port found
            setAuthority(authority.substring(0, index1 + 1) + host + authority.substring(index2));
         }
         else
         {
            // No port found
            setAuthority(authority.substring(0, index1 + 1) + host);
         }
      }
      else
      {
         // No user info found
         if(index1 != -1)
         {
            // Port found
            setAuthority(host + authority.substring(index2));
         }
         else
         {
            // No port found
            setAuthority(host);
         }
      }
   }

   /**
    * Sets the optional port number for server based hierarchical identifiers.
    * @param port The optional port number for server based hierarchical identifiers.
    */
   public void setHostPort(Integer port)
   {
      String authority = getAuthority();
      String newPort = (port == null) ? "" : ":" + port;
      int index = authority.indexOf(':');

      if(index != -1)
      {
         setAuthority(authority.substring(0, index) + newPort);
      }
      else
      {
         setAuthority(authority + newPort);
      }
   }

   /**
    * Sets the absolute resource identifier.
    * @param identifier The absolute resource identifier.
    */
   public void setIdentifier(String identifier)
   {
      if(identifier.indexOf('#') == -1)
      {
         throw new IllegalArgumentException("Illegal '#' character detected in parameter");
      }
      else
      {
         if(fragmentIndex != -1)
         {
            // Fragment found
            this.uri = identifier + this.uri.substring(fragmentIndex);
         }
         else
         {
            // No fragment found
            this.uri = identifier;
         }

         updateIndexes();
      }
   }

   /**
    * Sets the path component for hierarchical identifiers.
    * @param path The path component for hierarchical identifiers.
    */
   public void setPath(String path)
   {
      String ssp = getSchemeSpecificPart();

      if(ssp.startsWith("//"))
      {
         // Authority found
         int index1 = ssp.indexOf('/', 2);

         if(index1 != -1)
         {
            // Path found
            int index2 = ssp.indexOf('?');
            if(index2 != -1)
            {
               // Query found
               setSchemeSpecificPart(ssp.substring(0, index1) + path + ssp.substring(index2));
            }
            else
            {
               // No query found
               setSchemeSpecificPart(ssp.substring(0, index1) + path);
            }
         }
         else
         {
            // No path found
            int index2 = ssp.indexOf('?');
            if(index2 != -1)
            {
               // Query found
               setSchemeSpecificPart(ssp.substring(0, index2) + path + ssp.substring(index2));
            }
            else
            {
               // No query found
               setSchemeSpecificPart(ssp + path);
            }
         }
      }
      else
      {
         // No authority found
         int index = ssp.indexOf('?');
         if(index != -1)
         {
            // Query found
            setSchemeSpecificPart(path + ssp.substring(index));
         }
         else
         {
            // No query found
            setSchemeSpecificPart(path);
         }
      }
   }

   /**
    * Returns the query component for hierarchical identifiers.
    * @param query The query component for hierarchical identifiers.
    */
   public void setQuery(String query)
   {
      if(queryIndex != -1)
      {
         // Query found
         if(fragmentIndex != -1)
         {
            // Fragment found
            if(query != null)
            {
               this.uri = this.uri.substring(0, queryIndex + 1) + query + this.uri.substring(fragmentIndex);
            }
            else
            {
               this.uri = this.uri.substring(0, queryIndex) + this.uri.substring(fragmentIndex);
            }
         }
         else
         {
            // No fragment found
            if(query != null)
            {
               this.uri = this.uri.substring(0, queryIndex + 1) + query;
            }
            else
            {
               this.uri = this.uri.substring(0, queryIndex);
            }
         }
      }
      else
      {
         // No query found
         if(fragmentIndex != -1)
         {
            // Fragment found
            if(query != null)
            {
               this.uri = this.uri.substring(0, fragmentIndex) + '?' + query
                     + this.uri.substring(fragmentIndex);
            }
            else
            {
               // Do nothing;
            }
         }
         else
         {
            // No fragment found
            if(query != null)
            {
               this.uri = this.uri + '?' + query;
            }
            else
            {
               // Do nothing;
            }
         }
      }

      updateIndexes();
   }

   /**
    * Sets the scheme component.
    * @param scheme The scheme component.
    */
   public void setScheme(String scheme)
   {
      if(schemeIndex != -1)
      {
         // Scheme found
         this.uri = scheme + this.uri.substring(schemeIndex);
      }
      else
      {
         // No scheme found
         if(this.uri == null)
         {
            this.uri = scheme + ':';
         }
         else
         {
            this.uri = scheme + ':' + this.uri;
         }
      }

      updateIndexes();
   }

   /**
    * Sets the scheme specific part.
    * @param schemeSpecificPart The scheme specific part.
    */
   public void setSchemeSpecificPart(String schemeSpecificPart)
   {
      if(schemeIndex != -1)
      {
         // Scheme found
         if(fragmentIndex != -1)
         {
            // Fragment found
            this.uri = this.uri.substring(0, schemeIndex + 1) + schemeSpecificPart
                  + this.uri.substring(fragmentIndex);
         }
         else
         {
            // No fragment found
            this.uri = this.uri.substring(0, schemeIndex + 1) + schemeSpecificPart;
         }
      }
      else
      {
         // No scheme found
         if(fragmentIndex != -1)
         {
            // Fragment found
            this.uri = schemeSpecificPart + this.uri.substring(fragmentIndex);
         }
         else
         {
            // No fragment found
            this.uri = schemeSpecificPart;
         }
      }

      updateIndexes();
   }

   /**
    * Sets the segments of a hierarchical path.<br/>
    * A new absolute path will replace any existing one.
    * @param segments The segments of the hierarchical path.
    */
   public void setSegments(List<String> segments)
   {
      StringBuilder sb = new StringBuilder();
      for(String segment : segments)
      {
         sb.append('/').append(segment);
      }
      setPath(sb.toString());
   }

   /**
    * Sets the user info component for server based hierarchical identifiers.
    * @param userInfo The user info component for server based hierarchical identifiers.
    */
   public void setUserInfo(String userInfo)
   {
      String authority = getAuthority();
      String newUserInfo = (userInfo == null) ? "" : userInfo + '@';
      int index = authority.indexOf('@');

      if(index != -1)
      {
         setAuthority(newUserInfo + authority.substring(index + 1));
      }
      else
      {
         setAuthority(newUserInfo + authority);
      }
   }

   /**
    * Returns the URI reference string.
    * @return The URI reference string.
    */
   public String toString()
   {
      return this.uri;
   }

   /**
    * Returns the URI reference string.
    * @param query Indicates if the query should be included;
    * @param fragment Indicates if the fragment should be included;
    * @return The URI reference string.
    */
   public String toString(boolean query, boolean fragment)
   {
      if(query)
      {
         if(fragment)
         {
            return this.uri;
         }
         else
         {
            if(fragmentIndex != -1)
            {
               return this.uri.substring(0, fragmentIndex);
            }
            else
            {
               return this.uri;
            }
         }
      }
      else
      {
         if(fragment)
         {
            if(queryIndex != -1)
            {
               if(fragmentIndex != -1)
               {
                  return this.uri.substring(0, queryIndex) + "#" + getFragment();
               }
               else
               {
                  return this.uri.substring(0, queryIndex);
               }
            }
            else
            {
               return this.uri;
            }
         }
         else
         {
            if(queryIndex != -1)
            {
               return this.uri.substring(0, queryIndex);
            }
            else
            {
               return this.uri;
            }
         }
      }
   }

   /**
    * Update internal indexes.
    */
   private void updateIndexes()
   {
      if(uri != null)
      {
         schemeIndex = this.uri.indexOf(':');
         fragmentIndex = this.uri.indexOf('#');
         queryIndex = this.uri.indexOf('?');
      }
   }
   
}
