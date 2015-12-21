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

package com.google.gwt.user.client;

public class UserAgentAsserter_UserAgentPropertyImplSafari implements com.google.gwt.user.client.UserAgentAsserter.UserAgentProperty {
  
  public boolean getUserAgentRuntimeWarning() {
    return true;
  }
  
  
  public native String getRuntimeValue() /*-{
    var ua = navigator.userAgent.toLowerCase();
    var makeVersion = function(result) {
      return (parseInt(result[1]) * 1000) + parseInt(result[2]);
    };
    if ((function() { 
      return (ua.indexOf('opera') != -1);
})()) return 'opera';
    if ((function() { 
      return (
      (ua.indexOf('webkit') != -1)
      ||
      (function() {
      if (ua.indexOf('chromeframe') != -1) {
      return true;
      }
      if (typeof window['ActiveXObject'] != 'undefined') {
      try {
      var obj = new ActiveXObject('ChromeTab.ChromeFrame');
      if (obj) {
      obj.registerBhoIfNeeded();
      return true;
      }
      } catch(e) { }
      }
      return false;
      })()
      )
})()) return 'safari';
    if ((function() { 
      return (ua.indexOf('msie') != -1 && ($doc.documentMode >= 9));
})()) return 'ie9';
    if ((function() { 
      return (ua.indexOf('msie') != -1 && ($doc.documentMode >= 8));
})()) return 'ie8';
    if ((function() { 
      var result = /msie ([0-9]+)\.([0-9]+)/.exec(ua);
      if (result && result.length == 3)
        return (makeVersion(result) >= 6000);
})()) return 'ie6';
    if ((function() { 
      return (ua.indexOf('gecko') != -1);
})()) return 'gecko1_8';
    return 'unknown';
  }-*/;
  
  
  public String getCompileTimeValue() {
    return "safari";
  }
}
