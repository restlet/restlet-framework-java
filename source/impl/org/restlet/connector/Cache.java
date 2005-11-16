/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.restlet.connector;

/**
 * Connector used to reduce interaction latency.<br/><br/>
 * "The cache connector, can be located on the interface to a client or server connector in order to save cacheable responses
 * to current interactions so that they can be reused for later requested interactions. A cache may be used by a client to
 * avoid repetition of network communication, or by a server to avoid repeating the process of generating a response, with
 * both cases serving to reduce interaction latency. A cache is typically implemented within the address space of
 * the connector that uses it." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_2">Source dissertation</a>
 */
public interface Cache extends Connector
{
}




