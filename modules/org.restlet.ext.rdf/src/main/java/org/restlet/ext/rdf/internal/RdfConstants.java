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

package org.restlet.ext.rdf.internal;

import org.restlet.data.Reference;

/**
 * Constants related to RDF documents.
 * 
 * @author Thierry Boileau
 */
public class RdfConstants {

    /** List "first". */
    public static final Reference LIST_FIRST = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#first");

    /** List "rest". */
    public static final Reference LIST_REST = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#rest");

    /** Object "nil". */
    public static final Reference OBJECT_NIL = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");

    /** Predicate "implies" . */
    public static final Reference PREDICATE_IMPLIES = new Reference(
            "http://www.w3.org/2000/10/swap/log#implies");

    /** Predicate "object". */
    public static final Reference PREDICATE_OBJECT = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#object");

    /** Predicate "predicate". */
    public static final Reference PREDICATE_PREDICATE = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate");

    /** Predicate "same as". */
    public static final Reference PREDICATE_SAME = new Reference(
            "http://www.w3.org/2002/07/owl#sameAs");

    /** Predicate "statement". */
    public static final Reference PREDICATE_STATEMENT = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement");

    /** Predicate "subject". */
    public static final Reference PREDICATE_SUBJECT = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#subject");

    /** Predicate "is a". */
    public static final Reference PREDICATE_TYPE = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

    /** Rdf schema. */
    public static final Reference RDF_SCHEMA = new Reference(
            "http://www.w3.org/2000/01/rdf-schema#");

    /** Rdf syntax. */
    public static final Reference RDF_SYNTAX = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

    /** XML schema. */
    public static final Reference XML_SCHEMA = new Reference(
            "http://www.w3.org/2001/XMLSchema#");

    /** Float type of the XML schema. */
    public static final Reference XML_SCHEMA_TYPE_FLOAT = new Reference(
            "http://www.w3.org/2001/XMLSchema#float");

    /** Integer type of the XML schema. */
    public static final Reference XML_SCHEMA_TYPE_INTEGER = new Reference(
            "http://www.w3.org/2001/XMLSchema#int");

}
