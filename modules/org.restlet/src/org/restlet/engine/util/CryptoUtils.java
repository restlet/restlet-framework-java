/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.util;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Simple usage of standard cipher features from JRE.
 * 
 * @author Remi Dewitte <remi@gide.net>
 */
public class CryptoUtils {

    protected static Cipher newCipher(String algo, String base64Secret, int mode)
            throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(algo);
        cipher.init(mode, new SecretKeySpec(Base64.decode(base64Secret), algo));
        return cipher;
    }

    public static byte[] encrypt(String algo, String base64Secret,
            String content) throws GeneralSecurityException {
        return crypt(algo, base64Secret, Cipher.ENCRYPT_MODE, content
                .getBytes());
    }

    public static String decrypt(String algo, String base64Secret,
            byte[] encrypted) throws GeneralSecurityException {
        byte[] original = crypt(algo, base64Secret, Cipher.DECRYPT_MODE,
                encrypted);
        return new String(original);
    }

    protected static byte[] crypt(String algo, String base64Secret, int mode,
            byte[] what) throws GeneralSecurityException {
        return newCipher(algo, base64Secret, mode).doFinal(what);
    }
}
