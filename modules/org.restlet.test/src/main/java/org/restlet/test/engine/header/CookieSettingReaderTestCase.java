package org.restlet.test.engine.header;

import org.restlet.data.CookieSetting;
import org.restlet.data.CookieSetting.SameSite;
import org.restlet.engine.header.CookieSettingReader;
import org.restlet.test.RestletTestCase;

public class CookieSettingReaderTestCase extends RestletTestCase {

	public void testReadingWithoutSameSite() {
		CookieSetting readSetting = CookieSettingReader.read("cookie=value");
		assertNull(readSetting.getSameSite());
	}
	
	public void testReadingOfSameSite() {
		for(SameSite sameSite : SameSite.values()) {
			CookieSetting readSetting = CookieSettingReader.read("cookie=value; SameSite=" + sameSite);
			assertEquals(sameSite, readSetting.getSameSite());
		}
	}
	
	public void testReadingOfInvalidSameSite() {
		CookieSetting readSetting = CookieSettingReader.read("cookie=value; SameSite=InvalidSameSiteValue");
		assertNull( readSetting.getSameSite());
	}
	
	public void testReadingOfEmptySameSite() {
		CookieSetting readSetting = CookieSettingReader.read("cookie=value; SameSite=");
		assertNull( readSetting.getSameSite());
	}
}
