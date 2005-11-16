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

package org.restlet.component;

/**
 * Intermediary component providing transparent encapsulation of other services.<br/><br/>
 * "A gateway (a.k.a., reverse proxy) component is an intermediary imposed by the network or origin server to provide an
 * interface encapsulation of other services, for data translation, performance enhancement, or security enforcement. Note
 * that the difference between a proxy and a gateway is that a client determines when it will use a proxy." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_3">Source dissertation</a>
 */
public interface Gateway extends Component
{
}




