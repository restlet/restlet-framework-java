/*
 * Copyright 2005-2006 Noelios Consulting.
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
import java.util.ArrayList;
import java.util.List;

/**
 * Reference to a Uniform Resource Identifier (URI). Contrary to the java.net.URI 
 * class, this interface represents mutable references. It strictly conforms to the RFC 3986
 * specifying URIs and follow its naming conventions.<br/>
 * <pre>
 * 	URI reference        = absolute-reference | relative-reference
 * 
 * 	absolute-reference   = scheme ":" scheme-specific-part [ "#" fragment ]
 * 	scheme-specific-part = hierarchical-part [ "?" query ] | opaque-part
 * 	hierarchical-part    = "//" authority path-abempty | path-absolute | path-rootless | path-empty
 * 	authority            = [ user-info "@" ] host-name [ ":" host-port ]
 * 
 * 	relative-reference   = relative-part [ "?" query ] [ "#" fragment ]
 * </pre><br/>
 * Note that this class doesn't encode or decode the reserved characters. It assumes that the URI passed in are
 * properly encoded using the "%??" sequences. Use the JDK's URLEncoder and URLDecoder classes for this purpose.  
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 * @see <a href="http://www.faqs.org/rfcs/rfc3986.html">RFC 3986</a>
 * @see java.net.URLDecoder
 * @see java.net.URLEncoder
 */
public class Reference implements Data
{
   /** The base reference for relative references. */
   protected Reference baseRef;

   /** The internal reference. */
   protected String uri;

   /** The fragment separator index. */
   protected int fragmentIndex;

   /** The query separator index. */
   protected int queryIndex;

   /** The scheme separator index. */
   protected int schemeIndex;
   
   /**
    * Empty constructor.
    */
   public Reference()
   {
   	this.baseRef = null;
   	this.uri = null;
   	updateIndexes();
   }
   
   /**
    * Clone constructor.
    * @param ref The reference to clone.
    */
   public Reference(Reference ref)
   {
      this(ref.baseRef, ref.uri);
   }
   
   /**
    * Constructor from an absolute URI.
    * @param absoluteUri The absolute URI.
    */
   public Reference(String absoluteUri)
   {
      this((Reference)null, absoluteUri);
   }

   /**
    * Constructor from an identifier and a fragment.
    * @param identifier The resource identifier.
    * @param fragment The fragment identifier.
    */
   public Reference(String identifier, String fragment)
   {
      this((fragment == null) ? identifier : identifier + '#' + fragment);
   }
   
   /**
    * Constructor of absolute reference from its parts.
    * @param scheme The scheme ("http", "https" or "ftp").
    * @param hostName The host name or IP address.
    * @param hostPort The host port (default ports are correctly ignored).
    * @param path The path component for hierarchical identifiers.
    * @param query The optional query component for hierarchical identifiers.
    * @param fragment The optionale fragment identifier.
    */
   public Reference(String scheme, String hostName, int hostPort, String path, String query, String fragment)
   {
      this(toUri(scheme, hostName, hostPort, path, query, fragment));
   }

   /**
    * Constructor from a relative URI.
    * @param baseRef The base reference. 
    * @param relativeUri The relative URI.
    */
   public Reference(Reference baseRef, String relativeUri)
   {
      this.baseRef = baseRef;
      this.uri = relativeUri;
      updateIndexes();
   }
   
   /**
    * Constructor of relative reference from its parts.
    * @param baseRef The base reference. 
    * @param relativePart The relative part component (most of the time it is the path component).
    * @param query The optional query component for hierarchical identifiers.
    * @param fragment The optionale fragment identifier.
    */
   public Reference(Reference baseRef, String relativePart, String query, String fragment)
   {
   	this(baseRef, toUri(relativePart, query, fragment));
   }

   /**
    * Returns the authority component for hierarchical identifiers.
    * Includes the user info, host name and the host port number.
    * @return The authority component for hierarchical identifiers.
    */
   public String getAuthority()
   {
      String part = isRelative() ? getRelativePart() : getSchemeSpecificPart();

      if(part.startsWith("//"))
      {
         int index = part.indexOf('/', 2);

         if(index != -1)
         {
            return part.substring(2, index);
         }
         else
         {
            index = part.indexOf('?');
            if(index != -1)
            {
               return part.substring(2, index);
            }
            else
            {
               return part.substring(2);
            }
         }
      }
      else
      {
         return null;
      }
   }

