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

package com.noelios.restlet.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;

import com.noelios.restlet.util.CookieReader;
import com.noelios.restlet.util.CookieUtils;
import com.noelios.restlet.util.DateUtils;

/**
 * Unit tests for the Cookie related classes.
 */
public class CookiesTest extends TestCase
{
   /**
    * Tests the cookies parsing.
    */
   public void testParsing() throws IOException
   {
      // Netscape speficiation
      testCookie("CUSTOMER=WILE_E_COYOTE");
      testCookie("CUSTOMER=WILE_E_COYOTE; PART_NUMBER=ROCKET_LAUNCHER_0001");
      testCookie("CUSTOMER=WILE_E_COYOTE; PART_NUMBER=ROCKET_LAUNCHER_0001; SHIPPING=FEDEX");
      testCookie("NUMBER=RIDING_ROCKET_0023; PART_NUMBER=ROCKET_LAUNCHER_0001");
      
      testCookieSetting("CUSTOMER=WILE_E_COYOTE; path=/");
      testCookieSetting("PART_NUMBER=ROCKET_LAUNCHER_0001; path=/");
      testCookieSetting("SHIPPING=FEDEX; path=/foo");
      testCookieSetting("NUMBER=RIDING_ROCKET_0023; path=/ammo");
      
      testCookieDate("Tuesday, 09-Nov-99 23:12:40 GMT");
      
      // RFC 2109
      testCookie("$Version=\"1\"; Customer=\"WILE_E_COYOTE\"; $Path=\"/acme\"");
      testCookie("$Version=\"1\"; Customer=\"WILE_E_COYOTE\"; $Path=\"/acme\"; Part_Number=\"Rocket_Launcher_0001\"; $Path=\"/acme\"");
      testCookie("$Version=\"1\"; Customer=\"WILE_E_COYOTE\"; $Path=\"/acme\"; Part_Number=\"Rocket_Launcher_0001\"; $Path=\"/acme\"; Shipping=\"FedEx\"; $Path=\"/acme\"");
      testCookie("$Version=\"1\"; Part_Number=\"Riding_Rocket_0023\"; $Path=\"/acme/ammo\"; Part_Number=\"Rocket_Launcher_0001\"; $Path=\"/acme\"");
      
      testCookieSetting("Customer=\"WILE_E_COYOTE\"; Version=\"1\"; Path=\"/acme\"");
      testCookieSetting("Part_Number=\"Rocket_Launcher_0001\"; Version=\"1\"; Path=\"/acme\"");
      testCookieSetting("Shipping=\"FedEx\"; Version=\"1\"; Path=\"/acme\"");
      testCookieSetting("Part_Number=\"Rocket_Launcher_0001\"; Version=\"1\"; Path=\"/acme\"");
      testCookieSetting("Part_Number=\"Riding_Rocket_0023\"; Version=\"1\"; Path=\"/acme/ammo\"");
   }
   
   /**
    * Test one cookie header.
    * @param headerValue The cookie header value.
    * @throws IOException
    */
   private void testCookie(String headerValue) throws IOException
   {
      CookieReader cr = new CookieReader(headerValue);
      List<Cookie> cookies = new ArrayList<Cookie>();
      Cookie cookie = cr.readCookie();

      while(cookie != null)
      {
         cookies.add(cookie);
         cookie = cr.readCookie();
      }
      
      // Rewrite the header
      String newHeaderValue = CookieUtils.format(cookies);
      
      // Compare initial and new headers
      assertEquals(headerValue, newHeaderValue);
   }
   
   /**
    * Test one set cookie header.
    * @param headerValue The set cookie header value.
    * @throws IOException
    */
   private void testCookieSetting(String headerValue) throws IOException
   {
      CookieReader cr = new CookieReader(headerValue);
      CookieSetting cookie = cr.readCookieSetting();

      // Rewrite the header
      String newHeaderValue = CookieUtils.format(cookie);
      
      // Compare initial and new headers
      boolean result = newHeaderValue.toLowerCase().startsWith(headerValue.toLowerCase());
      assertTrue(result);
   }
   
   /**
    * Test a cookie date value.
    * @param headerValue The cookie date value.
    * @throws IOException
    */
   private void testCookieDate(String dateValue) throws IOException
   {
      Date date = DateUtils.parse(dateValue, DateUtils.FORMAT_RFC_1036);
      
      // Rewrite the date
      String newDateValue = DateUtils.format(date, DateUtils.FORMAT_RFC_1036);
      
      // Compare initial and new headers
      assertEquals(dateValue, newDateValue);
   }

}
