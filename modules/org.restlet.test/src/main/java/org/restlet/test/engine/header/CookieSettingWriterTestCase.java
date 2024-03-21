package org.restlet.test.engine.header;

import org.restlet.data.CookieSetting;
import org.restlet.data.CookieSetting.SameSite;
import org.restlet.engine.header.CookieSettingWriter;
import org.restlet.test.RestletTestCase;

public class CookieSettingWriterTestCase extends RestletTestCase {

	public void testWritingOfUnsetSameSite() {
		CookieSetting testSetting = new CookieSetting("cookie", "value");
		assertEquals("cookie=value", CookieSettingWriter.write(testSetting));
		
		assertEquals("cookie=value", CookieSettingWriter.write(testSetting));
	}
	
	public void testWritingOfSameSite() {
		for(SameSite sameSite : SameSite.values()) {
			CookieSetting testSetting = new CookieSetting("cookie", "value");
			testSetting.setSameSite(sameSite);
			assertEquals("cookie=value; SameSite=" + sameSite, CookieSettingWriter.write(testSetting));
		}
	}
}