   /**
    * Returns the base reference for relative references.
    * @return The base reference for relative references.
    */
   public Reference getBaseRef()
   {
   	return this.baseRef;
   }

   /**
    * Sets the base reference for relative references.
    * @param baseRef The base reference for relative references.
    */
   public void setBaseRef(Reference baseRef)
   {
   	this.baseRef = baseRef;
   }

   /**
    * Returns true if both reference are strictly equals, character-to-character.
    * @param ref The reference to compare.
    * @return True if both reference are strictly equals, character-to-character.
    */
   public boolean equals(Reference ref)
   {
   	return (ref == null) ? false : this.uri.equals(ref.uri);
   }
   
   /**
    * Indicates whether some other object is "equal to" this one.
    * @param obj The reference object with which to compare.
    * @return True if this object is the same as the obj argument. 
    */
   public boolean equals(Object obj) 
   {
   	return (obj instanceof Reference) ? equals((Reference) obj) : false;
   }
      
   /**
    * Returns a hash code value for the object.
    * @return A hash code value for the object.
    */
   public int hashCode() 
   {
   	return this.uri.hashCode();
	}
   	
   /**
    * Returns true if both reference are equivalent, meaning that they resolve to the same target reference.
    * @param ref The reference to compare.
    * @return True if both reference are equivalent.
    */
   public boolean isEquivalentTo(Reference ref)
   {
   	return getTargetRef().equals(ref.getTargetRef());
   }
   
   /**
    * Normalizes the reference. Useful before comparison between references or when building 
    * a target reference from a base and a relative references. 
    */
   public void normalize()
   {
      // 1. The input buffer is initialized with the now-appended path components 
   	//    and the output buffer is initialized to the empty string.
   	StringBuilder output = new StringBuilder();
   	StringBuilder input = new StringBuilder();
   	String path = getPath();
   	if(path != null) input.append(path);
   	
   	// 2. While the input buffer is not empty, loop as follows:
   	while(input.length() > 0)
   	{
   		// A. If the input buffer begins with a prefix of "../" or "./",
         //    then remove that prefix from the input buffer; otherwise,
   		if((input.length() >= 3) && input.substring(0, 3).equals("../"))
   		{
   			input.delete(0, 3);
   		}
   		else if((input.length() >= 2) && input.substring(0, 2).equals("./"))
   		{
   			input.delete(0, 2);
   		}
   		
   		// B. if the input buffer begins with a prefix of "/./" or "/.",
         //    where "." is a complete path segment, then replace that
         //    prefix with "/" in the input buffer; otherwise,
   		else if((input.length() >= 3) && input.substring(0, 3).equals("/./"))
   		{
   			input.delete(0, 2);
   		}
   		else if((input.length() == 2) && input.substring(0, 2).equals("/."))
   		{
   			input.delete(1, 2);
   		}
   		
         // C. if the input buffer begins with a prefix of "/../" or "/..",
         // where ".." is a complete path segment, then replace that
         // prefix with "/" in the input buffer and remove the last
         // segment and its preceding "/" (if any) from the output
         // buffer; otherwise,
   		else if((input.length() >= 4) && input.substring(0, 4).equals("/../"))
   		{
   			input.delete(0, 3);
   			removeLastSegment(output);
   		}
   		else if((input.length() == 3) && input.substring(0, 3).equals("/.."))
   		{
   			input.delete(1, 3);
   			removeLastSegment(output);
   		}
   		
         // D.  if the input buffer consists only of "." or "..", then remove
         // that from the input buffer; otherwise,
   		else if((input.length() == 1) && input.substring(0, 1).equals("."))
   		{
   			input.delete(0, 1);
   		}
   		else if((input.length() == 2) && input.substring(0, 2).equals(".."))
   		{
   			input.delete(0, 2);
   		}
   		
         // E.  move the first path segment in the input buffer to the end of
         // the output buffer, including the initial "/" character (if
         // any) and any subsequent characters up to, but not including,
         // the next "/" character or the end of the input buffer.   		
   		else
   		{
   			int max = -1;
   			for(int i = 1; (max == -1) && (i < input.length()); i++)
   			{
   				if(input.charAt(i) == '/') max = i;
   			}
   			
   			if(max != -1)
   			{
   				// We found the next "/" character.
   				output.append(input.substring(0, max));
   				input.delete(0, max);
   			}
   			else
   			{
   				// End of input buffer reached
   				output.append(input);
   				input.delete(0, input.length());
   			}
   		}
   	}
		
		// Finally, the output buffer is returned as the result
		setPath(output.toString());

		// Ensure that the scheme and host names are reset in lower case
		setScheme(getScheme());
		setHostName(getHostName());
   }

