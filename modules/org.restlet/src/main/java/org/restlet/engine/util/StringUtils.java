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

package org.restlet.engine.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * String manipulation utilities.
 * 
 * @author Jerome Louvel
 */
public class StringUtils {

    /**
     * Represents an XML or HTML character entity reference.
     * 
     * @author Thierry Boileau
     * 
     */
    private static class CharacterEntity {
        /** Character reference. */
        private String name;

        /** Numeric character reference. */
        private Integer numericValue;

        /**
         * Constructor.
         * 
         * @param numericValue
         *            The numeric value of the entity.
         * @param name
         *            the name of the entity.
         */
        public CharacterEntity(Integer numericValue, String name) {
            super();
            this.numericValue = numericValue;
            this.name = name;
        }

        /**
         * Returns the name of the entity.
         * 
         * @return The name of the entity.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the numeric value of the entity.
         * 
         * @return The numeric value of the entity.
         */
        public Integer getNumericValue() {
            return numericValue;
        }
    }

    /**
     * Stores a list en entities and is able to return one given its numeric
     * value or name.
     * 
     * @author Thierry Boileau
     * 
     */
    private static class CharacterEntitySolver {
        /** Map of names of entities according to their numeric value. */
        private String[] toName;

        /** Map of numeric values of entities according to their name. */
        private Map<String, Integer> toValue;

        /**
         * Constructor.
         */
        public CharacterEntitySolver() {
            toName = new String[10000];
            toValue = new HashMap<String, Integer>();
        }

        /**
         * Adds an entity to solve.
         * 
         * @param value
         *            The numeric value of the entity.
         * @param name
         *            The name of the entity.
         * @return The solver.
         */
        public void add(Integer value, String name) {
            toName[value] = name;
            toValue.put(name, value);
        }

        /**
         * Returns the entity name according to its numeric value.
         * 
         * @param value
         *            The numeric value of the entity.
         * @return The entity name according to its numeric value.
         */
        public String getName(int value) {
            return toName[value];
        }

        /**
         * Returns the numeric value of an entity according to its name.
         * 
         * @param name
         *            The name of the entity.
         * @return The numeric value of an entity according to its name.
         */
        public Integer getValue(String name) {
            return toValue.get(name);
        }

    }

    /** Entities defined for HTML 4.0. */
    private static CharacterEntitySolver html40Entities;

    /** Entities defined in http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent. */
    private static List<CharacterEntity> htmlLat1;

    /** Entities defined in http://www.w3.org/TR/xhtml1/DTD/xhtml-special.ent. */
    private static List<CharacterEntity> htmlSpecial;

    /** Entities defined in http://www.w3.org/TR/xhtml1/DTD/xhtml-symbol.ent. */
    private static List<CharacterEntity> htmlSymbol;

    /** Basic entities. */
    private static List<CharacterEntity> xml10;

