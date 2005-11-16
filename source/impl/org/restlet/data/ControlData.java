/*
 * Copyright © 2005 Jérôme LOUVEL.  All Rights Reserved.
 */

package org.restlet.data;

/**
 * Definition of the purpose of a message between components.<br/><br/>
 * "Control data defines the purpose of a message between components, such as the action being requested or the meaning of a
 * response. It is also used to parameterize requests and override the default behavior of some connecting elements. For
 * example, cache behavior can be modified by control data included in the request or response message." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_1_2">Source dissertation</a>
 */
public interface ControlData extends Data
{
}




