/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package com.google.gwt.i18n.client.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.constants.NumberConstants;
import com.google.gwt.i18n.client.constants.NumberConstantsImpl;
import com.google.gwt.i18n.client.DateTimeFormatInfo;
import com.google.gwt.i18n.client.impl.cldr.DateTimeFormatInfoImpl;

public class LocaleInfoImpl_ extends LocaleInfoImpl_shared {
  @Override
  public String getLocaleName() {
    return "default";
  }
  
  @Override
  public String getLocaleQueryParam() {
    return "locale";
  }
  
  @Override
  public DateTimeFormatInfo getDateTimeFormatInfo() {
    return GWT.create(com.google.gwt.i18n.client.impl.cldr.DateTimeFormatInfoImpl.class);
  }
  
  @Override
  public NumberConstants getNumberConstants() {
    return GWT.create(com.google.gwt.i18n.client.constants.NumberConstantsImpl.class);
  }
}