   /**
    * Removes the last segement from the output builder.
    * @param output The output builder to update.
    */
   private void removeLastSegment(StringBuilder output)
   {
		int min = -1;
		for(int i = output.length() - 1; (min == -1) && (i >= 0); i--)
		{
			if(output.charAt(i) == '/') min = i;
		}
		
		if(min != -1)
		{
			// We found the previous "/" character.
			output.delete(min, output.length());
		}
		else
		{
			// End of output buffer reached
			output.delete(0, output.length());
		}
   	
   }
   
   /**
    * Returns the target reference. This method resolves relative references against the base reference
    * then normalize them.
    * @return The target reference. 
    */
   public Reference getTargetRef()
   {
   	Reference result = null;
   	String scheme = getScheme();
   	
   	// Step 1 - Resolve relative reference against their base reference
   	if(scheme != null)
   	{
   		// Absolute URI detected
   		result = new Reference(this);
   	}
   	else
   	{
      	String authority = getAuthority();
      	String path = getPath();
      	String query = getQuery();
      	String fragment = getFragment();

      	// Create an empty reference
   		result = new Reference();
      	result.setScheme(baseRef.getScheme());
   		
      	if(authority != null)
      	{
      		result.setAuthority(authority);
      		result.setPath(path);
      		result.setQuery(query);
      	}
      	else
   		{
      		result.setAuthority(baseRef.getAuthority());

      		if((path == null) || (path.equals("")))
      		{
      			result.setPath(baseRef.getPath());
      			
               if(query != null)
               {
               	result.setQuery(query);
               }
               else
               {
               	result.setQuery(baseRef.getQuery());
               }
      		}
      		else
      		{
               if(path.startsWith("/"))
               {
               	result.setPath(path);
               }
               else
               {
            		String basePath = baseRef.getPath();
            		String mergedPath = null;
            		
            		if((baseRef.getAuthority() != null) && ((basePath == null) || (basePath.equals(""))))
            		{
            			mergedPath = "/" + path;
            		}
            		else
            		{
               		// Remove the last segment which may be empty if the path is ending with a slash
               		int lastSlash = basePath.lastIndexOf('/');
               		if(lastSlash == -1)
               		{
               			mergedPath = path;
               		}
               		else
               		{
               			mergedPath = basePath.substring(0, lastSlash + 1) + path;
               		}
            		}               	
               	
               	result.setPath(mergedPath);
               }
            
               result.setQuery(query);      			
      		}
   		}
      	
      	result.setFragment(fragment);
   	}
   	
   	// Step 2 - Normalize the target reference
   	result.normalize();
  	
   	return result;
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
   	String result = null;
      String authority = getAuthority();
      
      if(authority != null)
      {
	      int index1 = authority.indexOf('@');
	      int index2 = authority.indexOf(':');
	
	      if(index1 != -1)
	      {
	         // User info found
	         if(index2 != -1)
	         {
	            // Port found
	            result = authority.substring(index1 + 1, index2);
	         }
	         else
	         {
	            // No port found
	         	result = authority.substring(index1 + 1);
	         }
	      }
	      else
	      {
	         // No user info found
	         if(index2 != -1)
	         {
	            // Port found
	         	result = authority.substring(0, index2);
	         }
	         else
	         {
	            // No port found
	         	result = authority;
	         }
	      }
      }
      
      return result;
   }

