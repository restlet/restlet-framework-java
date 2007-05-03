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

package org.restlet.ext.velocity;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;

/**
 * Adapter between the Velocity Log system and the JDK's Logger system.
 * @author Jerome Louvel (contact@noelios.com)
 */
public class JdkLogSystem implements LogSystem
{
	/** The JDK's logger. */
	private Logger logger;

	/**
	 * Initialize the log system. Creates a new JDK Logger instance.
	 * @param rs The runtime services.
	 */
	public void init(RuntimeServices rs) throws Exception
	{
		this.logger = Logger.getLogger("org.restlet.ext.velocity");
	}

	/**
	 * Logs a Velocity message.
	 * @param level The priority level.
	 * @param message The message to log.
	 */
	public void logVelocityMessage(int level, String message)
	{
		this.logger.log(getLevel(level), message);
	}

	/**
	 * Converts a Velocity level into a JDK's Logger level.
	 * @param velocityLevel The Velocity level to convert.
	 * @return The JDK's Logger level.
	 */
	public static Level getLevel(int velocityLevel)
	{
		switch (velocityLevel)
		{
			case DEBUG_ID:
				return Level.FINE;
			case ERROR_ID:
				return Level.SEVERE;
			case INFO_ID:
				return Level.INFO;
			case WARN_ID:
				return Level.WARNING;
			default:
				return Level.INFO;
		}
	}

}