    static {
        xml10 = new ArrayList<CharacterEntity>();
        xml10.add(new CharacterEntity(34, "quot"));
        xml10.add(new CharacterEntity(38, "amp"));
        xml10.add(new CharacterEntity(62, "gt"));
        xml10.add(new CharacterEntity(60, "lt"));
        htmlLat1 = new ArrayList<CharacterEntity>();
        htmlLat1.add(new CharacterEntity(160, "nbsp"));
        htmlLat1.add(new CharacterEntity(161, "iexcl"));
        htmlLat1.add(new CharacterEntity(162, "cent"));
        htmlLat1.add(new CharacterEntity(163, "pound"));
        htmlLat1.add(new CharacterEntity(164, "curren"));
        htmlLat1.add(new CharacterEntity(165, "yen"));
        htmlLat1.add(new CharacterEntity(166, "brvbar"));
        htmlLat1.add(new CharacterEntity(167, "sect"));
        htmlLat1.add(new CharacterEntity(168, "uml"));
        htmlLat1.add(new CharacterEntity(169, "copy"));
        htmlLat1.add(new CharacterEntity(170, "ordf"));
        htmlLat1.add(new CharacterEntity(171, "laquo"));
        htmlLat1.add(new CharacterEntity(172, "not"));
        htmlLat1.add(new CharacterEntity(173, "shy"));
        htmlLat1.add(new CharacterEntity(174, "reg"));
        htmlLat1.add(new CharacterEntity(175, "macr"));
        htmlLat1.add(new CharacterEntity(176, "deg"));
        htmlLat1.add(new CharacterEntity(177, "plusmn"));
        htmlLat1.add(new CharacterEntity(178, "sup2"));
        htmlLat1.add(new CharacterEntity(179, "sup3"));
        htmlLat1.add(new CharacterEntity(180, "acute"));
        htmlLat1.add(new CharacterEntity(181, "micro"));
        htmlLat1.add(new CharacterEntity(182, "para"));
        htmlLat1.add(new CharacterEntity(183, "middot"));
        htmlLat1.add(new CharacterEntity(184, "cedil"));
        htmlLat1.add(new CharacterEntity(185, "sup1"));
        htmlLat1.add(new CharacterEntity(186, "ordm"));
        htmlLat1.add(new CharacterEntity(187, "raquo"));
        htmlLat1.add(new CharacterEntity(188, "frac14"));
        htmlLat1.add(new CharacterEntity(189, "frac12"));
        htmlLat1.add(new CharacterEntity(190, "frac34"));
        htmlLat1.add(new CharacterEntity(191, "iquest"));
        htmlLat1.add(new CharacterEntity(192, "Agrave"));
        htmlLat1.add(new CharacterEntity(193, "Aacute"));
        htmlLat1.add(new CharacterEntity(194, "Acirc"));
        htmlLat1.add(new CharacterEntity(195, "Atilde"));
        htmlLat1.add(new CharacterEntity(196, "Auml"));
        htmlLat1.add(new CharacterEntity(197, "Aring"));
        htmlLat1.add(new CharacterEntity(198, "AElig"));
        htmlLat1.add(new CharacterEntity(199, "Ccedil"));
        htmlLat1.add(new CharacterEntity(200, "Egrave"));
        htmlLat1.add(new CharacterEntity(201, "Eacute"));
        htmlLat1.add(new CharacterEntity(202, "Ecirc"));
        htmlLat1.add(new CharacterEntity(203, "Euml"));
        htmlLat1.add(new CharacterEntity(204, "Igrave"));
        htmlLat1.add(new CharacterEntity(205, "Iacute"));
        htmlLat1.add(new CharacterEntity(206, "Icirc"));
        htmlLat1.add(new CharacterEntity(207, "Iuml"));
        htmlLat1.add(new CharacterEntity(208, "ETH"));
        htmlLat1.add(new CharacterEntity(209, "Ntilde"));
        htmlLat1.add(new CharacterEntity(210, "Ograve"));
        htmlLat1.add(new CharacterEntity(211, "Oacute"));
        htmlLat1.add(new CharacterEntity(212, "Ocirc"));
        htmlLat1.add(new CharacterEntity(213, "Otilde"));
        htmlLat1.add(new CharacterEntity(214, "Ouml"));
        htmlLat1.add(new CharacterEntity(215, "times"));
        htmlLat1.add(new CharacterEntity(216, "Oslash"));
        htmlLat1.add(new CharacterEntity(217, "Ugrave"));
        htmlLat1.add(new CharacterEntity(218, "Uacute"));
        htmlLat1.add(new CharacterEntity(219, "Ucirc"));
        htmlLat1.add(new CharacterEntity(220, "Uuml"));
        htmlLat1.add(new CharacterEntity(221, "Yacute"));
        htmlLat1.add(new CharacterEntity(222, "THORN"));
        htmlLat1.add(new CharacterEntity(223, "szlig"));
        htmlLat1.add(new CharacterEntity(224, "agrave"));
        htmlLat1.add(new CharacterEntity(225, "aacute"));
        htmlLat1.add(new CharacterEntity(226, "acirc"));
        htmlLat1.add(new CharacterEntity(227, "atilde"));
        htmlLat1.add(new CharacterEntity(228, "auml"));
        htmlLat1.add(new CharacterEntity(229, "aring"));
        htmlLat1.add(new CharacterEntity(230, "aelig"));
        htmlLat1.add(new CharacterEntity(231, "ccedil"));
        htmlLat1.add(new CharacterEntity(232, "egrave"));
        htmlLat1.add(new CharacterEntity(233, "eacute"));
        htmlLat1.add(new CharacterEntity(234, "ecirc"));
        htmlLat1.add(new CharacterEntity(235, "euml"));
        htmlLat1.add(new CharacterEntity(236, "igrave"));
        htmlLat1.add(new CharacterEntity(237, "iacute"));
        htmlLat1.add(new CharacterEntity(238, "icirc"));
        htmlLat1.add(new CharacterEntity(239, "iuml"));
        htmlLat1.add(new CharacterEntity(240, "eth"));
        htmlLat1.add(new CharacterEntity(241, "ntilde"));
        htmlLat1.add(new CharacterEntity(242, "ograve"));
        htmlLat1.add(new CharacterEntity(243, "oacute"));
        htmlLat1.add(new CharacterEntity(244, "ocirc"));
        htmlLat1.add(new CharacterEntity(245, "otilde"));
        htmlLat1.add(new CharacterEntity(246, "ouml"));
        htmlLat1.add(new CharacterEntity(247, "divide"));
        htmlLat1.add(new CharacterEntity(248, "oslash"));
        htmlLat1.add(new CharacterEntity(249, "ugrave"));
        htmlLat1.add(new CharacterEntity(250, "uacute"));
        htmlLat1.add(new CharacterEntity(251, "ucirc"));
        htmlLat1.add(new CharacterEntity(252, "uuml"));
        htmlLat1.add(new CharacterEntity(253, "yacute"));
        htmlLat1.add(new CharacterEntity(254, "thorn"));
        htmlLat1.add(new CharacterEntity(255, "yuml"));
        htmlSymbol = new ArrayList<CharacterEntity>();
        htmlSymbol.add(new CharacterEntity(402, "fnof"));
        htmlSymbol.add(new CharacterEntity(913, "Alpha"));
        htmlSymbol.add(new CharacterEntity(914, "Beta"));
        htmlSymbol.add(new CharacterEntity(915, "Gamma"));
        htmlSymbol.add(new CharacterEntity(916, "Delta"));
        htmlSymbol.add(new CharacterEntity(917, "Epsilon"));
        htmlSymbol.add(new CharacterEntity(918, "Zeta"));
        htmlSymbol.add(new CharacterEntity(919, "Eta"));
        htmlSymbol.add(new CharacterEntity(920, "Theta"));
        htmlSymbol.add(new CharacterEntity(921, "Iota"));
        htmlSymbol.add(new CharacterEntity(922, "Kappa"));
        htmlSymbol.add(new CharacterEntity(923, "Lambda"));
        htmlSymbol.add(new CharacterEntity(924, "Mu"));
        htmlSymbol.add(new CharacterEntity(925, "Nu"));
        htmlSymbol.add(new CharacterEntity(926, "Xi"));
        htmlSymbol.add(new CharacterEntity(927, "Omicron"));
        htmlSymbol.add(new CharacterEntity(928, "Pi"));
        htmlSymbol.add(new CharacterEntity(929, "Rho"));
        htmlSymbol.add(new CharacterEntity(931, "Sigma"));
        htmlSymbol.add(new CharacterEntity(932, "Tau"));
        htmlSymbol.add(new CharacterEntity(933, "Upsilon"));
        htmlSymbol.add(new CharacterEntity(934, "Phi"));
        htmlSymbol.add(new CharacterEntity(935, "Chi"));
        htmlSymbol.add(new CharacterEntity(936, "Psi"));
        htmlSymbol.add(new CharacterEntity(937, "Omega"));
        htmlSymbol.add(new CharacterEntity(945, "alpha"));
        htmlSymbol.add(new CharacterEntity(946, "beta"));
        htmlSymbol.add(new CharacterEntity(947, "gamma"));
        htmlSymbol.add(new CharacterEntity(948, "delta"));
        htmlSymbol.add(new CharacterEntity(949, "epsilon"));
        htmlSymbol.add(new CharacterEntity(950, "zeta"));
        htmlSymbol.add(new CharacterEntity(951, "eta"));
        htmlSymbol.add(new CharacterEntity(952, "theta"));
        htmlSymbol.add(new CharacterEntity(953, "iota"));
        htmlSymbol.add(new CharacterEntity(954, "kappa"));
        htmlSymbol.add(new CharacterEntity(955, "lambda"));
        htmlSymbol.add(new CharacterEntity(956, "mu"));
        htmlSymbol.add(new CharacterEntity(957, "nu"));
        htmlSymbol.add(new CharacterEntity(958, "xi"));
        htmlSymbol.add(new CharacterEntity(959, "omicron"));
        htmlSymbol.add(new CharacterEntity(960, "pi"));
        htmlSymbol.add(new CharacterEntity(961, "rho"));
        htmlSymbol.add(new CharacterEntity(962, "sigmaf"));
        htmlSymbol.add(new CharacterEntity(963, "sigma"));
        htmlSymbol.add(new CharacterEntity(964, "tau"));
        htmlSymbol.add(new CharacterEntity(965, "upsilon"));
        htmlSymbol.add(new CharacterEntity(966, "phi"));
        htmlSymbol.add(new CharacterEntity(967, "chi"));
        htmlSymbol.add(new CharacterEntity(968, "psi"));
        htmlSymbol.add(new CharacterEntity(969, "omega"));
        htmlSymbol.add(new CharacterEntity(977, "thetasym"));
        htmlSymbol.add(new CharacterEntity(978, "upsih"));
        htmlSymbol.add(new CharacterEntity(982, "piv"));
        htmlSymbol.add(new CharacterEntity(8230, "hellip"));
        htmlSymbol.add(new CharacterEntity(8242, "prime"));
        htmlSymbol.add(new CharacterEntity(8243, "Prime"));
        htmlSymbol.add(new CharacterEntity(8254, "oline"));
        htmlSymbol.add(new CharacterEntity(8260, "frasl"));
        htmlSymbol.add(new CharacterEntity(8465, "image"));
        htmlSymbol.add(new CharacterEntity(8472, "weierp"));
        htmlSymbol.add(new CharacterEntity(8476, "real"));
        htmlSymbol.add(new CharacterEntity(8482, "trade"));
        htmlSymbol.add(new CharacterEntity(8501, "alefsym"));
        htmlSymbol.add(new CharacterEntity(8592, "larr"));
        htmlSymbol.add(new CharacterEntity(8593, "uarr"));
        htmlSymbol.add(new CharacterEntity(8594, "rarr"));
        htmlSymbol.add(new CharacterEntity(8595, "darr"));
        htmlSymbol.add(new CharacterEntity(8596, "harr"));
        htmlSymbol.add(new CharacterEntity(8629, "crarr"));
        htmlSymbol.add(new CharacterEntity(8656, "lArr"));
        htmlSymbol.add(new CharacterEntity(8657, "uArr"));
        htmlSymbol.add(new CharacterEntity(8658, "rArr"));
        htmlSymbol.add(new CharacterEntity(8659, "dArr"));
        htmlSymbol.add(new CharacterEntity(8660, "hArr"));
        htmlSymbol.add(new CharacterEntity(8704, "forall"));
        htmlSymbol.add(new CharacterEntity(8706, "part"));
        htmlSymbol.add(new CharacterEntity(8707, "exist"));
        htmlSymbol.add(new CharacterEntity(8709, "empty"));
        htmlSymbol.add(new CharacterEntity(8711, "nabla"));
        htmlSymbol.add(new CharacterEntity(8712, "isin"));
        htmlSymbol.add(new CharacterEntity(8713, "notin"));
        htmlSymbol.add(new CharacterEntity(8715, "ni"));
        htmlSymbol.add(new CharacterEntity(8719, "prod"));
        htmlSymbol.add(new CharacterEntity(8721, "sum"));
        htmlSymbol.add(new CharacterEntity(8722, "minus"));
        htmlSymbol.add(new CharacterEntity(8727, "lowast"));
        htmlSymbol.add(new CharacterEntity(8730, "radic"));
        htmlSymbol.add(new CharacterEntity(8733, "prop"));
        htmlSymbol.add(new CharacterEntity(8734, "infin"));
        htmlSymbol.add(new CharacterEntity(8736, "ang"));
        htmlSymbol.add(new CharacterEntity(8743, "and"));
        htmlSymbol.add(new CharacterEntity(8744, "or"));
        htmlSymbol.add(new CharacterEntity(8745, "cap"));
        htmlSymbol.add(new CharacterEntity(8746, "cup"));
        htmlSymbol.add(new CharacterEntity(8747, "int"));
        htmlSymbol.add(new CharacterEntity(8756, "there4"));
        htmlSymbol.add(new CharacterEntity(8764, "sim"));
        htmlSymbol.add(new CharacterEntity(8773, "cong"));
        htmlSymbol.add(new CharacterEntity(8776, "asymp"));
        htmlSymbol.add(new CharacterEntity(8800, "ne"));
        htmlSymbol.add(new CharacterEntity(8801, "equiv"));
        htmlSymbol.add(new CharacterEntity(8804, "le"));
        htmlSymbol.add(new CharacterEntity(8805, "ge"));
        htmlSymbol.add(new CharacterEntity(8834, "sub"));
        htmlSymbol.add(new CharacterEntity(8835, "sup"));
        htmlSymbol.add(new CharacterEntity(8836, "nsub"));
        htmlSymbol.add(new CharacterEntity(8838, "sube"));
        htmlSymbol.add(new CharacterEntity(8839, "supe"));
        htmlSymbol.add(new CharacterEntity(8853, "oplus"));
        htmlSymbol.add(new CharacterEntity(8855, "otimes"));
        htmlSymbol.add(new CharacterEntity(8869, "perp"));
        htmlSymbol.add(new CharacterEntity(8901, "sdot"));
        htmlSymbol.add(new CharacterEntity(8968, "lceil"));
        htmlSymbol.add(new CharacterEntity(8969, "rceil"));
        htmlSymbol.add(new CharacterEntity(8970, "lfloor"));
        htmlSymbol.add(new CharacterEntity(8971, "rfloor"));
        htmlSymbol.add(new CharacterEntity(9001, "lang"));
        htmlSymbol.add(new CharacterEntity(9002, "rang"));
        htmlSymbol.add(new CharacterEntity(9674, "loz"));
        htmlSymbol.add(new CharacterEntity(9824, "spades"));
        htmlSymbol.add(new CharacterEntity(9827, "clubs"));
        htmlSymbol.add(new CharacterEntity(9829, "hearts"));
        htmlSymbol.add(new CharacterEntity(9830, "diams"));
        htmlSpecial = new ArrayList<CharacterEntity>();
        htmlSpecial.add(new CharacterEntity(34, "quot"));
        htmlSpecial.add(new CharacterEntity(38, "amp"));
        htmlSpecial.add(new CharacterEntity(39, "apos"));
        htmlSpecial.add(new CharacterEntity(60, "lt"));
        htmlSpecial.add(new CharacterEntity(62, "gt"));
        htmlSpecial.add(new CharacterEntity(338, "OElig"));
        htmlSpecial.add(new CharacterEntity(339, "oelig"));
        htmlSpecial.add(new CharacterEntity(352, "Scaron"));
        htmlSpecial.add(new CharacterEntity(353, "scaron"));
        htmlSpecial.add(new CharacterEntity(376, "Yuml"));
        htmlSpecial.add(new CharacterEntity(710, "circ"));
        htmlSpecial.add(new CharacterEntity(732, "tilde"));
        htmlSpecial.add(new CharacterEntity(8194, "ensp"));
        htmlSpecial.add(new CharacterEntity(8195, "emsp"));
        htmlSpecial.add(new CharacterEntity(8201, "thinsp"));
        htmlSpecial.add(new CharacterEntity(8204, "zwnj"));
        htmlSpecial.add(new CharacterEntity(8205, "zwj"));
        htmlSpecial.add(new CharacterEntity(8206, "lrm"));
        htmlSpecial.add(new CharacterEntity(8207, "rlm"));
        htmlSpecial.add(new CharacterEntity(8211, "ndash"));
        htmlSpecial.add(new CharacterEntity(8212, "mdash"));
        htmlSpecial.add(new CharacterEntity(8216, "lsquo"));
        htmlSpecial.add(new CharacterEntity(8217, "rsquo"));
        htmlSpecial.add(new CharacterEntity(8218, "sbquo"));
        htmlSpecial.add(new CharacterEntity(8220, "ldquo"));
        htmlSpecial.add(new CharacterEntity(8221, "rdquo"));
        htmlSpecial.add(new CharacterEntity(8222, "bdquo"));
        htmlSpecial.add(new CharacterEntity(8224, "dagger"));
        htmlSpecial.add(new CharacterEntity(8225, "Dagger"));
        htmlSpecial.add(new CharacterEntity(8226, "bull"));
        htmlSpecial.add(new CharacterEntity(8240, "permil"));
        htmlSpecial.add(new CharacterEntity(8249, "lsaquo"));
        htmlSpecial.add(new CharacterEntity(8250, "rsaquo"));
        htmlSpecial.add(new CharacterEntity(8364, "euro"));
        List<CharacterEntity> list = new ArrayList<CharacterEntity>();
        list.addAll(xml10);
        list.addAll(htmlLat1);
        list.addAll(htmlSymbol);
        list.addAll(htmlSpecial);
        html40Entities = new CharacterEntitySolver();
        for (CharacterEntity entity : xml10) {
            html40Entities.add(entity.getNumericValue(), entity.getName());
        }
        for (CharacterEntity entity : htmlLat1) {
            html40Entities.add(entity.getNumericValue(), entity.getName());
        }
        for (CharacterEntity entity : htmlSymbol) {
            html40Entities.add(entity.getNumericValue(), entity.getName());
        }
        for (CharacterEntity entity : htmlSpecial) {
            html40Entities.add(entity.getNumericValue(), entity.getName());
        }
    }