   /**
    * Returns the optional port number for server based hierarchical identifiers.
    * @return The optional port number for server based hierarchical identifiers.
    */
   public Integer getHostPort()
   {
   	Integer result = null;
      String authority = getAuthority();
      
      if(authority != null)
      {
	      int index = authority.indexOf(':');
	
	      if(index != -1)
	      {
	         result = Integer.valueOf(authority.substring(index + 1));
	      }
      }
      
      return result;
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
    * Returns the path component for hierarchical identifiers.
    * @return The path component for hierarchical identifiers.
    */
   public String getPath()
   {
   	String result = null;
      String part = isRelative() ? getRelativePart() : getSchemeSpecificPart();
	
      if(part != null)
      {
	      if(part.startsWith("//"))
	      {
	         // Authority found
	         int index1 = part.indexOf('/', 2);
	
	         if(index1 != -1)
	         {
	            // Path found
	            int index2 = part.indexOf('?');
	            if(index2 != -1)
	            {
	               // Query found
	            	result = part.substring(index1, index2);
	            }
	            else
	            {
	               // No query found
	            	result = part.substring(index1);
	            }
	         }
	         else
	         {
	         	// Path must be empty in this case
	         }
	      }
	      else
	      {
	         // No authority found
	         int index = part.indexOf('?');
	         if(index != -1)
	         {
	            // Query found
	         	result = part.substring(0, index);
	         }
	         else
	         {
	            // No query found
	         	result = part;
	         }
	      }
      }
      
      return result;
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
               	// End of local path reached
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
                     boolean firstAdd = true;
                     boolean canAdd = false;
                     
                     for(int j = i; j < basePath.length(); j++)
                     {
                        if(basePath.charAt(j) == '/')
                        {
                           canAdd = true;
                        }
                        else if(canAdd)
                        {
                        	if(firstAdd)
                        	{
                     			firstAdd = false;
                        	}
                        	else
                        	{
                        		sb.append("/");
                        	}

                        	sb.append("..");
                           canAdd = false;
                        }
                     }
                     
                     if(lastSlashIndex + 1 < localPath.length())
                     {
                     	if(!firstAdd) sb.append('/');
                     	sb.append(localPath.substring(lastSlashIndex + 1));
                     }
                     
                     relativePath = sb.toString();
                     
                     if(relativePath.equals("")) relativePath = ".";
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
               boolean canAdd = false;
               boolean firstAdd = true;
               
               for(int j = i; j < basePath.length(); j++)
               {
                  if(basePath.charAt(j) == '/')
                  {
                     canAdd = true;
                  }
                  else if(canAdd)
                  {
                  	if(firstAdd)
                  	{
               			firstAdd = false;
                  	}
                  	else
                  	{
                  		sb.append("/");
                  	}
                  	
                     sb.append("..");
                     canAdd = false;
                  }
               }
               
            	if(!firstAdd) sb.append('/');
            	sb.append(localPath.substring(lastSlashIndex + 1));
               relativePath = sb.toString();
            }
         }
         
         // Builde the result reference
         result = new Reference();
      	String query = getQuery();
      	String fragment = getFragment();
         boolean modified = false;
         
      	if((query != null) && (!query.equals(base.getQuery())))
      	{
      		result.setQuery(query);
      		modified = true;
      	}
      	
      	if((fragment != null) && (!fragment.equals(base.getFragment())))
      	{
      		result.setFragment(fragment); 
      		modified = true;
      	}
      	
      	if(!modified || !relativePath.equals("."))
      	{
         	result.setPath(relativePath);
      	}
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
      return new Form(getQuery());
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
    * Returns the protocol associated to the scheme component.
    * @return The protocol associated to the scheme component.
    */
   public Protocol getProtocol()
   {
   	return Protocols.create(getScheme());
   }
   
   /**
    * Returns the scheme specific part.
    * @return The scheme specific part.
    */
   public String getSchemeSpecificPart()
   {
   	String result = null;
   	
      if(schemeIndex != -1)
      {
         // Scheme found
         if(fragmentIndex != -1)
         {
            // Fragment found
            result = this.uri.substring(schemeIndex + 1, fragmentIndex);
         }
         else
         {
            // No fragment found
         	result = this.uri.substring(schemeIndex + 1);
         }
      }
      
      return result;
   }

