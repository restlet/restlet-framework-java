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

package com.noelios.restlet;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.restlet.AbstractHandler;
import org.restlet.Call;
import org.restlet.DefaultRouter;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.component.Component;
import org.restlet.data.DefaultStatus;
import org.restlet.data.Protocol;
import org.restlet.data.Protocols;
import org.restlet.data.ScorerList;
import org.restlet.data.Status;
import org.restlet.data.Statuses;

/**
 * Router associated with a host and allowing different alias URI patterns. After configuration , you can use 
 * the getPattern() method to attach your HostRouter to a RestletContainer or a root router. Target Restlets 
 * can also be attached to a HostRouter for further delegation. By default, the supported protocol is HTTP, 
 * "localhost" URIs and IP-based URIs are allowed. However, client redirections to the preferred format are 
 * not issued automatically. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HostRouter extends AbstractHandler implements Router
{
	/**
	 * Usage mode for the HostRouter. 
	 */
	public enum UsageMode 
	{
		/**
		 * Default mode used when the HostRouter is attached to a Filter (or to a RestletContainer using
		 * an attach() method with no parameter. In this mode, the HostRouter is setting the contextPath
		 * of the handler call itself.
		 */
		FILTER, 
		
		/**
		 * Mode used when the HostRouter is attached to a parent router (or to a RestletContainer using
		 * an attach() method with a URI pattern parameter. In this mode, the parent router is setting the
		 * contextPath before delegating to call to the HostRouter. When setting the URI pattern for the
		 * parent router, just use the getPattern() method after having configured your HostRouter instance. 
		 */
		ROUTER
	};
	
	/**
	 * The usage mode. 
	 */
	protected UsageMode mode;
	
	/** 
	 * The list of allowed protocols. 
	 * Useful if both HTTP and HTTPS are allowed for example. 
	 */
	protected List<Protocol> allowedProtocols;
	
	/** 
	 * The list of allowed domain names. 
	 * If IP addresses are allowed they will be looked up from these names. 
	 */
	protected List<String> allowedDomains;
	
	/**
	 * The list of allowed port numbers.
	 */
	protected List<Integer> allowedPorts;
	
	/**
	 * The preferred protocol.
	 * Used to detect if client redirects or warnings must be issued. 
	 */
	protected Protocol preferredProtocol;
	
	/**
	 * The preferred domain name.
	 * Used to detect if client redirects or warnings must be issued. 
	 */
	protected String preferredDomain;
	
	/**
	 * The preferred port.
	 * Used to detect if client redirects or warnings must be issued. 
	 */
	protected int preferredPort;
	
	/** The preferred URI start based on other preferences for protocol, domain and port. */
	protected String preferredUri;
	
	/**
	 * Indicates if client redirects should be issued when the host URI doesn't match the preferred format.
	 */
	protected boolean redirectClient;

	/**
	 * Indicates the redirection status to use.
	 * @see org.restlet.test.data.Statuses.REDIRECTION_MOVED_PERMANENTLY
	 * @see org.restlet.test.data.Statuses.REDIRECTION_FOUND
	 * @see org.restlet.test.data.Statuses.REDIRECTION_MOVED_TEMPORARILY
	 */
	protected Status redirectStatus;
	
	/**
	 * Indicates if client warnings should be issued when the host URI doesn't match the preferred format.
	 * This will materialize as a Not Found status with a detailled explanation.
	 */
	protected boolean warnClient;
	
	/**
	 * Indicates if the IP addresses, equivalent of the domain names,
	 * are allowed as a way to specify URIs.
	 */
	protected boolean allowIpAddresses;
	
	/**
	 * Indicates if "localhost" is accepted as a valid domain name.
	 * In addition, if IP addresses are allowed, "127.0.0.1" is also allowed.
	 */
	protected boolean allowLocalHost;
	
	/**
	 * Indicates if default ports for the allowed protocols are allowed.
	 * Concretely allow the usage of any URI without explicit port number.
	 */
	protected boolean allowDefaultPorts;
	
	/**
	 * Front router only used in the Filter usage mode.
	 * Its target is the internal delegate router.
	 */
	protected Router frontRouter;
	
	/**
	 * Back router used in all usage modes.
	 */
	protected Router backRouter;
	
   /**
    * Constructor to match the machine's host name on port 80.
    * @param owner The owner component.
    */
   public HostRouter(Component owner)
   {
		this(owner, 80);
   }
	
   /**
    * Constructor to match a given host name on port 80.
    * @param owner The owner component.
    * @param domain The domain name.
    */
   public HostRouter(Component owner, String domain)
   {
   	this(owner, domain, 80);
   }
	
   /**
    * Constructor to match the machine's host name on a given port.
    * @param owner The owner component.
    * @param port The port number.
    */
   public HostRouter(Component owner, int port)
   {
		this(owner, getLocalHostName(), port);
   }
   
   /**
    * Constructor to match a given host name on a given port.
    * @param owner The owner component.
    * @param domain The domain name.
    * @param port The port number.
    */
   public HostRouter(Component owner, String domain, int port)
   {
   	super(owner);
      this.mode = UsageMode.FILTER;
      this.allowedProtocols = new ArrayList<Protocol>();
      this.allowedProtocols.add(Protocols.HTTP);
      this.allowedDomains = new ArrayList<String>();
      if(domain != null) this.allowedDomains.add(domain);
      this.allowedPorts = new ArrayList<Integer>();
      this.allowedPorts.add(port);
      this.preferredProtocol = Protocols.HTTP;
      this.preferredDomain = domain;
      this.preferredPort = port;
      this.redirectClient = false;
      this.redirectStatus = Statuses.REDIRECTION_FOUND;
      this.warnClient = false;
      this.allowIpAddresses = true;
      this.allowLocalHost = true;
      this.allowDefaultPorts = true;
      this.frontRouter = null;
      this.backRouter = new DefaultRouter(owner);
      updatePreferredUri();
   }

   /**
    * Returns the attachment mode.
    * @return The attachment mode.
    */
   public UsageMode getUsageMode()
   {
   	return this.mode;
   }

   /**
    * Sets the attachment mode.
    * @param mode The attachment mode.
    */
   public void setMode(UsageMode mode)
   {
   	this.mode = mode;
   }
   
   /**
    * Returns the path pattern that can be used to attach the HostRouter to a parent RestletContainer/Router.
    * This pattern is dynamically generated based on the current configuration.
    * @return The path pattern that can be used to attach the HostRouter to a parent RestletContainer/Router.
    */
   public String getPattern()
   {
   	StringBuilder sb = new StringBuilder();
   	boolean first = true;
   	
   	// Append the allowed protocol scheme names
		sb.append('(');
   	for(Protocol protocol : getAllowedProtocols())
   	{
   		if(first)
   		{
   			first = false;
   		}
   		else
   		{
      		sb.append('|');
   		}

   		sb.append("((?i)"); // Scheme names are case insensitive
   		sb.append(Pattern.quote(protocol.getSchemeName()));
   		sb.append(Pattern.quote("://"));
   		sb.append(')');
   	}
		sb.append(')');
   	
   	// Append the allowed host names
		sb.append('(');
		first = true;
		
   	// Append the allowed domain names
   	for(String domain: getAllowedDomains())
   	{
   		if(first)
   		{
   			first = false;
   		}
   		else
   		{
      		sb.append('|');
   		}

   		sb.append("((?i)"); // Domain names are case insensitive
   		sb.append(Pattern.quote(domain));
   		sb.append(')');
   		
   		if(isAllowIpAddresses())
   		{
      		sb.append('|');
      		sb.append('(');
      		sb.append(Pattern.quote(getIpAddress(domain)));
      		sb.append(')');
   		}
   	}

   	// Append special "localhost" URIs
		if(isAllowLocalHost())
		{
   		if(first)
   		{
   			first = false;
   		}
   		else
   		{
      		sb.append('|');
   		}

   		sb.append("((?i)localhost)");

   		if(isAllowIpAddresses())
			{
	   		sb.append('|');
	   		sb.append('(');
	   		sb.append(Pattern.quote("127.0.0.1"));
	   		sb.append(')');
			}
		}

		sb.append(')');
   	
   	// Append the allowed port numbers
		sb.append('(');
		first = true;
		
   	for(Integer port : getAllowedPorts())
   	{
   		if(first)
   		{
   			first = false;
   		}
   		else
   		{
      		sb.append('|');
   		}

   		sb.append("(:");
   		sb.append(port.toString());
   		sb.append(')');
   	}
		sb.append(')');
		
		if(isAllowDefaultPorts())
		{
			sb.append('?');
		}
		
   	return sb.toString();
   }

   /**
    * Updates the preference URI that is used to detect conformance of calls.
    */
   protected void updatePreferredUri()
   {
   	StringBuilder sb = new StringBuilder();
   	sb.append(getPreferredProtocol().getSchemeName()).append("://");
   	sb.append(getPreferredDomain());
   	
   	if(getPreferredPort() != getPreferredProtocol().getDefaultPort())
   	{
   		sb.append(':').append(getPreferredPort());
   	}
   	
   	this.preferredUri = sb.toString();
   }
   
   /**
    * Handles a call.
    * @param call The call to handle.
    */
	public void handle(Call call)
   {
   	boolean handle = true;
   	
		if(isRedirectClient() || isWarnClient())
		{
	   	// Check if the preferred URI format was respected 
	   	if(!call.getResourceRef().toString(false, false).startsWith(getPreferredUri()))
	   	{
				if(isRedirectClient())
			   {
			   	// Redirect the caller to the preferred format
					call.setRedirectRef(getPreferredUri() + call.getContext().getRelativeRef());
					call.setStatus(getRedirectStatus());
				}
				else if(isWarnClient())
				{
		   		// Redirect the caller to the preferred format
					String description = "Used this URI instead: " + getPreferredUri() + call.getContext().getRelativeRef(); 
					call.setStatus(new DefaultStatus(Statuses.CLIENT_ERROR_BAD_REQUEST, description));
			   }

				// We'll stop the call handling here 
				handle = false;
	   	}
		}

		if(handle)
		{
			// Actually handles the call.
	   	if(getUsageMode() == UsageMode.FILTER)
	   	{
	   		// First test outside the synchronized block
	   		if(this.frontRouter == null)
	   		{
	   			synchronized(this)
	   			{
	   				// We test again after synchronization
	   	   		if(this.frontRouter == null)
	   	   		{
	   	   			this.frontRouter = new DefaultRouter(getOwner());
	   	   			this.frontRouter.getScorers().add(getPattern(), this.backRouter);
	   	   		}
	   			}
	   		}
	   		
	   		this.frontRouter.handle(call);
	   	}
	   	else if(getUsageMode() == UsageMode.ROUTER)
	   	{
	   		this.backRouter.handle(call);
	   	}
		}
   }

   /**
    * Returns the local host name.
    * @return The local host name.
    */
   public static String getLocalHostName()
   {
   	String result = null;
   	
   	try
		{
			result = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e)
		{
		}
		
		return result;
   }

   /**
    * Returns the IP address of a given domain name.
    * @param domain The domain name.
    * @return The IP address.
    */
   public static String getIpAddress(String domain)
   {
   	String result = null;
   	
   	try
		{
			result = InetAddress.getByName(domain).getHostAddress();
		}
		catch (UnknownHostException e)
		{
		}
		
		return result;
   }

	/** 
	 * Returns the list of allowed protocols. 
	 * Useful if both HTTP and HTTPS are allowed for example.
	 * @return The list of allowed protocols.
	 */
   public List<Protocol> getAllowedProtocols()
   {
   	return this.allowedProtocols;
   }
   
	/** 
	 * Returns the list of allowed domain names. 
	 * If IP addresses are allowed they will be looked up from these names. 
	 * @return The list of allowed domain names.
	 */
   public List<String> getAllowedDomains()
   {
   	return this.allowedDomains;
   }
   
	/** 
	 * Returns the list of allowed ports. 
	 * @return The list of allowed protocols.
	 */
   public List<Integer> getAllowedPorts()
   {
   	return this.allowedPorts;
   }

   /**
	 * Returns the preferred protocol.
	 * Used to detect if client redirects or warnings must be issued. 
    * @return The preferred protocol.
    */
   public Protocol getPreferredProtocol()
   {
   	return this.preferredProtocol;
   }

   /**
	 * Sets the preferred protocol.
	 * Used to detect if client redirects or warnings must be issued. 
    * @param preferredProtocol The preferred protocol.
    */
   public void setPreferredProtocol(Protocol preferredProtocol)
   {
   	this.preferredProtocol = preferredProtocol;
   	updatePreferredUri();
   }
   
   /**
	 * Returns the preferred domain name.
	 * Used to detect if client redirects or warnings must be issued. 
    * @return The preferred domain name.
    */
   public String getPreferredDomain()
   {
   	return this.preferredDomain;
   }

   /**
	 * Sets the preferred domain name.
	 * Used to detect if client redirects or warnings must be issued. 
    * @param preferredDomain The preferred domain name.
    */
   public void setPreferredDomain(String preferredDomain)
   {
   	this.preferredDomain = preferredDomain;
   	updatePreferredUri();
   }
   
   /**
	 * Returns the preferred port.
	 * Used to detect if client redirects or warnings must be issued. 
    * @return The preferred port.
    */
   public int getPreferredPort()
   {
   	return this.preferredPort;
   }

   /**
	 * Sets the preferred port.
	 * Used to detect if client redirects or warnings must be issued. 
    * @param preferredPort The preferred port.
    */
   public void setPreferredPort(int preferredPort)
   {
   	this.preferredPort = preferredPort;
   	updatePreferredUri();
   }
   
   /**
	 * Returns the preferred URI start as defined by other properties (preferred protocol, domain, port and root path).
	 * Used to detect if client redirects or warnings must be issued. 
    * @return The preferred root path.
    */
   public String getPreferredUri()
   {
   	return this.preferredUri;
   }

   /**
    * Indicates if client redirects should be issued when the host URI doesn't match the preferred format.
    * @return True if client redirects should be issued.
    */
   public boolean isRedirectClient()
   {
   	return this.redirectClient;
   }

   /**
    * Indicates if client redirects should be issued when the host URI doesn't match the preferred format.
    * @param redirectClient True if client redirects should be issued.
    */
   public void setRedirectClient(boolean redirectClient)
   {
   	this.redirectClient = redirectClient;
   }

	/**
	 * Indicates the redirection status used.
	 * @return The redirection status used.
	 */
	public Status getRedirectStatus()
	{
		return this.redirectStatus;
	}

	/**
	 * Indicates the redirection status used.
	 * @param status The redirection status used.
	 */
	public void setRedirectStatus(Status status)
	{
		this.redirectStatus = status;
	}

   /**
    * Indicates if client warnings should be issued when the host URI doesn't match the preferred format.
	 * This will materialize as a Not Found status with a detailled explanation.
    * @return True if client warnings should be issued.
    */
   public boolean isWarnClient()
   {
   	return this.warnClient;
   }

   /**
    * Indicates if client warnings should be issued when the host URI doesn't match the preferred format.
	 * This will materialize as a Not Found status with a detailled explanation.
    * @param warnClient True if client warnings should be issued.
    */
   public void setWarnClient(boolean warnClient)
   {
   	this.warnClient = warnClient;
   }

   /**
    * Indicates if the IP addresses, equivalent of the domain names, are allowed as a way to specify URIs.
    * @return True if the IP addresses, equivalent of the domain names, are allowed as a way to specify URIs.
    */
   public boolean isAllowIpAddresses()
   {
   	return this.allowIpAddresses;
   }
   
   /**
    * Indicates if the IP addresses, equivalent of the domain names, are allowed as a way to specify URIs.
    * @param allowIpAddresses True if the IP addresses, equivalent of the domain names, are allowed as a way to specify URIs.
    */
   public void setAllowIpAddresses(boolean allowIpAddresses)
   {
   	this.allowIpAddresses = allowIpAddresses;
   }

   /**
	 * Indicates if "localhost" is accepted as a valid domain name.
	 * In addition, if IP addresses are allowed, "127.0.0.1" is also allowed.
	 * @return True if "localhost" is accepted as a valid domain name.
	 */
	public boolean isAllowLocalHost()
	{
		return this.allowLocalHost;
	}

   /**
	 * Indicates if "localhost" is accepted as a valid domain name.
	 * In addition, if IP addresses are allowed, "127.0.0.1" is also allowed.
	 * @param allowLocalHost True if "localhost" is accepted as a valid domain name.
	 */
	public void setAllowLocalHost(boolean allowLocalHost)
	{
		this.allowLocalHost = allowLocalHost;
	}

	/**
	 * Indicates if default ports for the allowed protocols are allowed.
	 * Concretely allow the usage of any URI without explicit port number.
	 * @return True if default ports for the allowed protocols are allowed.
	 */
	public boolean isAllowDefaultPorts()
	{
		return this.allowDefaultPorts;
	}

	/**
	 * Indicates if default ports for the allowed protocols are allowed.
	 * Concretely allow the usage of any URI without explicit port number.
	 * @param allowDefaultPorts True if default ports for the allowed protocols are allowed.
	 */
	public void setAllowDefaultPorts(boolean allowDefaultPorts)
	{
		this.allowDefaultPorts = allowDefaultPorts;
	}
	
	
	// -------------------------
	// ROUTER METHODS DELEGATION
	// -------------------------

	/**
	 * Finds the next Restlet if available.
	 * @param call The current call.
	 * @return The next Restlet if available or null.
	 */
	public Restlet findNext(Call call)
	{
		return this.backRouter.findNext(call);
	}
	
	/**
	 * Returns the modifiable list of scorers.
	 * @return The modifiable list of scorers.
	 */
	public ScorerList getScorers()
	{
		return this.backRouter.getScorers();
	}

	/**
	 * Returns the routing mode.
	 * @return The routing mode.
	 */
	public Mode getMode()
	{
		return this.backRouter.getMode();
	}
	
	/**
	 * Sets the routing mode.
	 * @param mode The routing mode.
	 */
	public void setMode(Mode mode)
	{
		this.backRouter.setMode(mode);
	}
	
	/**
	 * Returns the minimum score required to have a match.
	 * @return The minimum score required to have a match.
	 */
	public float getRequiredScore()
	{
		return this.backRouter.getRequiredScore();
	}
	
	/**
	 * Sets the score required to have a match.
	 * @param score The score required to have a match.
	 */
	public void setRequiredScore(float score)
	{
		this.backRouter.setRequiredScore(score);
	}

	/**
	 * Returns the maximum number of attempts if no attachment could be matched on the first attempt.
	 * This is useful when the attachment scoring is dynamic and therefore could change on a retry.
	 * @return The maximum number of attempts if no attachment could be matched on the first attempt.
	 */
	public int getMaxAttempts()
	{
		return this.backRouter.getMaxAttempts();
	}
	
	/**
	 * Sets the maximum number of attempts if no attachment could be matched on the first attempt.
	 * This is useful when the attachment scoring is dynamic and therefore could change on a retry.
	 * @param maxAttempts The maximum number of attempts. 
	 */
	public void setMaxAttempts(int maxAttempts)
	{
		this.backRouter.setMaxAttempts(maxAttempts);
	}

	/**
	 * Returns the delay (in seconds) before a new attempt.
	 * @return The delay (in seconds) before a new attempt.
	 */
	public long getRetryDelay()
	{
		return this.backRouter.getRetryDelay();
	}
	
	/**
	 * Sets the delay (in seconds) before a new attempt.
	 * @param delay The delay (in seconds) before a new attempt.
	 */
	public void setRetryDelay(long delay)
	{
		this.backRouter.setRetryDelay(delay);
	}

}