    /**
     * Returns the string with the first character capitalized.
     * 
     * 
     * @param string
     *            a string reference to check
     * @return the string with the first character capitalized.
     */
    public static String firstLower(String string) {
        if (!isNullOrEmpty(string)) {
            return string.substring(0, 1).toLowerCase() + string.substring(1);
        }
        return string;
    }

    /**
     * Returns the string with the first character capitalized.
     * 
     * 
     * @param string
     *            a string reference to check
     * @return the string with the first character capitalized.
     */
    public static String firstUpper(String string) {
        if (!isNullOrEmpty(string)) {
            return string.substring(0, 1).toUpperCase() + string.substring(1);
        }
        return string;
    }

    // [ifndef gwt] method
    /**
     * Encodes the given String into a sequence of bytes using the Ascii
     * character set.
     * 
     * @param string
     *            The string to encode.
     * @return The String encoded with the Ascii character set as an array of
     *         bytes.
     */
    public static byte[] getAsciiBytes(String string) {
        if (string != null) {
            try {
                return string.getBytes("US-ASCII");
            } catch (Exception e) {
                // Should not happen.
                return null;
            }
        }
        return null;
    }

    // [ifndef gwt] method
    /**
     * Encodes the given String into a sequence of bytes using the Latin1
     * character set.
     * 
     * @param string
     *            The string to encode.
     * @return The String encoded with the Latin1 character set as an array of
     *         bytes.
     */
    public static byte[] getLatin1Bytes(String string) {
        if (string != null) {
            try {
                return string.getBytes("ISO-8859-1");
            } catch (Exception e) {
                // Should not happen.
                return null;
            }
        }
        return null;
    }