   /**
    * Returns the relative part for relative references only.
    * @return The relative part for relative references only.
    */
   public String getRelativePart()
   {
   	String result = null;
   	
      if(schemeIndex == -1)
      {
         // This is a relative reference, no scheme found
      	if(queryIndex != -1)
      	{
      		// Query found
      		result = this.uri.substring(0, queryIndex);
      	}
      	else
      	{
	         if(fragmentIndex != -1)
	         {
	            // Fragment found
	         	result = this.uri.substring(0, fragmentIndex);
	         }
	         else
	         {
	            // No fragment found
	         	result = this.uri;
	         }
      	}
      }
      
      return result;
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
    * Returns the hierarchical part which is equivalent to the scheme specific part less the query component.
    * @return The hierarchical part .
    */
   public String getHierarchicalPart()
   {
      if(schemeIndex != -1)
      {
         // Scheme found
      	if(queryIndex != -1)
      	{
      		// Query found
      		return this.uri.substring(schemeIndex + 1, queryIndex);
      	}
      	else
      	{
      		// No query found
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
      }
      else
      {
         // No scheme found
      	if(queryIndex != -1)
      	{
      		// Query found
      		return this.uri.substring(0, queryIndex);
      	}
      	else
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
      }
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
   	String result = null;
      String authority = getAuthority();
      
      if(authority != null)
      {
	      int index = authority.indexOf('@');
	
	      if(index != -1)
	      {
	         result = authority.substring(0, index);
	      }
      }
      
      return result;
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
      String oldPart = isRelative() ? getRelativePart() : getSchemeSpecificPart();
   	String newPart;
      String newAuthority = (authority == null) ? "" : "//" + authority;

      if(oldPart.startsWith("//"))
      {
         int index = oldPart.indexOf('/', 2);

         if(index != -1)
         {
            newPart = newAuthority + oldPart.substring(index);
         }
         else
         {
            index = oldPart.indexOf('?');
            if(index != -1)
            {
            	newPart = newAuthority + oldPart.substring(index);
            }
            else
            {
            	newPart = newAuthority;
            }
         }
      }
      else
      {
      	newPart = newAuthority + oldPart;
      }
      
   	if(isAbsolute())
   	{
	      setSchemeSpecificPart(newPart);
   	}
   	else
   	{
   		setRelativePart(newPart);
   	}
   }

   /**
    * Sets the fragment identifier.
    * @param fragment The fragment identifier.
    */
   public void setFragment(String fragment)
   {
      if((fragment != null) && (fragment.indexOf('#') != -1))
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
            	if(this.uri != null)
            	{
            		this.uri = this.uri + '#' + fragment;
            	}
            	else
            	{
            		this.uri = '#' + fragment;
            	}
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
      
      if(authority != null)
      {
	   	if(host == null) 
	   	{
	   		host = "";
	   	}
	   	else
	   	{
	   		// URI specification indicates that host names should be produced in lower case
	   		host = host.toLowerCase();
	   	}
	   	
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
	         if(index2 != -1)
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
   }

   /**
    * Sets the optional port number for server based hierarchical identifiers.
    * @param port The optional port number for server based hierarchical identifiers.
    */
   public void setHostPort(Integer port)
   {
      String authority = getAuthority();
      
      if(authority != null)
      {
	      int index = authority.indexOf(':');
	      String newPort = (port == null) ? "" : ":" + port;
	
	      if(index != -1)
	      {
	         setAuthority(authority.substring(0, index) + newPort);
	      }
	      else
	      {
	         setAuthority(authority + newPort);
	      }
      }
      else
      {
      	throw new IllegalArgumentException("No authority defined, please define a host name first");
      }
   }

   /**
    * Sets the absolute resource identifier.
    * @param identifier The absolute resource identifier.
    */
   public void setIdentifier(String identifier)
   {
   	if(identifier == null) identifier = "";
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
      String oldPart = isRelative() ? getRelativePart() : getSchemeSpecificPart();
   	String newPart = null;
   	
   	if(oldPart != null)
   	{
      	if(path == null) path = "";

      	if(oldPart.startsWith("//"))
	      {
	         // Authority found
	         int index1 = oldPart.indexOf('/', 2);
	
	         if(index1 != -1)
	         {
	            // Path found
	            int index2 = oldPart.indexOf('?');
	            if(index2 != -1)
	            {
	               // Query found
	            	newPart = oldPart.substring(0, index1) + path + oldPart.substring(index2);
	            }
	            else
	            {
	               // No query found
	            	newPart = oldPart.substring(0, index1) + path;
	            }
	         }
	         else
	         {
	            // No path found
	            int index2 = oldPart.indexOf('?');
	            if(index2 != -1)
	            {
	               // Query found
	            	newPart = oldPart.substring(0, index2) + path + oldPart.substring(index2);
	            }
	            else
	            {
	               // No query found
	            	newPart = oldPart + path;
	            }
	         }
	      }
	      else
	      {
	         // No authority found
	         int index = oldPart.indexOf('?');
	         if(index != -1)
	         {
	            // Query found
	         	newPart = path + oldPart.substring(index);
	         }
	         else
	         {
	            // No query found
	         	newPart = path;
	         }
	      }
   	}
   	else
   	{
   		newPart = path;
   	}
   	
   	if(isAbsolute())
   	{
	      setSchemeSpecificPart(newPart);
   	}
   	else
   	{
   		setRelativePart(newPart);
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
            	if(this.uri != null)
            	{
            		this.uri = this.uri + '?' + query;
            	}
            	else
            	{
            		this.uri = '?' + query;
            	}
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
   	if(scheme != null)
   	{
   		// URI specification indicates that scheme names should be produced in lower case
   		scheme = scheme.toLowerCase();
   	}
   	
      if(schemeIndex != -1)
      {
         // Scheme found
      	if(scheme != null)
      	{
      		this.uri = scheme + this.uri.substring(schemeIndex);
      	}
      	else
      	{
      		this.uri = this.uri.substring(schemeIndex + 1);
      	}
      }
      else
      {
         // No scheme found
      	if(scheme != null)
      	{
	         if(this.uri == null)
	         {
	            this.uri = scheme + ':';
	         }
	         else
	         {
	            this.uri = scheme + ':' + this.uri;
	         }
      	}
      }

      updateIndexes();
   }

   /**
    * Sets the scheme component based on this protocol.
    * @param protocol The protocol of the scheme component.
    */
   public void setProtocol(Protocol protocol)
   {
   	setScheme(protocol.getSchemeName());
   }

   /**
    * Sets the scheme specific part.
    * @param schemeSpecificPart The scheme specific part.
    */
   public void setSchemeSpecificPart(String schemeSpecificPart)
   {
   	if(schemeSpecificPart == null) schemeSpecificPart = "";
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
    * Sets the relative part for relative references only.
    * @param relativePart The relative part to set.
    */
   public void setRelativePart(String relativePart)
   {
   	if(relativePart == null) relativePart = "";
      if(schemeIndex == -1)
      {
         // This is a relative reference, no scheme found
      	if(queryIndex != -1)
      	{
      		// Query found
      		this.uri = relativePart + this.uri.substring(queryIndex);
      	}
      	else if(fragmentIndex != -1)
	      {
	      	// Fragment found
	         this.uri = relativePart + this.uri.substring(fragmentIndex);
	      }
	      else
	      {
	      	// No fragment found
	         this.uri = relativePart;
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
      
      if(authority != null)
      {
	      int index = authority.indexOf('@');
	      String newUserInfo = (userInfo == null) ? "" : userInfo + '@';

	      if(index != -1)
	      {
	         setAuthority(newUserInfo + authority.substring(index + 1));
	      }
	      else
	      {
	         setAuthority(newUserInfo + authority);
	      }
   	}
	   else
	   {
	   	throw new IllegalArgumentException("No authority defined, please define a host name first");
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
         this.schemeIndex = this.uri.indexOf(':');
         this.queryIndex = this.uri.indexOf('?');
         this.fragmentIndex = this.uri.indexOf('#');
      }
      else
      {
         this.schemeIndex = -1;
         this.queryIndex = -1;
         this.fragmentIndex = -1;
      }
   }
   
   /**
    * Creates an URI from its parts.
    * @param scheme The scheme ("http", "https" or "ftp").
    * @param hostName The host name or IP address.
    * @param hostPort The host port (default ports are correctly ignored).
    * @param path The path component for hierarchical identifiers.
    * @param query The optional query component for hierarchical identifiers.
    * @param fragment The optionale fragment identifier.
    */
   public static String toUri(String scheme, String hostName, Integer hostPort, String path, String query, String fragment)
   {
   	StringBuilder sb = new StringBuilder();

   	// Append the scheme and host name
   	sb.append(scheme).append("://").append(hostName);

   	// Append the host port number
   	if(hostPort != null)
   	{
   		if((scheme.equals("ftp")   && (hostPort != 21)) ||
   			(scheme.equals("http")  && (hostPort != 80)) || 
   			(scheme.equals("https") && (hostPort != 443)))
   		{
   			sb.append(':').append(hostPort);
   		}
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
      return sb.toString();
   }
   
   /**
    * Creates a relative URI from its parts.
    * @param relativePart The relative part component.
    * @param query The optional query component for hierarchical identifiers.
    * @param fragment The optionale fragment identifier.
    */
   public static String toUri(String relativePart, String query, String fragment)
   {
   	StringBuilder sb = new StringBuilder();

   	// Append the path
   	if(relativePart != null)
   	{
   		sb.append(relativePart);
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
      return sb.toString();
   }
   
}