    /**
     * Returns the given {@link String} according to the HTML 4.0 encoding
     * rules.
     * 
     * @param str
     *            The {@link String} to encode.
     * @return The converted {@link String} according to the HTML 4.0 encoding
     *         rules.
     */
    public static String htmlEscape(String str) {
        if (str == null) {
            return null;
        }
        int len = str.length();
        StringBuilder sb = new StringBuilder((int) (len * 1.5));
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            String entityName = html40Entities.getName(c);
            if (entityName == null) {
                if (c > 127) {
                    // Escape non ASCII characters.
                    sb.append("&#").append(Integer.toString(c, 10)).append(';');
                } else {
                    // ASCII characters are not escaped.
                    sb.append(c);
                }
            } else {
                sb.append('&').append(entityName).append(';');
            }
        }
        return sb.toString();
    }

    /**
     * Returns the given {@link String} decoded according to the HTML 4.0
     * decoding rules.
     * 
     * @param str
     *            The {@link String} to decode.
     * @return The given {@link String} decoded according to the HTML 4.0
     *         decoding rules.
     */
    public static String htmlUnescape(String str) {
        if (str == null) {
            return null;
        }
        int len = str.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c == '&') {
                int nextIndex = i + 1;
                int semicolonIndex = -1;
                int ampersandIndex = -1;
                boolean stop = false;
                for (int j = nextIndex; !stop && j < len; j++) {
                    char ch = str.charAt(j);
                    if (';' == ch) {
                        semicolonIndex = j;
                        stop = true;
                    } else if ('&' == ch) {
                        ampersandIndex = j;
                        stop = true;
                    }
                }
                if (semicolonIndex != -1) {
                    // Entity found
                    if (nextIndex != semicolonIndex) {
                        int entityValue = -1;
                        String entityName = str.substring(nextIndex,
                                semicolonIndex);
                        if (entityName.charAt(0) == '#') {
                            // Numeric value
                            if (entityName.length() > 1) {
                                char hexChar = entityName.charAt(1);
                                try {
                                    if (hexChar == 'X') {
                                        entityValue = Integer.parseInt(
                                                entityName.substring(2), 16);
                                    } else if (hexChar == 'x') {
                                        entityValue = Integer.parseInt(
                                                entityName.substring(2), 16);
                                    } else {
                                        entityValue = Integer.parseInt(
                                                entityName.substring(1), 10);
                                    }
                                    if (!Character
                                            .isValidCodePoint(entityValue)) {
                                        // Invalid Unicode character
                                        entityValue = -1;
                                    }
                                } catch (NumberFormatException e) {
                                    entityValue = -1;
                                }
                            }
                        } else {
                            Integer val = html40Entities.getValue(entityName);
                            if (val != null) {
                                entityValue = val.intValue();
                            }
                        }
                        if (entityValue == -1) {
                            sb.append('&').append(entityName).append(';');
                        } else {
                            sb.append((char) entityValue);
                        }
                    } else {
                        sb.append("&;");
                    }
                    i = semicolonIndex;
                } else if (stop) {
                    // found a "&" character
                    sb.append(str, i, ampersandIndex).append('&');
                    i = ampersandIndex;
                } else {
                    // End of the string reached, no more entities to parse.
                    sb.append(str, i, len);
                    i = len;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Returns {@code true} if the given string is null or is the empty string.
     * 
     * <p>
     * Consider normalizing your string references with {@link #nullToEmpty}. If
     * you do, you can use {@link String#isEmpty()} instead of this method, and
     * you won't need special null-safe forms of methods like
     * {@link String#toUpperCase} either.
     * 
     * @param string
     *            a string reference to check
     * @return {@code true} if the string is null or is the empty string
     */
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Returns the given string if it is non-null; the empty string otherwise.
     * 
     * @param string
     *            the string to test and possibly return
     * @return {@code string} itself if it is non-null; {@code ""} if it is null
     */
    public static String nullToEmpty(String string) {
        return (string == null) ? "" : string;
    }

    // [ifndef gwt] method
    /**
     * Returns an list of trimmed token splitted with the split character ",".
     * 
     * @param stringToSplit
     *            The String to split.
     * @return List of tokens.
     */
    public static List<String> splitAndTrim(String stringToSplit) {
        return splitAndTrim(stringToSplit, ",");
    }

    // [ifndef gwt] method
    /**
     * Returns an list of trimmed token splitted with the split character.
     * 
     * @param stringToSplit
     *            The String to split.
     * @param splitCharacter
     *            The split Character.
     * @return List of tokens.
     */
    public static List<String> splitAndTrim(String stringToSplit,
            String splitCharacter) {
        List<String> list = new ArrayList<>();
        // StringTokenizer is 3 times more performant than String#split.
        StringTokenizer st = new StringTokenizer(stringToSplit, splitCharacter);
        while (st.hasMoreTokens()) {
            list.add(st.nextToken().trim());
        }
        return list;
    }

    /**
     * Strips a delimiter character from both ends of the source string.
     * 
     * @param source
     *            The source string to strip.
     * @param delimiter
     *            The character to remove.
     * @return The stripped string.
     */
    public static String strip(String source, char delimiter) {
        return strip(source, delimiter, true, true);
    }

    /**
     * Strips a delimiter character from a source string.
     * 
     * @param source
     *            The source string to strip.
     * @param delimiter
     *            The character to remove.
     * @param start
     *            Indicates if start of source should be stripped.
     * @param end
     *            Indicates if end of source should be stripped.
     * @return The stripped source string.
     */
    public static String strip(String source, char delimiter, boolean start,
            boolean end) {
        int beginIndex = 0;
        int endIndex = source.length();
        boolean stripping = true;

        // Strip beginning
        while (stripping && (beginIndex < endIndex)) {
            if (source.charAt(beginIndex) == delimiter) {
                beginIndex++;
            } else {
                stripping = false;
            }
        }

        // Strip end
        stripping = true;
        while (stripping && (beginIndex < endIndex - 1)) {
            if (source.charAt(endIndex - 1) == delimiter) {
                endIndex--;
            } else {
                stripping = false;
            }
        }

        return source.substring(beginIndex, endIndex);
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private StringUtils() {
    }

}
